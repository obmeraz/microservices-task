#!/bin/bash
set -euo pipefail

BUCKET_NAME="resource-service-mp3"

awslocal s3 mb "s3://${BUCKET_NAME}" 2>/dev/null || true

echo "S3 bucket '${BUCKET_NAME}' is ready"