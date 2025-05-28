use sqlx;
use sqlx::MySql;
use sqlx::Pool;
use tracing::info;

use crate::error::ServerError;
use crate::models::markdown_file::MarkdownFileResponse;
use crate::models::page::PageResponse;
use crate::time_utils::get_now;

#[derive(Clone)]
pub struct MarkdownFileRepository {
    pool: Pool<MySql>,
}

impl MarkdownFileRepository {
    pub fn init(pool: Pool<MySql>) -> MarkdownFileRepository {
        info!("⏳ Creating MarkdownFilesRepository");
        MarkdownFileRepository { pool }
    }

    pub async fn insert(
        &self,
        user_id: u64,
        file_name: &str,
    ) -> Result<MarkdownFileResponse, ServerError> {
        let now = get_now();
        let entry_id = sqlx::query!(
            r#"
            INSERT INTO markdown_files (user_id, file_name, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            updated_at=VALUES(updated_at)
            "#,
            user_id,
            file_name,
            now,
            now
        )
        .execute(&self.pool)
        .await
        .map_err(|err| ServerError::from(err))?
        .last_insert_id();

        Ok(MarkdownFileResponse {
            id: entry_id,
            user_id,
            file_name: file_name.to_string(),
            created_at: now,
            updated_at: now,
        })
    }

    pub async fn delete(&self, user_id: u64, file_name: &str) -> Result<(), ServerError> {
        sqlx::query!(
            r#"DELETE FROM markdown_files WHERE user_id=? AND file_name=?"#,
            user_id,
            file_name
        )
        .execute(&self.pool)
        .await
        .map(|_| ())
        .map_err(|err| ServerError::from(err))
    }

    pub async fn delete_by_id(&self, id: u64) -> Result<(), ServerError> {
        sqlx::query!(r#"DELETE FROM markdown_files WHERE id = ?"#, id)
            .execute(&self.pool)
            .await
            .map(|_| ())
            .map_err(|err| ServerError::from(err))
    }

    pub async fn get_page_for_user_id(
        &self,
        user_id: u64,
        page: u64,
        size: u64,
    ) -> Result<PageResponse<MarkdownFileResponse>, ServerError> {
        let total_elements: i64 =
            sqlx::query_scalar(r#"SELECT COUNT(*) FROM markdown_files WHERE user_id=?"#)
                .bind(user_id)
                .fetch_one(&self.pool)
                .await?;

        let content: Vec<MarkdownFileResponse> = sqlx::query_as!(
            MarkdownFileResponse,
            r#"
            SELECT id, user_id, file_name, created_at, updated_at FROM markdown_files
            WHERE user_id = ? ORDER BY id ASC LIMIT ? OFFSET ?
            "#,
            user_id,
            size,
            page * size
        )
        .fetch_all(&self.pool)
        .await
        .map_err(|err| ServerError::from(err))?;

        Ok(PageResponse::new(
            content,
            page as u64,
            size as u64,
            total_elements as u64,
        ))
    }

    pub async fn exists(&self, user_id: u64, file_name: &str) -> Result<bool, ServerError> {
        sqlx::query_scalar(
            r#"
            SELECT EXISTS(SELECT 1 FROM markdown_files WHERE user_id=? AND file_name=?)
            "#,
        )
        .bind(user_id)
        .bind(file_name)
        .fetch_one(&self.pool)
        .await
        .map_err(|err| ServerError::from(err))
    }
}
