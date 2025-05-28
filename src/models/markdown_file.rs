use serde::Deserialize;
use serde::Serialize;
use sqlx::FromRow;

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize, FromRow)]
pub struct MarkdownFileResponse {
    pub id: u64,
    pub user_id: u64,
    pub file_name: String,
    pub created_at: u64,
    pub updated_at: u64,
}
