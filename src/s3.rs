use aws_config::Region;
use aws_sdk_s3::config::Credentials;
use aws_sdk_s3::Client;
use tracing::info;

use crate::env::Env;
use crate::env::EnvType;

#[derive(Clone)]
pub struct MarkdownFileStorage {
    client: Client,
    base_path: String,
    bucket: String,
}

impl MarkdownFileStorage {
    pub async fn init(env: &Env) -> MarkdownFileStorage {
        info!("⏳ Initializing MarkdownFileStorage");
        info!("⏳ Creating S3 client for MarkdownFileStorage");

        let credentials = Credentials::new(
            &env.markdown_storage_access_key_id,
            &env.markdown_storage_secret_key,
            None,
            None,
            "",
        );

        let sdk_config = aws_config::from_env()
            .credentials_provider(credentials)
            .region(Region::new(env.markdown_storage_region.to_string()))
            .endpoint_url(&env.markdown_storage_service_endpont)
            .load()
            .await;

        MarkdownFileStorage {
            client: Client::new(&sdk_config),
            base_path: match env.env_type {
                EnvType::PROD => "prod".to_string(),
                EnvType::QA => "qa".to_string(),
            },
            bucket: env.markdown_storage_bucket_name.to_string(),
        }
    }

    pub async fn upload(&self, file_path: &str, file_bytes: Vec<u8>) {
        let key = self.build_key(file_path);
        let content_length = file_bytes.len() as i64;

        let response = self
            .client
            .put_object()
            .bucket(&self.bucket)
            .key(key)
            .body(file_bytes.into())
            .content_type("file/text")
            .content_length(content_length)
            .send()
            .await;
        info!("{:#?}", response);
    }

    pub async fn download(self, file_path: &str) -> Vec<u8> {
        let key = self.build_key(file_path);

        let response = self
            .client
            .get_object()
            .bucket(&self.bucket)
            .key(key)
            .send()
            .await
            .unwrap();
        response.body.collect().await.unwrap().into_bytes().to_vec()
    }

    pub async fn delete(self, file_path: &str) {
        let key = self.build_key(file_path);

        let response = self
            .client
            .delete_object()
            .bucket(&self.bucket)
            .key(key)
            .send()
            .await;
        info!("{:#?}", response);
    }

    fn build_key(&self, file_path: &str) -> String {
        format!("{}/{}", self.base_path, file_path)
    }
}
