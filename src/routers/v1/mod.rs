mod auth;

use axum::Router;

use crate::ServerRouter;

pub fn setup_router() -> ServerRouter {
    Router::new().nest("/auth", auth::setup_router())
}
