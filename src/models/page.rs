use serde::Deserialize;

#[derive(Deserialize)]
pub struct PageQuery {
    pub page: usize,
    pub size: usize,
}
