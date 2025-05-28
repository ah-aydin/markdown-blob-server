use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
pub struct UserResponse {
    pub id: u64,
    pub email: String,
    pub created_at: u64,
    pub updated_at: u64,
}

#[derive(Debug, Clone, Serialize)]
pub struct User {
    pub id: u64,
    pub email: String,
    pub password_hash: String,
    pub created_at: u64,
    pub updated_at: u64,
}
