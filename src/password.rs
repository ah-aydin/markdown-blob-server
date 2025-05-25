use argon2::password_hash::rand_core::OsRng;
use argon2::password_hash::PasswordHash;
use argon2::password_hash::PasswordHasher;
use argon2::password_hash::PasswordVerifier;
use argon2::password_hash::SaltString;
use argon2::Argon2;

use crate::error::ServerError;

pub fn hash_password(password: &str) -> Result<String, ServerError> {
    let salt = SaltString::generate(&mut OsRng);
    let argon2 = Argon2::default();

    argon2
        .hash_password(password.as_bytes(), &salt)
        .map(|res| res.to_string())
        .map_err(|err| ServerError::from(err))
}

pub fn verify_password(password: &str, password_hash: &str) -> Result<bool, ServerError> {
    let parsed_hash = PasswordHash::new(&password_hash).map_err(|err| ServerError::from(err))?;
    Ok(Argon2::default()
        .verify_password(password.as_bytes(), &parsed_hash)
        .is_ok())
}
