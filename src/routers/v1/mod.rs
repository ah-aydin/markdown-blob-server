mod auth;
mod markdown_file_storage;

use axum::Router;

use crate::{ServerRouter, ServerState};

pub fn setup_router(server_state: ServerState) -> ServerRouter {
    Router::new().nest("/auth", auth::setup_router()).nest(
        "/markdown-file-storage",
        markdown_file_storage::setup_router(server_state.clone()),
    )
}
