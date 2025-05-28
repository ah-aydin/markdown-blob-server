# Stage 1: Build
FROM rust:1.87-slim-bookworm AS builder

WORKDIR /app

RUN rm -rf /var/lib/apt/lists/*

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY .sqlx ./.sqlx
RUN cargo fetch
RUN cargo build --release

# Stage 2: Run
FROM debian:bookworm-slim AS runner

WORKDIR /app

RUN rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/release/markdown-blob-server ./markdown-blob-server

EXPOSE 8080

ENTRYPOINT ["./markdown-blob-server"]
