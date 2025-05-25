use serde::Deserialize;
use serde::Serialize;

#[derive(Deserialize)]
pub struct SignupPayload {
    pub email: String,
    pub password: String,
}

#[derive(Deserialize)]
pub struct LoginPayload {
    pub email: String,
    pub password: String,
}

#[derive(Serialize)]
pub struct LoginResponse {
    pub access_token: String,
    pub refresh_token: String,
}

#[derive(Deserialize)]
pub struct RefreshPayload {
    pub refresh_token: String,
}
