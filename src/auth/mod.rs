pub mod jwt;

#[derive(Debug, Clone)]
pub struct AuthedUser {
    pub user_id: u64,
}

impl AuthedUser {
    pub fn new(user_id: u64) -> AuthedUser {
        AuthedUser { user_id }
    }
}
