# Base image for building
FROM lukemathwalker/cargo-chef:latest-rust-1 AS chef
WORKDIR /app

# Stage 1: Plan
FROM chef AS planner

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY .sqlx ./.sqlx

RUN cargo chef prepare --recipe-path recipe.json

# Stage 2: Build
FROM chef AS builder 

COPY --from=planner /app/recipe.json recipe.json
RUN cargo chef cook --release --recipe-path recipe.json

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY .sqlx ./.sqlx

RUN cargo build --release

# Stage 3: Run
FROM debian:bookworm-slim AS runtime

WORKDIR /app

COPY --from=builder /app/target/release/markdown-blob-server ./markdown-blob-server

EXPOSE 8080

ENTRYPOINT ["./markdown-blob-server"]
