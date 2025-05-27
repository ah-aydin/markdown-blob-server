use axum::extract::Path;
use axum::extract::Query;
use axum::extract::State;
use axum::Extension;

use crate::auth::AuthedUser;
use crate::error::ServerError;
use crate::models::page::PageQuery;
use crate::ServerState;

pub async fn get_page(
    State(ServerState { .. }): State<ServerState>,
    page_query: Query<PageQuery>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<String, ServerError> {
    Ok(format!(
        "Getting page {} with size {} for user {}",
        page_query.page, page_query.size, user_id
    ))
}

pub async fn upload(
    State(ServerState { .. }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<String, ServerError> {
    Ok(format!("Uploading {file_name} for user {user_id}"))
}

pub async fn delete(
    State(ServerState { .. }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<String, ServerError> {
    Ok(format!("Deleting {file_name} for user {user_id}"))
}

pub async fn download(
    State(ServerState { .. }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<String, ServerError> {
    Ok(format!("Downloading {file_name} for user {user_id}"))
}
