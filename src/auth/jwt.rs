use crate::env::Env;
use crate::error::ServerError;

use jwt_simple::claims::Claims;
use jwt_simple::prelude::Duration;
use jwt_simple::prelude::HS256Key;
use jwt_simple::prelude::MACLike;
use serde::Deserialize;
use serde::Serialize;
use tracing::info;

#[derive(Clone)]
pub struct JwtHandler {
    key: HS256Key,
    access_token_lifetime: u64,
    refresh_token_lifetime: u64,
}

#[derive(Copy, Clone, Default, Debug, Serialize, Deserialize)]
enum TokenType {
    #[default]
    Access,
    Refresh,
}

#[derive(Copy, Clone, Default, Debug, Serialize, Deserialize)]
struct JwtClaims {
    token_type: TokenType,
}

impl JwtHandler {
    pub fn init(env: &Env) -> JwtHandler {
        info!("⏳ Initializing JWT handler");
        JwtHandler {
            key: HS256Key::from_bytes(env.jwt_secret.as_bytes()),
            access_token_lifetime: env.jwt_access_token_lifetime,
            refresh_token_lifetime: env.jwt_refresh_token_lifetime,
        }
    }

    pub fn create_auth_token_for_user(&self, user_id: i64) -> Result<String, ServerError> {
        let claims = Claims::with_custom_claims(
            JwtClaims {
                token_type: TokenType::Access,
            },
            Duration::from_millis(self.access_token_lifetime),
        )
        .with_subject(user_id);
        self.key
            .authenticate(claims)
            .map_err(|err| ServerError::from(err))
    }

    pub fn create_refresh_token_for_user(&self, user_id: i64) -> Result<String, ServerError> {
        let claims = Claims::with_custom_claims(
            JwtClaims {
                token_type: TokenType::Refresh,
            },
            Duration::from_millis(self.refresh_token_lifetime),
        )
        .with_subject(user_id);
        self.key
            .authenticate(claims)
            .map_err(|err| ServerError::from(err))
    }

    pub fn validate_access_token(&self, access_token: &str) -> Result<(bool, i64), ServerError> {
        let claims = self
            .key
            .verify_token::<JwtClaims>(&access_token, None)
            .map_err(|err| ServerError::from(err))?;

        let user_id = claims
            .subject
            .unwrap()
            .parse()
            .map_err(|_| ServerError::InternalServerError("Unknown error".to_string()))?;

        match claims.custom.token_type {
            TokenType::Access => Ok((true, user_id)),
            TokenType::Refresh => Ok((false, user_id)),
        }
    }

    pub fn validate_refresh_token(
        &self,
        refresh_token: String,
    ) -> Result<(bool, i64), ServerError> {
        let claims = self
            .key
            .verify_token::<JwtClaims>(&refresh_token, None)
            .map_err(|err| ServerError::from(err))?;

        let user_id = claims
            .subject
            .unwrap()
            .parse()
            .map_err(|_| ServerError::InternalServerError("Unknown error".to_string()))?;

        match claims.custom.token_type {
            TokenType::Access => Ok((false, user_id)),
            TokenType::Refresh => Ok((true, user_id)),
        }
    }
}
