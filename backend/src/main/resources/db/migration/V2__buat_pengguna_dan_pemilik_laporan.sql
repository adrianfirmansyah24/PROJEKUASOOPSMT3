CREATE TABLE pengguna (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    peran VARCHAR(20) NOT NULL,
    aktif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_pengguna_email UNIQUE (email),
    CONSTRAINT chk_pengguna_peran CHECK (peran IN ('PENGGUNA', 'PETUGAS'))
);

ALTER TABLE laporan
    ADD COLUMN pelapor_id BIGINT NULL,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- V1 dapat memiliki laporan contoh tanpa akun. Akun nonaktif ini hanya dibuat jika
-- data lama benar-benar ada, sehingga migrasi tetap aman tanpa menghapus data lokal.
INSERT INTO pengguna (nama, email, password_hash, peran, aktif)
SELECT 'Data Migrasi Lokal', 'legacy@foundly.local', 'LOGIN_DISABLED', 'PENGGUNA', FALSE
FROM DUAL
WHERE EXISTS (SELECT 1 FROM laporan);

UPDATE laporan
SET pelapor_id = (SELECT id FROM pengguna WHERE email = 'legacy@foundly.local')
WHERE pelapor_id IS NULL;

ALTER TABLE laporan
    MODIFY pelapor_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_laporan_pelapor
        FOREIGN KEY (pelapor_id) REFERENCES pengguna(id) ON DELETE RESTRICT,
    ADD INDEX idx_laporan_pelapor (pelapor_id);
