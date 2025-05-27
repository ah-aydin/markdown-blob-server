use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::response::Response;
use axum::Json;
use serde_json::json;
use tracing::error;

pub enum ServerError {
    BadRequest(String),   // 400
    Unauthorized(String), // 401
    NotFound(String),     // 404
    Conflict(String),     // 409

    InternalServerError(String), // 500
}

impl IntoResponse for ServerError {
    fn into_response(self) -> Response {
        let (status_code, error_type, msg) = match self {
            ServerError::BadRequest(msg) => (StatusCode::BAD_REQUEST, "INVALID_INPUT", msg),
            ServerError::Unauthorized(msg) => (StatusCode::UNAUTHORIZED, "UNAUTHORIZED", msg),
            ServerError::NotFound(msg) => (StatusCode::NOT_FOUND, "NOT_FOUND", msg),
            ServerError::Conflict(msg) => (StatusCode::CONFLICT, "CONFLICT", msg),
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
        ServerError::InternalServerError("Unknown error".to_string())
    }
}

impl From<argon2::password_hash::Error> for ServerError {
    fn from(err: argon2::password_hash::Error) -> Self {
        error!("Pasword hash error error: {:?}", err);
        ServerError::InternalServerError("Unknown error".to_string())
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
        error!("JWT failure: {:?}", err);
        ServerError::InternalServerError("Unknown error".to_string())
    }
}
