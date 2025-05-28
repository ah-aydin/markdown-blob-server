use serde::Deserialize;
use serde::Serialize;

#[derive(Deserialize)]
pub struct PageQuery {
    pub page: u64,
    pub size: u64,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct PageResponse<T> {
    pub content: Vec<T>,
    pub total_elements: u64,
    pub total_pages: u64,
    pub page_number: u64,
    pub page_size: u64,
    pub is_first: bool,
    pub is_last: bool,
    pub has_next: bool,
    pub has_previous: bool,
}

impl<T> PageResponse<T> {
    pub fn new(
        content: Vec<T>,
        page_number: u64,
        page_size: u64,
        total_elements: u64,
    ) -> PageResponse<T> {
        let total_pages = (total_elements + page_size - 1) / page_size;
        PageResponse {
            content,
            total_elements,
            total_pages,
            page_number,
            page_size,
            is_first: page_number == 0,
            is_last: page_number >= total_pages.saturating_sub(1),
            has_next: page_number < total_pages.saturating_sub(1),
            has_previous: page_number > 0,
        }
    }
}
