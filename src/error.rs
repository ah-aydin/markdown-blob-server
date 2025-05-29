use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::response::Response;
use axum::Json;
use jwt_simple::JWTError;
use serde_json::json;
use tracing::error;

pub enum ServerError {
    BadRequest(String),   // 400
    Unauthorized(String), // 401
    NotFound(String),     // 404
    Conflict(String),     // 409

    TokenExpired(String), // 401

    InternalServerError(String), // 500
}

impl ServerError {
    fn internal_server_error() -> ServerError {
        ServerError::InternalServerError("Unknown error".to_string())
    }
}

impl IntoResponse for ServerError {
    fn into_response(self) -> Response {
        let (status_code, error_type, msg) = match self {
            ServerError::BadRequest(msg) => (StatusCode::BAD_REQUEST, "INVALID_INPUT", msg),
            ServerError::Unauthorized(msg) => (StatusCode::UNAUTHORIZED, "UNAUTHORIZED", msg),
            ServerError::NotFound(msg) => (StatusCode::NOT_FOUND, "NOT_FOUND", msg),
            ServerError::Conflict(msg) => (StatusCode::CONFLICT, "CONFLICT", msg),

            ServerError::TokenExpired(msg) => (StatusCode::UNAUTHORIZED, "TOKEN_EXPIRED", msg),

            ServerError::InternalServerError(msg) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                msg,
            ),
        };

        let body = Json(json!({"error_type": error_type, "message": msg}));
        (status_code, body).into_response()
    }
}

impl From<sqlx::Error> for ServerError {
    fn from(err: sqlx::Error) -> Self {
        error!("DB error: {:?}", err);
        ServerError::internal_server_error()
    }
}

impl From<argon2::password_hash::Error> for ServerError {
    fn from(err: argon2::password_hash::Error) -> Self {
        error!("Pasword hash error error: {:?}", err);
        ServerError::internal_server_error()
    }
}

impl From<axum::extract::rejection::JsonRejection> for ServerError {
    fn from(err: axum::extract::rejection::JsonRejection) -> Self {
        error!("Invalid JSON payload: {:?}", err);
        ServerError::BadRequest(format!("Invalid request body format: {:?}", err))
    }
}

impl From<jwt_simple::Error> for ServerError {
    fn from(err: jwt_simple::Error) -> Self {
        for cause in err.chain() {
            if let Some(JWTError::TokenHasExpired) = cause.downcast_ref::<JWTError>() {
                return ServerError::TokenExpired("The token has expired".to_string());
            }
        }
        error!("JWT failure: {:?}", err);
        ServerError::internal_server_error()
    }
}

impl<E: std::fmt::Debug, R: std::fmt::Debug>
    From<aws_smithy_runtime_api::client::result::SdkError<E, R>> for ServerError
{
    fn from(err: aws_smithy_runtime_api::client::result::SdkError<E, R>) -> Self {
        error!("AWS S3 error: {:?}", err);
        ServerError::internal_server_error()
    }
}

impl From<aws_smithy_types::byte_stream::error::Error> for ServerError {
    fn from(err: aws_smithy_types::byte_stream::error::Error) -> Self {
        error!("AWS ByteStream error: {:?}", err);
        ServerError::internal_server_error()
    }
}
