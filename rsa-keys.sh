#!/bin/bash

KEY_DIR="certs"
PRIV_KEY="${KEY_DIR}/private.pem"
PUB_KEY="${KEY_DIR}/public.pem"

mkdir -p "${KEY_DIR}"

openssl genrsa -out "${PRIV_KEY}" 2048

openssl rsa -in "${PRIV_KEY}" -pubout -out "${PUB_KEY}"

chmod 600 "${PRIV_KEY}"
chmod 644 "${PUB_KEY}"
ls -la "${KEY_DIR}"