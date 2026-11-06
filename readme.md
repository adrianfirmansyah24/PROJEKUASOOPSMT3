# Foundly

Proyek UAS PBO semester 3: web barang hilang dan temuan di lingkungan UPN “Veteran”
Jakarta dengan verifikasi kepemilikan. Backend Java 21 + Spring Boot 4.1.2,
database MySQL 8.4.13 di Docker, dan rencana frontend React + CSS.

## Tahap saat ini

Backend menyediakan health, registrasi, login/logout berbasis session cookie, informasi
akun aktif, serta pembuatan dan pembacaan laporan. Setiap laporan memiliki pelapor,
tetapi identitasnya tidak ditampilkan melalui response publik. Data disimpan melalui
JPA dan skema dikelola Flyway. Klaim, penyerahan, saran kecocokan, update/delete
laporan, foto, dan frontend belum tersedia. Ini tahap pengembangan lokal.
Rancangan database target ada di [docs/erd.md](docs/erd.md). Implementasi V1, alur kode,
batas tahap, dan pemetaan PBO ada di [docs/tahap-1.md](docs/tahap-1.md).
Implementasi V2 dan cara memakai session/CSRF ada di
[docs/tahap-2-autentikasi.md](docs/tahap-2-autentikasi.md).
Konsep pengguna, petugas, anonimitas, dan bantuan komunitas dibahas di
[docs/konsep-peran-dan-interaksi.md](docs/konsep-peran-dan-interaksi.md).
Riwayat pekerjaan ada di [AGENTS.md](AGENTS.md).

## 1. Jalankan database

Prasyarat: Docker Desktop aktif, Docker Compose tersedia, dan JDK 21.
Dari root proyek, jika belum punya .env:

```sh
cp .env.example .env
```

Isi DB_PASSWORD dan MYSQL_ROOT_PASSWORD di .env dengan password lokal masing-masing.
Pada komputer pengembangan ini, .env sudah dibuat otomatis dengan password acak.
Jangan menimpa .env yang sudah digunakan volume MySQL. Nilai inisialisasi user/password
MySQL hanya diterapkan saat volume pertama dibuat. .env tidak masuk Git.

```sh
docker compose up -d --wait
docker compose ps
```

MySQL tersedia di 127.0.0.1:3307, database foundly, user foundly. Password aplikasi
adalah DB_PASSWORD dari .env. Gunakan informasi yang sama jika ingin membuka database
lewat DBeaver/MySQL Workbench. Spring Boot memakai user aplikasi, bukan root.
Port 3306 dan 8080 sudah digunakan proyek lain pada komputer ini.

```sh
docker compose stop
# Menyalakan kembali:
docker compose up -d --wait
```

Data tersimpan di volume foundly_mysql_data dan tetap ada setelah stop/start maupun
container dibuat ulang. `docker compose down` juga mempertahankan volume;
`docker compose down -v` menghapus volume beserta seluruh datanya.

## 2. Jalankan backend

```sh
cd backend
./mvnw spring-boot:run
```

Maven Wrapper mengunduh Maven/dependensi saat pertama dipakai. Tidak perlu Maven global.
Windows: gunakan mvnw.cmd. Spring membaca .env root saat dijalankan dari root/backend.
PORT=8081 di .env memilih port lokal; tanpa konfigurasi PORT default-nya 8080.
Jika port 8081 sedang digunakan, pilih PORT lain di .env.

Jika macOS gagal menemukan JDK meskipun java -version berhasil, atur sesuai lokasi JDK.
Untuk Homebrew di komputer pengembangan ini:

```sh
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
```

Buka http://127.0.0.1:8081/api/health. Response: application Foundly, status UP,
message Backend Foundly berjalan. Endpoint ini memeriksa HTTP server saja.
Backend memeriksa koneksi, migrasi, dan kesesuaian skema database saat startup.
Hentikan backend dengan Ctrl+C. Database berjalan terpisah di Docker.

## 3. Coba akun dan laporan

Request `GET` laporan dapat dibaca tanpa login. Untuk registrasi, login, logout, atau
membuat laporan, ambil token melalui `GET /api/auth/csrf`, pertahankan cookie, dan kirim
nilai token pada header `X-CSRF-TOKEN`. Postman biasanya menyimpan cookie secara
otomatis. Urutan request dan contoh body akun dijelaskan lengkap di
[dokumentasi tahap 2](docs/tahap-2-autentikasi.md).

Setelah login, kirim `POST http://127.0.0.1:8081/api/laporan`, Body → raw → JSON,
header CSRF dari sesi yang sama, lalu gunakan [contoh body](docs/laporan-contoh.json).
Daftar publik dapat dicoba langsung dari root proyek:

```sh
curl 'http://127.0.0.1:8081/api/laporan?jenis=PENEMUAN&kataKunci=headphone&page=0&size=20'
```

POST laporan menghasilkan HTTP 201. Gunakan id pada response untuk
`GET /api/laporan/{id}`. Ciri privat dan identitas pelapor tidak dikirim dalam response
publik. Jangan memasukkan bukti sensitif ke nama atau deskripsi publik. Semua endpoint
masih untuk pengembangan lokal.

## Pengujian dan build

Docker harus aktif. Dari backend:

```sh
./mvnw clean verify
```

Testcontainers membuat MySQL sementara pada port acak, terpisah dari volume Foundly,
lalu menghapusnya setelah tes. Saat ini terdapat 22 tes yang mencakup migrasi,
registrasi dan BCrypt, CSRF, session login/logout, otorisasi, kepemilikan laporan,
polymorphism setelah dimuat dari MySQL, privasi response/pencarian, filter/paginasi,
input tidak valid, dan HTTP 404. Jangan mengatur tes agar menghapus database
pengembangan. Build menghasilkan backend/target/foundly-0.0.1-SNAPSHOT.jar.

Jika Testcontainers di macOS tidak menemukan Docker Desktop:

```sh
export DOCKER_HOST="unix://$HOME/.docker/run/docker.sock"
```

## Struktur

```text
compose.yaml                  MySQL lokal dan volume permanen
.env.example                  Contoh konfigurasi tanpa password asli
docs/                         Rancangan tahap dan contoh request
backend/
  pom.xml                     Dependensi/build
  mvnw / mvnw.cmd             Maven Wrapper
  src/main/java/id/ac/upnvj/foundly/
    FoundlyApplication.java   Titik masuk
    controller/               Request HTTP
    config/                   Spring Security, session, CSRF, dan CORS
    service/                  Proses aplikasi dan transaksi
    model/                    Object, pewarisan, aturan domain
    repository/               Akses database
    dto/                      Data request/response
    exception/                Kesalahan dan response API
    security/                 Principal dan penghubung akun ke Spring Security
  src/main/resources/
    application.properties    Konfigurasi
    db/migration/             Riwayat skema SQL
  src/test/java/              Tes integrasi MySQL
```
