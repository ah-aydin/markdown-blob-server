use axum::{
    extract::{Request, State},
    middleware::Next,
    response::Response,
};
use tracing::error;

use crate::{auth::AuthedUser, error::ServerError, ServerState};

const AUTH_HEADER_PREFIX: &'static str = "Bearer ";

pub async fn auth_middleware(
    State(ServerState { jwt_handler, .. }): State<ServerState>,
    mut request: Request,
    next: Next,
) -> Result<Response, ServerError> {
    let auth_header = request.headers().get("Authorization");
    if auth_header.is_none() {
        return Err(ServerError::BadRequest(
            "Authorization header is missing".to_string(),
        ));
    }

    let auth_header = auth_header.unwrap().to_str();
    if auth_header.is_err() {
        error!("Failed to transform auth header into string");
        return Err(ServerError::InternalServerError(
            "Unknown error".to_string(),
        ));
    }

    let auth_header = auth_header.unwrap();
    if !auth_header.starts_with(AUTH_HEADER_PREFIX) {
        return Err(ServerError::BadRequest(
            "Bad authorization header format".to_string(),
        ));
    }

    let jwt_token = auth_header.trim_start_matches(AUTH_HEADER_PREFIX);
    let (is_valid, user_id) = jwt_handler.validate_access_token(jwt_token)?;
    if !is_valid {
        return Err(ServerError::Unauthorized("Token is not valid".to_string()));
    }

    let authed_user = AuthedUser::new(user_id);

    request.extensions_mut().insert(authed_user);

    Ok(next.run(request).await)
}
