use axum::body::Bytes;
use axum::extract::Path;
use axum::extract::Query;
use axum::extract::State;
use axum::Extension;

use crate::auth::AuthedUser;
use crate::error::ServerError;
use crate::models::page::PageQuery;
use crate::ServerState;

// TODO better error handling for S3 client calls
// TODO handle accorindg to user ID
// TODO check for modified files presence
// TODO modify entries in the markdown file table accordingly
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
    State(ServerState {
        markdown_file_storage,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
    file_bytes: Bytes,
) -> Result<(), ServerError> {
    markdown_file_storage
        .upload(&format!("{user_id}/{file_name}"), file_bytes.to_vec())
        .await;
    Ok(())
}

pub async fn delete(
    State(ServerState {
        markdown_file_storage,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<(), ServerError> {
    markdown_file_storage
        .delete(&format!("{user_id}/{file_name}"))
        .await;
    Ok(())
}

pub async fn download(
    State(ServerState {
        markdown_file_storage,
        ..
    }): State<ServerState>,
    Path(file_name): Path<String>,
    Extension(AuthedUser { user_id }): Extension<AuthedUser>,
) -> Result<Vec<u8>, ServerError> {
    Ok(markdown_file_storage
        .download(&format!("{user_id}/{file_name}"))
        .await)
}
