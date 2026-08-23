CREATE TABLE IF NOT EXISTS storages (
    id           BIGSERIAL PRIMARY KEY,
    storage_type VARCHAR(100) NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    path         VARCHAR(100) NOT NULL
);

INSERT INTO storages (storage_type, bucket, path)
SELECT 'STAGING', 'staging-storage', '/files'
WHERE NOT EXISTS (
    SELECT 1 FROM storages WHERE storage_type = 'STAGING'
);

INSERT INTO storages (storage_type, bucket, path)
SELECT 'PERMANENT', 'permanent-storage', '/files'
WHERE NOT EXISTS (
    SELECT 1 FROM storages WHERE storage_type = 'PERMANENT'
);
