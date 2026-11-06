# Tahap 2: autentikasi dan kepemilikan laporan

Tahap ini mengimplementasikan migrasi V2, akun pengguna, autentikasi Spring Security
dengan session cookie, serta hubungan setiap laporan dengan akun pembuatnya. Tahap 1
tetap dijelaskan di [tahap-1.md](tahap-1.md), sedangkan rancangan seluruh tabel target
ada di [erd.md](erd.md).

## Yang sudah tersedia

- Tabel `pengguna` dengan peran `PENGGUNA` atau `PETUGAS`.
- Registrasi publik selalu menghasilkan peran `PENGGUNA`.
- Password disimpan sebagai hash BCrypt dan tidak pernah dikirim dalam response.
- Login membuat sesi server dan browser menerima cookie `FOUNDLY_SESSION` yang
  `HttpOnly` dan `SameSite=Lax`.
- Perlindungan CSRF aktif untuk request yang mengubah data.
- CORS menerima credential dari origin frontend yang dikonfigurasi.
- Pembuatan laporan wajib login dan menyimpan `pelapor_id` dari sesi, bukan dari body.
- Daftar dan detail laporan publik tetap dapat dibaca tanpa login dan tidak menampilkan
  identitas pelapor atau ciri privat.
- Akun dapat dinonaktifkan tanpa menghapus laporan atau riwayatnya.

Session timeout saat ini 30 menit. `SESSION_COOKIE_SECURE=false` hanya sesuai untuk
HTTP lokal. Pada deployment HTTPS nilainya wajib `true`.

## Perubahan database V2

Migrasi `V2__buat_pengguna_dan_pemilik_laporan.sql` membuat tabel `pengguna`, lalu
menambahkan `pelapor_id`, `created_at`, `updated_at`, dan `version` pada `laporan`.
Foreign key memakai `RESTRICT` agar riwayat laporan tidak hilang ketika akun dihapus.

Database V1 lokal dapat berisi laporan tanpa akun. Migrasi tidak menghapus laporan
tersebut: jika data lama ada, Flyway membuat akun `legacy@foundly.local` yang nonaktif
dan menghubungkan laporan lama kepadanya. Akun migrasi tidak dapat digunakan login.
Setelah migrasi selesai, setiap laporan wajib memiliki pelapor.

## Alur session dan CSRF

1. Client memanggil `GET /api/auth/csrf` dan menyimpan cookie yang diberikan server.
2. Client mengirim nilai `token` dari response pada header `X-CSRF-TOKEN` untuk
   registrasi, login, logout, pembuatan laporan, dan request perubahan data lain.
3. Login yang berhasil menyimpan identitas akun pada session server. ID session dapat
   diganti saat login untuk mencegah session fixation.
4. Request berikutnya mengirim cookie yang sama; backend memperoleh ID pengguna dari
   session dan menerapkan hak akses.
5. Logout menghapus session dan cookie.

Cookie sesi bukan password dan bukan tempat menyimpan seluruh data pengguna. CSRF token
juga bukan token login; fungsinya membuktikan request perubahan berasal dari client yang
memiliki sesi yang sama.

## Endpoint

| Method | Path | Akses | Keterangan |
| --- | --- | --- | --- |
| GET | `/api/auth/csrf` | Publik | Membuat/membaca sesi dan mengembalikan token CSRF |
| POST | `/api/auth/register` | Publik + CSRF | Membuat akun `PENGGUNA`; tidak otomatis login |
| POST | `/api/auth/login` | Publik + CSRF | Memvalidasi email/password dan membuat sesi login |
| GET | `/api/auth/me` | Login | Mengembalikan akun pada sesi aktif |
| POST | `/api/auth/logout` | Login + CSRF | Mengakhiri sesi; HTTP 204 |
| GET | `/api/laporan` | Publik | Daftar/pencarian laporan aman |
| GET | `/api/laporan/{id}` | Publik | Detail publik laporan |
| POST | `/api/laporan` | Login + CSRF | Membuat laporan milik akun aktif |

Email registrasi dinormalisasi menjadi huruf kecil dan harus unique. Password registrasi
minimal 8 dan maksimal 64 karakter. Email atau password login yang salah menghasilkan
pesan HTTP 401 generik agar tidak membocorkan apakah sebuah email terdaftar.

## Mencoba di Postman

Postman biasanya menyimpan cookie otomatis untuk host yang sama.

1. Kirim `GET http://127.0.0.1:8081/api/auth/csrf`.
2. Salin nilai `token` dari JSON response ke header `X-CSRF-TOKEN`.
3. Registrasi dengan `POST /api/auth/register`, header tersebut, dan body:

```json
{
  "nama": "Pengguna Uji",
  "email": "pengguna@example.test",
  "password": "rahasia123"
}
```

4. Ambil token baru melalui `GET /api/auth/csrf`, kemudian login dengan
   `POST /api/auth/login` memakai header CSRF dan body:

```json
{
  "email": "pengguna@example.test",
  "password": "rahasia123"
}
```

5. Cek sesi melalui `GET /api/auth/me`.
6. Ambil token CSRF dari sesi login, lalu buat laporan dengan `POST /api/laporan` dan
   body [laporan-contoh.json](laporan-contoh.json). Jangan mengirim `pelapor_id`; backend
   mengambilnya dari akun yang login.
7. Untuk logout, kirim `POST /api/auth/logout` dengan token CSRF sesi tersebut.

Jika menerima HTTP 403 pada POST, pastikan cookie dari `GET /api/auth/csrf` masih ikut
terkirim dan nilai header `X-CSRF-TOKEN` berasal dari sesi yang sama. HTTP 401 berarti
endpoint membutuhkan login atau kredensial login salah.

## Pemetaan PBO tambahan

| Konsep | File/class | Pemakaian |
| --- | --- | --- |
| Encapsulation | `Pengguna` | Field private, normalisasi email dan kondisi aktif dijaga object |
| Class dan object | `PenggunaService` | Membuat object pengguna dari request yang valid |
| Enum | `PeranPengguna` | Membatasi peran domain menjadi `PENGGUNA`/`PETUGAS` |
| Interface | `UserDetailsService` melalui `PenggunaDetailsService` | Menghubungkan akun domain ke kontrak autentikasi Spring Security |
| Abstraction/layering | Controller, service, repository, DTO | HTTP, proses aplikasi, database, dan data response dipisahkan |
| Exception handling | Exception akun + `ApiExceptionHandler` | Duplicate email, akun nonaktif, dan kegagalan autentikasi dipetakan ke HTTP |

Framework membantu menjalankan keamanan, tetapi class domain dan service tetap menjadi
tempat aturan aplikasi. Tahap ini belum menambah klaim, penyerahan, saran kecocokan,
update laporan, atau endpoint khusus petugas.

## Hasil verifikasi

- Clean build dan tes integrasi: 22 tes lulus tanpa failure, error, atau skipped.
- Tes memakai MySQL Testcontainers terpisah dan mencakup registrasi, hash password,
  normalisasi/duplikasi email, CSRF, login/session/logout, otorisasi pembuatan laporan,
  kepemilikan laporan, seluruh subclass barang, privasi, pencarian, validasi, dan 404.
- Migrasi V2 juga diterapkan pada volume pengembangan V1. Flyway mencapai versi 2,
  laporan lama ID 1 tetap ada, dan terhubung ke akun migrasi nonaktif.
- Backend aktual berhasil startup dengan Java 21 dan endpoint health menghasilkan 200.

## Langkah berikutnya

Lengkapi perubahan/status laporan dan lampiran pada V3, kemudian implementasikan tabel
serta API saran kecocokan pada V4. Setelah itu lanjutkan klaim/bukti, penyerahan, dan
frontend React. Hak akses method petugas ditambahkan bersama endpoint petugas pertama.
