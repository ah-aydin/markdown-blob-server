use axum::Router;

use crate::ServerRouter;

mod v1;

pub fn setup_router() -> ServerRouter {
    Router::new().nest("/v1", v1::setup_router())
}
