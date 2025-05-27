use crate::handlers;

use axum::routing::post;
use axum::Router;

use crate::ServerRouter;

pub fn setup_router() -> ServerRouter {
    Router::new()
        .route("/", post(handlers::auth::signup))
        .route("/login", post(handlers::auth::login))
        .route("/refresh", post(handlers::auth::refresh))
}
