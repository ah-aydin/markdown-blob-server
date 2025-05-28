#!/bin/bash
cargo sqlx prepare
docker build -t markdown-blob-server:latest .
