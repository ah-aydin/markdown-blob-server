use crate::handlers;
use crate::middleware::auth_middleware;
use crate::ServerState;

use axum::middleware::from_fn_with_state;
use axum::routing::delete;
use axum::routing::get;
use axum::routing::post;
use axum::Router;

use crate::ServerRouter;

pub fn setup_router(server_state: ServerState) -> ServerRouter {
    Router::new()
        .route("/", get(handlers::markdown_file_storage::get_page))
        .route(
            "/{file_name}",
            post(handlers::markdown_file_storage::upload),
        )
        .route(
            "/{file_name}",
            delete(handlers::markdown_file_storage::delete),
        )
        .route(
            "/{file_name}",
            get(handlers::markdown_file_storage::download),
        )
        .layer(from_fn_with_state(server_state.clone(), auth_middleware))
}
