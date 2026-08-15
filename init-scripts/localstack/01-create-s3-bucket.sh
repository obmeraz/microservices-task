#!/bin/bash
set -euo pipefail

STAGING_BUCKET="staging-storage"
PERMANENT_BUCKET="permanent-storage"

awslocal s3 mb "s3://${STAGING_BUCKET}" 2>/dev/null || true
awslocal s3 mb "s3://${PERMANENT_BUCKET}" 2>/dev/null || true

echo "S3 buckets '${STAGING_BUCKET}' and '${PERMANENT_BUCKET}' are ready"
