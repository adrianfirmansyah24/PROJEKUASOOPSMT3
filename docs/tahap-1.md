# Tahap 1: laporan tersimpan di MySQL

Dokumen ini menjelaskan cakupan historis implementasi V1. V2 autentikasi dan
kepemilikan laporan sudah tersedia dan dijelaskan di
[tahap-2-autentikasi.md](tahap-2-autentikasi.md). Rancangan database target
yang telah difinalisasi tersedia di [ERD Foundly](erd.md). Tabel pada ERD target yang
belum memiliki migrasi belum tersedia di MySQL.

## Batas tahap

Pada V1 cakupannya membuat laporan, daftar/pencarian, dan detail. Saat itu belum ada
update/delete, upload foto, registrasi/login, relasi pelapor, klaim, atau penyerahan.
Registrasi/login, CSRF, dan relasi pelapor kemudian ditambahkan oleh V2; batas lain
masih berlaku. API tetap untuk pengembangan lokal dan server terikat ke 127.0.0.1.
Status lanjutan ditambahkan bersama alurnya.
Lokasi sekarang berupa teks kampus/area/detail; daftar lokasi UPNVJ terkontrol
belum diterapkan. Jangan memakai data identitas asli untuk percobaan.

## Struktur data awal

| Tabel | Kolom inti | Hubungan |
| --- | --- | --- |
| barang | id, kategori, nama, deskripsi_publik, ciri_privat | Satu deskripsi barang untuk satu laporan |
| laporan | id, barang_id, jenis, status, kampus, area, detail_lokasi, waktu_kejadian | barang_id adalah foreign key unik ke barang.id |

Relasi awal adalah satu laporan ke satu deskripsi barang. Dua laporan yang diduga
cocok tetap memiliki deskripsi masing-masing. Kepemilikan belum ditentukan otomatis.
Tabel pengguna dan relasi pelapor sudah ditambahkan oleh V2. Lampiran, saran kecocokan,
klaim, bukti klaim, dan penyerahan masih berupa rancangan ERD target dan akan ditambahkan
bertahap melalui migrasi berikutnya.

Subclass barang disimpan menggunakan SINGLE_TABLE: kolom kategori menentukan apakah
baris dimuat JPA sebagai BarangElektronik, KartuIdentitas, atau BarangUmum.
Tidak ada tabel terpisah untuk setiap subclass karena atributnya masih sama;
perilaku panduan buktinya berbeda.

`V1__buat_barang_dan_laporan.sql` adalah migrasi Flyway pertama. Flyway mencatat
migrasi yang sudah berjalan dalam flyway_schema_history. Hibernate menggunakan
`ddl-auto=validate`: memeriksa kesesuaian entity, tanpa mengubah tabel otomatis.
Setelah V1 digunakan, perubahan skema berikutnya dibuat sebagai V2, V3, dan seterusnya.

## Alur kode

1. LaporanController menerima JSON dan memvalidasi field request.
2. LaporanService membuat subclass Barang sesuai kategori, lalu object Laporan.
3. Constructor Laporan menetapkan status awal: KEHILANGAN menjadi AKTIF;
   PENEMUAN menjadi MENUNGGU_PENITIPAN. Status tidak diterima dari request.
4. Repository menyimpan laporan dan barang dalam satu transaksi.
5. LaporanResponse mengambil data publik dan memanggil getPanduanBuktiKlaim()
   melalui referensi Barang. Implementasi subclass menentukan hasilnya.
6. Spring mengirim JSON. Ciri privat tetap tersimpan di database dan tidak masuk response.

Pencarian hanya menggunakan nama/deskripsi publik; ciri privat tidak dicari.
Ciri privat saat ini disimpan sebagai teks di database lokal dan belum memiliki
endpoint pembacaan petugas. Field publik tetap harus diisi tanpa bukti sensitif.

## Pemetaan materi PBO yang sudah dipakai

| Konsep | File/method | Pemakaian di aplikasi |
| --- | --- | --- |
| Class dan object | LaporanService.buat | Membuat object Barang dan Laporan dari request |
| Encapsulation | Barang dan Laporan | Atribut private, tanpa setter umum; constructor menetapkan kondisi awal |
| Abstraction | Barang | Abstract class dengan kontrak kategori dan panduan bukti |
| Inheritance | BarangElektronik, KartuIdentitas, BarangUmum | extends Barang dan super pada constructor |
| Polymorphism/overriding | LaporanResponse.dari | getPanduanBuktiKlaim dipanggil melalui Barang; hasil berbeda per subclass |
| Interface | LaporanRepository extends JpaRepository | Kontrak akses data yang implementasinya disediakan Spring Data; interface strategi buatan sendiri menyusul |
| Collection/generics | DaftarLaporanResponse | List<LaporanResponse> untuk hasil pencarian |
| Custom exception | LaporanTidakDitemukanException | Service melempar exception, handler mengubahnya menjadi HTTP 404 |

DemoPBO.java, interface strategi pencocokan buatan sendiri, overloading, dan contoh
exception alur klaim belum dikerjakan. Pemetaan ini belum menjamin seluruh rubrik
praktikum tercakup.

## API lokal

Base URL lokal: http://127.0.0.1:8081 (PORT di .env).

| Method | Path | Hasil |
| --- | --- | --- |
| GET | /api/health | HTTP 200, server berjalan; bukan pemeriksaan database langsung |
| POST | /api/laporan | HTTP 201, JSON laporan dan header Location |
| GET | /api/laporan | HTTP 200, daftar paginasi |
| GET | /api/laporan/{id} | HTTP 200, detail publik; 404 jika tidak ada |

Pada versi terkini, POST memerlukan session login dan header CSRF seperti dijelaskan di
`tahap-2-autentikasi.md`. Request menggunakan Content-Type: application/json dan body
dari laporan-contoh.json.
Jenis: KEHILANGAN atau PENEMUAN. Kategori: ELEKTRONIK, KARTU_IDENTITAS, UMUM.
Waktu kejadian memakai waktu lokal Jakarta format yyyy-MM-ddTHH:mm:ss dan tidak
boleh di masa depan. Field wajib tidak boleh kosong; panjang field dibatasi.

Parameter daftar: jenis (opsional), kataKunci (opsional, nama/deskripsi publik),
page (mulai 0, default 0), size (1–100, default 20). Urutan ID terbaru dahulu.
Filter kategori, lokasi, dan waktu menyusul.

Response daftar: data, page, size, totalElements. Input tidak valid menghasilkan
HTTP 400 dengan ProblemDetail. Entity tidak dikirim langsung sebagai JSON.

## Langkah berikutnya

Akun, session, hak akses pembuatan, dan pelapor laporan sudah diselesaikan di V2.
Berikutnya lengkapi perubahan laporan sesuai hak pemilik dan lampiran, kemudian saran
kecocokan, penitipan/klaim/penyerahan beserta transisi statusnya. Setelah API diuji,
hubungkan React. Rancangan perlu tetap dicocokkan dengan rubrik dosen.
