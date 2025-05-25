use axum::extract::State;
use axum::Json;

use crate::error::ServerError;
use crate::models::auth::LoginPayload;
use crate::models::auth::LoginResponse;
use crate::models::auth::RefreshPayload;
use crate::models::auth::SignupPayload;
use crate::models::user::UserResponse;
use crate::password::hash_password;
use crate::password::verify_password;
use crate::ServerState;

pub async fn signup(
    State(ServerState {
        user_repository, ..
    }): State<ServerState>,
    Json(SignupPayload { email, password }): Json<SignupPayload>,
) -> Result<Json<UserResponse>, ServerError> {
    if user_repository.get_user_by_email(&email).await?.is_some() {
        return Err(ServerError::Conflict(format!(
            "User with email '{email}' already exists"
        )));
    }

    Ok(Json(
        user_repository
            .insert(&email, &hash_password(&password)?)
            .await?,
    ))
}

pub async fn login(
    State(ServerState {
        jwt_handler,
        user_repository,
        ..
    }): State<ServerState>,
    Json(LoginPayload { email, password }): Json<LoginPayload>,
) -> Result<Json<LoginResponse>, ServerError> {
    let user = user_repository.get_user_by_email(&email).await?;
    if user.is_none() {
        return Err(ServerError::NotFound("User not found".to_string()));
    }

    let user = user.unwrap();
    if !verify_password(&password, &user.password_hash)? {
        return Err(ServerError::Unauthorized("Invalid password".to_string()));
    }

    Ok(Json(LoginResponse {
        access_token: jwt_handler.create_auth_token_for_user(user.id)?,
        refresh_token: jwt_handler.create_refresh_token_for_user(user.id)?,
    }))
}

pub async fn refresh(
    State(ServerState { jwt_handler, .. }): State<ServerState>,
    Json(RefreshPayload { refresh_token }): Json<RefreshPayload>,
) -> Result<Json<LoginResponse>, ServerError> {
    let (is_refresh, user_id) = jwt_handler.validate_refresh_token(refresh_token)?;
    if !is_refresh {
        return Err(ServerError::Conflict("Invalid token".to_string()));
    }

    Ok(Json(LoginResponse {
        access_token: jwt_handler.create_auth_token_for_user(user_id)?,
        refresh_token: jwt_handler.create_refresh_token_for_user(user_id)?,
    }))
}
