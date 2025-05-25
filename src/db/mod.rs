pub mod user_repository;

use sqlx::mysql::MySqlPoolOptions;
use sqlx::MySql;
use sqlx::Pool;
use tracing::info;

use crate::env::Env;

pub struct DB {
    pub pool: Pool<MySql>,
}

impl DB {
    pub async fn init(env: &Env) -> DB {
        let db_url = &env.db_url;
        let db_url_without_login_data = db_url
            .split("@")
            .nth(1)
            .expect("❌ Database URL has wrong format");

        info!(
            "⏳ Initializing DB connection to {}",
            db_url_without_login_data
        );

        let pool = MySqlPoolOptions::new()
            .max_connections(8)
            .connect(db_url)
            .await
            .expect(&format!(
                "❌ Failed to connect to DB: {}",
                db_url_without_login_data
            ));

        DB { pool }
    }
}
