use tracing::error;
use tracing::info;

#[derive(Debug, Clone, Copy)]
pub enum EnvType {
    PROD,
    QA,
}

impl EnvType {
    fn from_str(s: &str) -> EnvType {
        match s {
            "PROD" => EnvType::PROD,
            "QA" => EnvType::QA,
            _ => {
                error!("Got unexpected value for EnvType: {s}");
                std::process::exit(1);
            }
        }
    }
}

#[derive(Debug, Clone)]
pub struct Env {
    pub env_type: EnvType,

    pub server_port: u64,

    pub db_url: String,

    pub jwt_access_token_lifetime: u64,
    pub jwt_refresh_token_lifetime: u64,
    pub jwt_secret: String,

    pub markdown_storage_access_key_id: String,
    pub markdown_storage_bucket_name: String,
    pub markdown_storage_region: String,
    pub markdown_storage_secret_key: String,
    pub markdown_storage_service_endpont: String,
}

impl Env {
    pub fn init() -> Env {
        info!("⏳ Loading '.env' file");
        let _ = dotenv::dotenv(); // For local only, ignore error

        info!("⏳ Reading environment variables");
        let env = Env {
            env_type: EnvType::from_str(&read_env_var("ENV_TYPE")),

            server_port: read_env_var_as_u64("SERVER_PORT"),

            db_url: read_env_var("DATABASE_URL"),

            jwt_access_token_lifetime: read_env_var_as_u64("JWT_ACCESS_TOKEN_LIFETIME"),
            jwt_refresh_token_lifetime: read_env_var_as_u64("JWT_REFRESH_TOKEN_LIFETIME"),
            jwt_secret: read_env_var("JWT_SECRET"),

            markdown_storage_access_key_id: read_env_var("MARKDOWN_STORAGE_ACCESS_KEY_ID"),
            markdown_storage_bucket_name: read_env_var("MARKDOWN_STORAGE_BUCKET_NAME"),
            markdown_storage_region: read_env_var("MARKDOWN_STORAGE_REGION"),
            markdown_storage_secret_key: read_env_var("MARKDOWN_STORAGE_SECRET_KEY"),
            markdown_storage_service_endpont: read_env_var("MARKDOWN_STORAGE_SERVICE_ENDPOINT"),
        };

        info!("✅ Done reading environment variables");

        env
    }
}

fn read_env_var(key: &str) -> String {
    std::env::var(key).expect(&format!("❌ Failed to read env variable '{key}'"))
}

fn read_env_var_as_u64(key: &str) -> u64 {
    let s_value = read_env_var(key);
    s_value.parse().expect(&format!(
        "❌ Failed to parse env variable '{key}={s_value}' into u32"
    ))
}
