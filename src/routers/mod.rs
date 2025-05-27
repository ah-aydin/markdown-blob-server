use axum::Router;

use crate::{ServerRouter, ServerState};

mod v1;

pub fn setup_router(server_state: ServerState) -> ServerRouter {
    Router::new().nest("/v1", v1::setup_router(server_state.clone()))
}
