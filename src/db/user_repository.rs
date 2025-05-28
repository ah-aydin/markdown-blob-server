use sqlx;
use sqlx::MySql;
use sqlx::Pool;
use tracing::info;

use crate::error::ServerError;
use crate::models::user::User;
use crate::models::user::UserResponse;
use crate::time_utils::get_now;

#[derive(Clone)]
pub struct UserRepository {
    pool: Pool<MySql>,
}

impl UserRepository {
    pub fn init(pool: Pool<MySql>) -> UserRepository {
        info!("⏳ Creating UserRepository");
        UserRepository { pool }
    }

    pub async fn insert(
        &self,
        email: &str,
        password_hash: &str,
    ) -> Result<UserResponse, ServerError> {
        let now = get_now();
        let user_id = sqlx::query!(
            r#"
            INSERT INTO users (email, password_hash, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            "#,
            email,
            password_hash,
            now,
            now
        )
        .execute(&self.pool)
        .await
        .map_err(|err| ServerError::from(err))?
        .last_insert_id();

        Ok(UserResponse {
            id: user_id,
            email: email.to_string(),
            created_at: now,
            updated_at: now,
        })
    }

    pub async fn get_user_by_email(&self, email: &str) -> Result<Option<User>, ServerError> {
        sqlx::query_as!(User, "SELECT * FROM users WHERE email = ?", email)
            .fetch_optional(&self.pool)
            .await
            .map_err(|err| ServerError::from(err))
    }
}
