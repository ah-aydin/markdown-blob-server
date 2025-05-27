mod auth;
mod db;
mod env;
mod error;
mod handlers;
mod middleware;
mod models;
mod password;
mod routers;
mod s3;

use std::time::Duration;

use auth::jwt::JwtHandler;
use axum::http;
use axum::routing::Router;
use db::user_repository::UserRepository;
use db::DB;
use env::Env;
use env::EnvType;
use s3::MarkdownFileStorage;
use tower_http::cors::CorsLayer;
use tower_http::timeout::TimeoutLayer;
use tower_http::trace::TraceLayer;
use tracing::info;
use tracing::info_span;
use tracing::Level;

pub type ServerRouter = Router<ServerState>;

#[derive(Clone)]
pub struct ServerState {
    env_type: EnvType,
    jwt_handler: JwtHandler,
    markdown_file_storage: MarkdownFileStorage,

    user_repository: UserRepository,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt()
        .with_max_level(Level::INFO)
        .with_span_events(tracing_subscriber::fmt::format::FmtSpan::NONE)
        .init();

    let env = Env::init();

    let db = DB::init(&env).await;
    let addr = format!("0.0.0.0:{}", env.server_port);

    let server_state = ServerState {
        env_type: env.env_type,
        jwt_handler: JwtHandler::init(&env),
        markdown_file_storage: MarkdownFileStorage::init(&env).await,
        user_repository: UserRepository::init(db.pool.clone()),
    };

    let app = Router::new()
        .nest("/api", routers::setup_router(server_state.clone()))
        .with_state(server_state)
        .layer(TimeoutLayer::new(Duration::from_secs(30)))
        .layer(CorsLayer::new().allow_origin(tower_http::cors::Any))
        .layer(
            TraceLayer::new_for_http()
                .make_span_with(|request: &http::Request<_>| {
                    info_span!(
                        "",
                        method=%request.method(),
                        uri=%request.uri(),
                        correlation_id=uuid::Uuid::new_v4().to_string(),
                        status=tracing::field::Empty,
                        latency=tracing::field::Empty
                    )
                })
                .on_request(|request: &http::Request<_>, _: &tracing::Span| {
                    info!("{} {}", request.method(), request.uri())
                })
                .on_response(
                    |response: &http::Response<_>, latency: Duration, span: &tracing::Span| {
                        let status = response.status();
                        span.record("status", format!("{:?}", status));
                        span.record("latency", format!("{:?}", latency));
                    },
                ),
        );

    let listener = tokio::net::TcpListener::bind(&addr).await.unwrap();

    info!("Starting server on port {}", env.server_port);
    drop(env);

    axum::serve(listener, app).await.unwrap();
}
