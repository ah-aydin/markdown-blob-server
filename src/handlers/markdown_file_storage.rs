use axum::body::Bytes;
use axum::extract::Path;
use axum::extract::Query;
use axum::extract::State;
use axum::Extension;
use axum::Json;

use crate::auth::AuthedUser;
use crate::error::ServerError;
use crate::models::markdown_file::MarkdownFileResponse;
use crate::models::page::PageQuery;
use crate::models::page::PageResponse;
use crate::ServerState;

pub async fn get_page(
    State(ServerState {
        markdown_file_repository,
        ..
    }): State<ServerState>,
    Query(page_query): Query<PageQuery>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<Json<PageResponse<MarkdownFileResponse>>, ServerError> {
    markdown_file_repository
        .get_page_for_user_id(user_id, page_query.page, page_query.size)
        .await
        .map(|page| Json(page))
}

pub async fn upload(
    State(ServerState {
        markdown_file_storage,
        markdown_file_repository,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
    file_bytes: Bytes,
) -> Result<Json<MarkdownFileResponse>, ServerError> {
    let inserted_file_response_data = markdown_file_repository.insert(user_id, &file_name).await?;

    let s3_response = markdown_file_storage
        .upload(&format!("{user_id}/{file_name}"), file_bytes.to_vec())
        .await;
    if let Some(err) = s3_response.err() {
        markdown_file_repository
            .delete_by_id(inserted_file_response_data.id)
            .await?;
        return Err(err);
    }

    Ok(Json(inserted_file_response_data))
}

pub async fn delete(
    State(ServerState {
        markdown_file_storage,
        markdown_file_repository,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<(), ServerError> {
    markdown_file_storage
        .delete(&format!("{user_id}/{file_name}"))
        .await?;
    markdown_file_repository.delete(user_id, &file_name).await?;
    Ok(())
}

pub async fn download(
    State(ServerState {
        markdown_file_storage,
        markdown_file_repository,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<Vec<u8>, ServerError> {
    if !markdown_file_repository.exists(user_id, &file_name).await? {
        return Err(ServerError::NotFound(format!("{file_name} does not exist")));
    }
    Ok(markdown_file_storage
        .download(&format!("{user_id}/{file_name}"))
        .await?)
}
