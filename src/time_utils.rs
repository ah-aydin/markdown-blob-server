use std::time::SystemTime;
use std::time::UNIX_EPOCH;

pub fn get_now() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .expect("Time went backwards")
        .as_millis() as u64
}
