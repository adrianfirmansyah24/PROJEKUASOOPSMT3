CREATE TABLE barang (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    kategori VARCHAR(30) NOT NULL,
    nama VARCHAR(120) NOT NULL,
    deskripsi_publik VARCHAR(2000) NOT NULL,
    ciri_privat VARCHAR(2000) NOT NULL,
    CONSTRAINT chk_kategori CHECK (kategori IN ('ELEKTRONIK', 'KARTU_IDENTITAS', 'UMUM'))
);

CREATE TABLE laporan (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barang_id BIGINT NOT NULL,
    jenis VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    kampus VARCHAR(120) NOT NULL,
    area VARCHAR(120) NOT NULL,
    detail_lokasi VARCHAR(500) NOT NULL,
    waktu_kejadian DATETIME(6) NOT NULL,
    CONSTRAINT uk_laporan_barang UNIQUE (barang_id),
    CONSTRAINT fk_laporan_barang FOREIGN KEY (barang_id) REFERENCES barang(id),
    CONSTRAINT chk_status_awal CHECK (
        (jenis = 'KEHILANGAN' AND status = 'AKTIF') OR
        (jenis = 'PENEMUAN' AND status = 'MENUNGGU_PENITIPAN')
    ),
    INDEX idx_laporan_jenis (jenis)
);
