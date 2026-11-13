# proyekOOPSMT3 — Foundly

Catatan keputusan proyek, diperbarui 13 November 2026. Dokumen ini menjadi konteks untuk melanjutkan pekerjaan di Codex. Fondasi backend serta tahap awal laporan dan konfigurasi MySQL Docker sudah dibuat. Hasil verifikasi terbaru tercatat pada riwayat perubahan.

## Konteks dan tujuan

Proyek akhir UAS Pemrograman Berorientasi Objek menggunakan Java. Aplikasi harus berfungsi dan menunjukkan penerapan materi PBO secara eksplisit, mudah dibaca, serta mudah dijelaskan seperti tugas praktikum. Pengguna belum pernah menggunakan Spring Boot, sehingga implementasi dan penjelasan perlu bertahap.

Instruksi konteks awal pengguna: pekerjaan berada dalam proyek yang lebih luas bernama proyekOOPSMT3.

## Keputusan yang telah disepakati

- Nama aplikasi: Foundly.
- Produk: aplikasi web barang hilang dan barang temuan dengan verifikasi kepemilikan.
- Lingkup awal: lingkungan UPN “Veteran” Jakarta.
- Frontend: React dan CSS.
- Backend: Java dengan Spring Boot, menyediakan REST API.
- Java 21 dikonfirmasi dan disetujui langsung oleh pengguna pada 6 November 2026.
- Database: MySQL.
- Pengguna menyetujui MySQL lokal lewat Docker dan mengizinkan menjalankan Docker serta membangun backend/database bertahap. Gunakan container/volume khusus Foundly; proyek Docker lain tidak diubah.
- Akses database yang direncanakan: Spring Data JPA.
- API dapat diuji dengan Postman sebelum dihubungkan ke frontend.
- Autentikasi menggunakan Spring Security session cookie, BCrypt, CSRF protection, dan CORS ber-credential untuk React. Implementasi V2 beserta panduan uji ada di `docs/tahap-2-autentikasi.md`.
- Pengguna meminta menyimpan konteks ini dahulu; jangan menganggap permintaan menyimpan sebagai perintah membangun seluruh aplikasi sekarang.

## Lokasi dan radius

- Versi awal menggunakan lokasi bertingkat: kampus, area/gedung, lalu detail lokasi.
- GPS, peta, dan pencarian berbasis radius belum termasuk fitur inti; dapat dipertimbangkan setelah fitur utama selesai.
- Lingkup kampus membatasi lokasi barang yang dilaporkan, bukan lokasi fisik pengguna saat mengakses aplikasi. Pengguna tetap dapat mengakses dari rumah.
- Jika radius ditambahkan, gunakan titik perkiraan kehilangan/penemuan sebagai pusat pencarian yang dapat dipilih pengguna, bukan selalu posisi perangkat saat ini.
- Lokasi membantu pencarian, tetapi tidak membuktikan kepemilikan. Pencarian yang lebih luas tetap perlu dimungkinkan karena barang dapat berpindah.

## Alur produk awal

1. Pengguna membuat laporan kehilangan atau penemuan.
2. Pengguna mencari laporan berdasarkan kategori, lokasi, waktu, dan ciri barang.
3. Aplikasi dapat menyarankan kecocokan berbasis aturan sederhana; AI tidak diperlukan untuk versi awal.
4. Calon pemilik mengajukan klaim dan bukti pendukung.
5. Petugas memeriksa bukti dan memutuskan klaim.
6. Penyerahan barang dicatat, lalu status dan riwayat diperbarui.

Peran target: `PENGGUNA` dan `PETUGAS`; petugas menangani fungsi administrasi versi awal. ERD target, relasi, privasi, dan aturan status difinalisasi di `docs/erd.md`. Mekanisme autentikasi sudah diputuskan dan diterapkan. Penyimpanan file fisik, daftar lokasi terkontrol, serta unit penitipan belum diputuskan.

Pisahkan informasi publik dari ciri/bukti privat untuk verifikasi. Jangan menampilkan bukti sensitif pada API daftar laporan publik. Saran kecocokan atau kecocokan satu ciri tidak boleh otomatis menetapkan kepemilikan.

## Kebutuhan PBO

Empat pilar wajib tampak jelas dan benar-benar digunakan dalam alur aplikasi:

| Materi | Contoh rancangan awal |
| --- | --- |
| Class dan object | Pengguna, LaporanKehilangan, BarangTemuan, Klaim |
| Encapsulation | Atribut private; perubahan status dan data penting melalui method yang memvalidasi aturan |
| Inheritance | BarangElektronik dan KartuIdentitas mewarisi BarangTemuan |
| Abstraction | Abstract class BarangTemuan dengan kontrak perilaku yang relevan |
| Polymorphism dan overriding | getPanduanBuktiKlaim() menghasilkan panduan berbeda sesuai subclass |
| Interface dan implements | StrategiPencocokan diterapkan oleh strategi pencocokan elektronik dan kartu |
| Overloading | Pencarian berdasarkan kata kunci, atau kata kunci sekaligus lokasi |
| Constructor, this, super | Inisialisasi object dan pemanggilan constructor induk |
| Collection dan generics | List<BarangTemuan> dan koleksi laporan/klaim |
| Exception handling | try-catch, throw, throws; finally digunakan pada kebutuhan yang relevan |
| Custom exception | BuktiKlaimTidakLengkapException dan BarangSudahDiserahkanException |

Nama class di atas merupakan contoh desain, bukan skema final yang wajib dipaksakan. Gunakan pewarisan ketika ada hubungan dan perilaku yang masuk akal; jangan membuat subclass yang hanya berbeda nama untuk memenuhi checklist.

Sediakan rencana DemoPBO.java yang dapat dijalankan dari terminal memakai class domain yang sama dengan backend. Tujuannya menunjukkan constructor, inheritance, polymorphism, interface, dan exception tanpa perlu membuka React.

Setiap konsep perlu dapat dipetakan ke file, method, dan skenario demonstrasi. Hindari class contoh yang tidak pernah dipakai pada aplikasi. Pemakaian framework saja tidak cukup sebagai bukti penguasaan seluruh materi PBO.

## Materi yang sudah diketahui

- Empat pilar OOP disebutkan langsung oleh pengguna.
- Pengguna menyebut materi sebelumnya sebagai implement/implements; interface perlu tercakup.
- MODUL PRAKTIKUM 11.pdf telah dibaca: Exception Handling dan Custom Exception, termasuk try-catch-finally, beberapa catch, checked/unchecked exception, throw dan throws.
- Modul dan rubrik lain belum tersedia. Jangan menjamin seluruh materi satu semester sudah tercakup sebelum mencocokkannya dengan silabus/modul lengkap.
- Contoh materi tambahan dalam tabel merupakan rancangan cakupan, bukan klaim bahwa semua topik tersebut sudah diajarkan.

## Arah implementasi saat pengguna meminta lanjut

- Pisahkan controller, service, model/domain, repository, DTO, dan exception sesuai kebutuhan.
- Controller menangani request/response; service menjalankan proses aplikasi; domain menyimpan perilaku dan aturan; repository mengakses data.
- Mulai dari API GET sederhana, koneksi MySQL dan CRUD, lalu alur klaim, pengujian API, dan integrasi React.
- Pastikan frontend benar-benar memanggil backend Java dan data tersimpan di MySQL.
- Fondasi menggunakan JDK 21, Spring Boot 4.1.2, dan Maven Wrapper. Dependensi awal: Spring Web MVC, Validation, dan starter pengujian terkait.
- Hindari penambahan AI, integrasi peta, aplikasi mobile, atau fitur besar lain tanpa kebutuhan yang disepakati.
- Jelaskan kode dengan bahasa Indonesia yang sederhana dan konkret agar pengguna mampu mendemokan dan mempertanggungjawabkannya saat UAS.

## Langkah berikutnya yang belum dikerjakan

V1 laporan dan V2 akun/session/kepemilikan laporan sudah diimplementasikan serta terverifikasi. Berikutnya V3 untuk penyempurnaan laporan dan lampiran, V4 untuk saran kecocokan, lalu klaim/bukti dan penyerahan. Endpoint update laporan, fungsi petugas, frontend, dan deployment belum dibuat. Dokumentasi implementasi ada di `docs/tahap-1.md` dan `docs/tahap-2-autentikasi.md`.

## Cara kerja yang diminta pengguna

- Setiap membuat atau mengubah sesuatu dalam proyek, perbarui `AGENTS.md` ini pada pekerjaan yang sama.
- Catat apa yang berubah, alasan keputusan, hasil verifikasi, keterbatasan, dan langkah berikutnya. Jangan menulis rencana seolah sudah selesai.
- Gunakan `AGENTS.md` di root proyek ini sebagai catatan aktif. Dokumen awal di Downloads merupakan sumber konteks historis, bukan lokasi kerja proyek.
- Implementasi dilakukan bertahap dan dijelaskan dalam bahasa Indonesia agar pengguna memahami kode.

## Rancangan hasil pembahasan 6 November 2026

Pengguna menilai rancangan sudah cukup bagus dan meminta mulai fondasi Spring Boot. Rancangan ini menjadi arah kerja; detail berikut masih dapat disempurnakan sebelum fitur bisnis dibuat.

- Fitur utama: registrasi/login, laporan kehilangan, laporan penemuan, pencarian/filter, pengajuan klaim, pemeriksaan klaim, pencatatan penyerahan, dashboard dan riwayat.
- Dua peran akun: pengguna dan petugas/admin. Penemu, pelapor, dan calon pemilik merupakan posisi dalam proses, bukan peran akun terpisah.
- Usulan operasional: barang dititipkan kepada petugas sebelum klaim dapat disetujui. Unit/tempat penitipan belum ditetapkan.
- Alur klaim: laporan temuan → penitipan → pengajuan bukti → pemeriksaan petugas → persetujuan/penolakan → penyerahan dan penutupan laporan.
- Calon pemilik tidak harus mempunyai laporan kehilangan untuk mengklaim barang temuan.
- Satu barang hanya boleh mempunyai satu klaim yang disetujui; persetujuan belum berarti barang sudah diserahkan.
- Usulan status temuan: menunggu penitipan, tersedia, menunggu penyerahan, selesai. Status klaim: diajukan, dalam pemeriksaan, disetujui, ditolak. Status kehilangan: aktif, selesai, dibatalkan.
- Rancangan class inti: `Pengguna`, abstract `Barang`, `BarangElektronik`, `KartuIdentitas`, `BarangUmum`, `Laporan`, `Klaim`, `Penyerahan`, dan `KlaimService`.
- `Barang` menyediakan kontrak `getPanduanBuktiKlaim()` yang di-override subclass. Nama ini memperbarui contoh awal `BarangTemuan`; belum ada implementasi domain.
- `Laporan` memiliki jenis kehilangan/penemuan dan merujuk deskripsi barang milik laporan tersebut. Deskripsi dari dua laporan tidak otomatis dianggap benda fisik yang sama.
- Status dienkapsulasi melalui method yang memvalidasi aturan; service mengatur proses lintas objek. Strategi pencocokan berbasis interface ditambahkan saat fitur kecocokan dikerjakan.
- ERD target sudah dibuat di `docs/erd.md`. Diagram class konseptual masih berada di percakapan dan belum memiliki file final tersendiri.

## Riwayat perubahan

### 13 November 2026 — Otomatisasi test backend di GitHub Actions

- Pengguna menanyakan indikator test berwarna kuning pada GitHub dan meminta test langsung dijalankan otomatis.
- Pemeriksaan GitHub pada commit `ea912bf` menemukan 0 workflow run, 0 check run, dan 0 commit status. Test 22/22 sebelumnya hanya dijalankan lokal, sehingga GitHub belum mempunyai CI yang dapat menghasilkan status hijau atau merah.
- Menambahkan `.github/workflows/backend-test.yml` dengan trigger setiap push/PR ke `main`, serta `workflow_dispatch` untuk eksekusi manual. Semua commit memperoleh status test yang jelas, termasuk perubahan dokumentasi.
- Workflow menggunakan runner Ubuntu, Eclipse Temurin Java 21, cache Maven, dan menjalankan `./mvnw --batch-mode --no-transfer-progress clean verify` dari folder `backend`.
- Tes integrasi tetap menggunakan MySQL 8.4.13 melalui Testcontainers dan Docker bawaan runner GitHub; tidak memakai `.env` atau database pengembangan lokal.
- Izin workflow dibatasi ke `contents: read`, timeout 20 menit, dan run lama pada ref yang sama dibatalkan bila ada run baru.
- Perubahan ini harus di-commit dan di-push ke `main`; hasil GitHub Actions kemudian dipantau sampai selesai sebelum pekerjaan dinyatakan berhasil.

### 6 November 2026 — V2 autentikasi session dan kepemilikan laporan

- Pengguna menyetujui konsep peran/interaksi dan meminta melanjutkan database serta backend. Implementasi dibatasi pada tahap V2 agar perubahan dapat dipahami dan diuji sebelum fitur bisnis berikutnya.
- Menambahkan Spring Security dan dukungan tes keamanan. Autentikasi memakai session server, cookie `FOUNDLY_SESSION` HttpOnly/SameSite=Lax, timeout 30 menit, session ID rotation ketika login, CSRF aktif, dan CORS ber-credential untuk origin React yang dikonfigurasi. Cookie `Secure` dapat diaktifkan lewat environment saat HTTPS.
- Menambahkan entity/repository/service akun: `Pengguna`, `PeranPengguna`, `PenggunaRepository`, `PenggunaService`, principal dan `UserDetailsService`. Registrasi publik selalu membuat `PENGGUNA`, email dinormalisasi/unique, dan password di-hash BCrypt. Password/hash tidak masuk DTO response.
- Menambahkan endpoint `GET /api/auth/csrf`, `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, dan `POST /api/auth/logout`. Response 401/403 berbentuk JSON problem dan pesan login salah dibuat generik. Registrasi mengembalikan 201 tanpa header `Location` palsu karena belum ada endpoint publik untuk membaca pengguna berdasarkan ID.
- Menambahkan migrasi Flyway V2 untuk tabel `pengguna`, kepemilikan laporan melalui `pelapor_id`, timestamp, optimistic `version`, index, dan foreign key `RESTRICT`. Laporan V1 lama tidak dihapus; migrasi membuat akun `legacy@foundly.local` nonaktif hanya jika diperlukan lalu menghubungkan data lama kepadanya.
- Pembuatan `POST /api/laporan` sekarang wajib login dan mengambil ID pelapor dari session, bukan body request. GET daftar/detail tetap publik dan tidak membocorkan identitas pelapor maupun ciri privat.
- Memperluas tes integrasi menjadi 22 skenario: registrasi, normalisasi/duplikasi email, BCrypt/tidak bocornya hash, CSRF, login/session/logout, kredensial salah, otorisasi pembuatan laporan, kepemilikan, seluruh subclass barang, privasi, pencarian, validasi, dan 404. Hasil clean build: 22 lulus, 0 failure, 0 error, 0 skipped.
- Menerapkan V2 pada volume MySQL pengembangan yang sebelumnya berada di V1. Flyway berhasil mencapai versi 2; akun legacy nonaktif terbentuk; laporan contoh ID 1 berjenis `PENEMUAN` tetap ada dan terhubung ke akun tersebut. Backend Java 21 berhasil startup dan `GET /api/health` menghasilkan HTTP 200.
- Pengguna mengonfirmasi bantuan komunitas sebagai bagian arah produk. ERD target diperluas dari tujuh menjadi delapan tabel dengan `saran_kecocokan`; saran menghubungkan laporan kehilangan/temuan dan bersumber dari pengguna atau sistem, tetapi bukan klaim atau bukti kepemilikan. Tabel/API saran belum diimplementasikan dan direncanakan pada V4.
- Menambahkan `docs/tahap-2-autentikasi.md`, memperbarui ERD, konsep interaksi, README, dokumentasi V1, contoh environment, dan catatan aktif ini. Langkah berikutnya adalah V3 penyempurnaan laporan/lampiran, bukan langsung mengerjakan seluruh fitur sekaligus.
- Docker Desktop dan MySQL hanya dinyalakan selama verifikasi. Setelah dokumentasi dan pemeriksaan akhir selesai, keduanya dihentikan kembali agar tidak membebani laptop.
- Atas permintaan pengguna, seluruh perubahan V2 dipaketkan dan didorong ke `origin/main` pada repository `adrianfirmansyah24/PROJEKUASOOPSMT3`. Identitas commit repository-local menggunakan `adrianfirmansyah24` dan email no-reply GitHub; `.env` lokal tetap diabaikan dan tidak masuk commit.

### 6 November 2026 — Keputusan session cookie dan konsep interaksi komunitas

- Pengguna menyetujui autentikasi session cookie dan meminta penjelasan ulang mengenai peran petugas, pengguna, interaksi antarpengguna, dan anonimitas sebelum implementasi.
- Menambahkan `docs/konsep-peran-dan-interaksi.md`. Perubahan sesi ini hanya dokumentasi; tidak mengubah Java, migrasi database, Docker, atau data.
- Menetapkan konsep yang direkomendasikan: Foundly adalah pencarian berbasis komunitas dengan verifikasi dimediasi petugas. Satu role `PENGGUNA` dapat menjadi pelapor kehilangan, penemu, pemberi saran, atau pengaju klaim sesuai kasus.
- Menetapkan model privasi yang direkomendasikan: identitas pelapor anonim bagi pengguna lain, tetapi akun diketahui sistem dan dapat diakses petugas sesuai kebutuhan. Membaca laporan publik dapat tanpa login; membuat laporan, memberi saran, dan mengajukan klaim wajib login.
- Menetapkan petugas sebagai akun resmi unit kampus yang menerima barang, melihat data privat, memeriksa klaim, dan mencatat penyerahan. Petugas juga menangani administrasi operasional versi awal; belum ada role admin terpisah.
- Merekomendasikan interaksi tidak langsung melalui laporan dan `saran kecocokan`, tanpa chat atau pertukaran kontak pada MVP. Saran tidak sama dengan klaim dan tidak membuktikan kepemilikan.
- Saat sesi ini berakhir, `saran kecocokan` masih proposal. Pengguna kemudian mengonfirmasinya dan ERD direvisi dengan tabel kedelapan `saran_kecocokan`; tabel/API tersebut tetap belum diimplementasikan.
- Keputusan autentikasi: Spring Security session cookie, password BCrypt, cookie HttpOnly/SameSite, CSRF aktif, dan CORS credential untuk React. Penyimpanan session awal menggunakan mekanisme server Spring; sesi persisten lintas restart bukan kebutuhan MVP.
- README diperbarui agar menunjuk dokumentasi konsep ini. Verifikasi dokumentasi menggunakan `git diff --check`; Docker dan backend tetap mati.

### 6 November 2026 — Finalisasi ERD target

- Pengguna meminta memfinalisasi ERD sebelum implementasi berikutnya dan mewajibkan setiap perubahan dicatat dalam Markdown.
- Menambahkan `docs/erd.md` sebagai sumber rancangan database target. Tidak mengubah kode Java, migrasi V1, database lokal, container, atau data.
- Menetapkan tujuh tabel inti awal: `pengguna`, `barang`, `laporan`, `lampiran_laporan`, `klaim`, `bukti_klaim`, dan `penyerahan`, lengkap dengan key, cardinality, kamus data, aturan privasi, index, optimistic locking, dan kebijakan tanpa hard delete untuk data transaksi. Rancangan kemudian menjadi delapan tabel setelah `saran_kecocokan` dikonfirmasi.
- Menetapkan bahwa klaim wajib merujuk laporan penemuan dan opsional merujuk laporan kehilangan milik pengaju. Laporan kehilangan tidak menjadi syarat mengajukan klaim.
- Menetapkan alur status laporan kehilangan/penemuan dan klaim, termasuk pembatalan klaim disetujui sebelum penyerahan serta perubahan atomik saat penyerahan dicatat.
- Menetapkan dua peran akun: `PENGGUNA` dan `PETUGAS`; petugas menjalankan fungsi administrasi versi awal. Pendaftaran publik tidak dapat menentukan peran petugas.
- Menetapkan lampiran/bukti sebagai metadata file di MySQL; file fisik tidak disimpan sebagai BLOB. Penyedia storage diputuskan saat implementasi upload.
- Menetapkan rencana migrasi awal tanpa mengubah V1. Urutan terbaru setelah fitur saran dikonfirmasi adalah V2 pengguna/kepemilikan (selesai), V3 penyempurnaan laporan/lampiran, V4 saran kecocokan, V5 klaim/bukti, dan V6 penyerahan.
- Memperbarui README dan `docs/tahap-1.md` agar membedakan implementasi V1 dari ERD target.
- Hal yang tetap terbuka: session/token untuk login, storage file, daftar lokasi resmi, unit penitipan, serta pencocokan dengan rubrik lengkap dosen.
- Verifikasi dokumentasi dilakukan tanpa menyalakan Docker atau backend: memeriksa referensi file, sintaks dasar Mermaid ERD, dan `git diff --check`.

### 6 November 2026 — Persiapan dan push kode ke GitHub

- Pengguna meminta melakukan push ke remote GitHub `https://github.com/adrianfirmansyah24/PROJEKUASOOPSMT3`.
- Memeriksa keamanan dan privasi: file `.env` yang memuat password lokal dipastikan diabaikan oleh `.gitignore` dan tidak masuk ke index Git. Hanya `.env.example` yang berisi placeholder yang diunggah.
- Memperkuat `.gitignore` di root dengan menambahkan `target/` dan `*.log` agar artefak kompilasi dan log lokal tidak terbawa.
- Menyiapkan commit seluruh fondasi backend Java/Spring Boot, skrip migrasi Flyway V1, konfigurasi Docker Compose, dokumentasi tahap 1, dan `AGENTS.md`.
- Melakukan push commit ke branch `main` pada remote GitHub.

### 6 November 2026 — Menghentikan layanan lokal dan memperjelas cakupan

- Pengguna melaporkan laptop terasa berat dan meminta mematikan Docker serta penjelasan letak ERD/PBO.
- Menghentikan backend JAR yang dijalankan sesi sebelumnya dan MySQL Foundly melalui `docker compose stop mysql`. Volume dan laporan contoh dipertahankan.
- Memeriksa `docker ps`: tidak ada container aktif lain setelah Foundly dihentikan. `docker desktop stop --timeout 45` berhasil (exit 0) untuk membebaskan sumber daya mesin virtual. Status aktif pada catatan pengujian sebelumnya adalah historis; backend, MySQL Foundly, dan Docker Desktop sekarang dihentikan.
- Memperjelas status: implementasi masih tahap awal laporan, belum seluruh Foundly atau seluruh materi PBO. Empat pilar sudah dipakai pada class Barang/subclass dan pemetaan response API. Interface strategi buatan sendiri, overloading, DemoPBO, klaim, dan penyerahan masih menyusul.
- Skema dua tabel saat ini ditulis di `docs/tahap-1.md` dan SQL V1. Belum ada berkas gambar/diagram ERD visual lengkap. Relasi laporan.barang_id ke barang.id bersifat unik; setiap laporan wajib punya satu deskripsi barang, constraint database membatasi satu barang paling banyak dirujuk satu laporan.
- Selama belajar/membaca kode, Docker dan backend tidak perlu dinyalakan. Aktifkan kembali hanya saat pengujian API/database. Tidak menjalankan ulang build atau tes untuk penghentian layanan/dokumentasi ini.

### 6 November 2026 — MySQL Docker dan tahap awal laporan

- Menyalakan Docker Desktop sesuai izin pengguna. Menemukan proyek lain memakai port 3306/8080; Foundly memakai MySQL 3307 dan backend 8081 lewat .env. Tidak mengubah container proyek lain.
- Menambahkan compose.yaml: MySQL 8.4.13, database/user foundly, binding localhost, healthcheck SQL, restart policy, dan volume foundly_mysql_data. Password aplikasi/root acak dibuat di .env lokal (diabaikan Git), dengan .env.example sebagai template.
- Menambahkan Spring Data JPA, driver MySQL, Flyway, dan Testcontainers. Spring membaca .env root/backend; koneksi memakai user aplikasi. Skema dikelola migrasi SQL, Hibernate hanya validate, open-in-view dimatikan.
- Membuat migrasi V1 untuk barang dan laporan, relasi satu-ke-satu, foreign key, unique constraint, discriminator kategori, dan aturan konsistensi status awal. Perubahan status lanjutan harus disertai migrasi baru.
- Membuat abstract Barang dan subclass BarangElektronik/KartuIdentitas/BarangUmum dengan SINGLE_TABLE; constructor memakai super, atribut private, dan panduan klaim di-override. Laporan menentukan status awal tanpa setter umum.
- Menambahkan POST /api/laporan, GET /api/laporan, dan GET /api/laporan/{id}, lapisan controller/service/repository/DTO, transaksi, validasi, paginasi, filter jenis/kata kunci, serta custom exception HTTP 404 dan input error HTTP 400.
- Response publik tidak memuat ciri privat. Pencarian hanya pada nama/deskripsi publik. Panduan bukti dipanggil secara polymorphic dan bukan penentuan kepemilikan otomatis.
- Batas tahap: belum ada login/otorisasi, relasi pelapor, upload foto, update/delete, filter kategori/lokasi/waktu, daftar lokasi terkontrol, klaim, penyerahan, atau frontend. API diikat ke localhost; belum siap dipublikasikan. DemoPBO dan interface strategi pencocokan buatan sendiri menyusul.
- Menambahkan pengujian HTTP dengan container MySQL sementara terpisah, termasuk migrasi, penyimpanan/pembacaan semua subclass, privasi, filter/paginasi, validasi, dan 404. Data pengembangan tidak dibersihkan oleh tes.
- Memperbarui README, docs/tahap-1.md, dan contoh body JSON Postman/curl. Dokumen tahap mencatat skema awal, tanggung jawab class, dan pemetaan materi PBO yang sudah dipakai.
- Verifikasi: build bersih offline `./mvnw -o -B -Dmaven.repo.local=/tmp/foundly-maven/repository clean verify` berhasil (BUILD SUCCESS), 16 tes lulus, tanpa failure/error/skipped. Menggunakan JAVA_HOME Homebrew, MAVEN_USER_HOME=/tmp/foundly-maven, dan DOCKER_HOST socket Docker Desktop.
- Kendala verifikasi yang sudah ditangani: unduhan/ekstraksi pertama melewati timeout Testcontainers; menambahkan konfigurasi timeout pull serta startup untuk Docker Desktop lambat. Percobaan berikutnya menemukan class sintetis pengujian tidak lengkap; clean build menghasilkan ulang class tersebut dan seluruh tes kemudian lulus.
- MySQL pengembangan berstatus healthy; login user foundly dan SELECT DATABASE()/VERSION() berhasil. Backend JAR dijalankan pada 127.0.0.1:8081, log sementara di /tmp/foundly-backend.log. Migrasi V1 berhasil diterapkan ke database foundly.
- Percobaan API nyata: GET /api/health menghasilkan HTTP 200; POST menggunakan docs/laporan-contoh.json menghasilkan HTTP 201 dan laporan contoh ID 1 (headphone hitam). Response tidak memuat ciri privat. Satu data contoh ini sengaja dipertahankan agar pengguna bisa mencoba GET detail.
- Pemeriksaan persistensi: MySQL Foundly direstart, kembali healthy, dan query SQL membuktikan laporan ID 1 serta riwayat migrasi V1 tetap ada. GET detail ID 1 dan pencarian headphone setelah restart menghasilkan HTTP 200 dengan satu laporan dan tanpa ciri privat. Perintah Compose tidak menyentuh volume/container proyek lain. Validasi Compose dan git diff --check lulus.

### 6 November 2026 — Penegasan tujuan dan hubungan PBO dengan backend

- Pengguna menyetujui Java 21 dan meminta penjelasan lengkap serta penegasan arah proyek.
- PBO adalah pendekatan menyusun kode Java, bukan teknologi terpisah dari backend. Class domain PBO Foundly menjadi bagian backend Spring Boot dan dipakai oleh endpoint aplikasi.
- Tujuan produk: web barang hilang/temuan dengan verifikasi klaim oleh petugas dan pencatatan penyerahan di lingkungan UPN “Veteran” Jakarta.
- Tujuan akademik: empat pilar dan materi pendukung tampak pada class, method, aturan, dan skenario aplikasi yang benar-benar berjalan serta dapat dijelaskan pengguna.
- Alur teknis yang menjadi pedoman: React/Postman → controller → service → objek domain dan repository → MySQL, lalu response DTO kembali ke pemanggil. Domain menerapkan aturan objek; service mengatur otorisasi dan aturan lintas objek dalam transaksi yang sesuai.
- Spring Boot menyediakan infrastruktur web dan pengelolaan komponen; penggunaan annotation/framework saja tidak dianggap bukti lengkap penguasaan PBO.
- Rencana contoh yang dipakai aplikasi: panduan bukti klaim dipanggil melalui `Barang` dan di-override menurut subclass; perubahan status `Klaim` divalidasi melalui method; service memeriksa peran, ketersediaan barang, dan klaim yang sudah disetujui. Bukti tetap diperiksa petugas, bukan otomatis diputuskan dari panduan/kecocokan.
- `DemoPBO.java` kelak memakai class domain yang sama dengan backend, tanpa menyalin logika ke aplikasi contoh terpisah.
- Ruang lingkup tetap mengikuti fitur utama dan dua peran yang dibahas; detail formulir, status, ERD, serta lokasi penitipan masih perlu dimatangkan. Tidak menjamin seluruh rubrik semester tercakup sebelum modul lengkap tersedia.
- Urutan kerja: pahami fondasi yang sudah ada → matangkan model/aturan dan ERD → koneksi MySQL/JPA serta CRUD laporan → klaim/pemeriksaan/penyerahan dan otorisasi → integrasi React → demo serta pemetaan materi PBO. Login harus tersedia sebelum fitur berotorisasi dinyatakan selesai.
- Perubahan sesi ini hanya dokumentasi; belum menambahkan fitur bisnis. Verifikasi dokumentasi: `git diff --check`.

### 6 November 2026 — Fondasi backend Spring Boot

- Membuat `backend/` dari Spring Initializr resmi, Java 21 dan Spring Boot 4.1.2, dengan Maven Wrapper untuk macOS/Linux dan Windows.
- Menambahkan `HealthController` dan DTO `HealthResponse`: `GET /api/health` mengembalikan identitas aplikasi, status `UP`, dan pesan dalam JSON.
- Menambahkan konfigurasi port melalui `PORT` dengan default 8080; backend belum memerlukan MySQL agar tahap awal mudah dijalankan.
- Menambahkan uji integrasi yang menjalankan server pada port acak dan memeriksa HTTP 200, content type JSON, dan isi response health.
- Mengisi `readme.md` dengan prasyarat, cara menjalankan, contoh response, pengujian, struktur, serta penjelasan alur request.
- Menambahkan ignore file lokal/rahasia dan mengaktifkan dokumen ini sebagai catatan proyek.
- Java 21 tersedia di komputer pengembangan; Maven global tidak tersedia. macOS belum mendeteksi JDK lewat `java_home`, sehingga README mencantumkan cara mengatur `JAVA_HOME` untuk Homebrew.
- Memperbaiki versi parent POM hasil generator dari `4.1.2.RELEASE` menjadi `4.1.2`, sesuai artefak yang tersedia di Maven Central.
- Verifikasi: `./mvnw -B -Dmaven.repo.local=/tmp/foundly-maven/repository clean verify` dengan `JAVA_HOME` Homebrew dan `MAVEN_USER_HOME=/tmp/foundly-maven` berhasil (`BUILD SUCCESS`). Satu uji integrasi lulus tanpa failure/error; server nyata mengembalikan HTTP 200 dan JSON health yang diharapkan. JAR executable dihasilkan di `backend/target/foundly-0.0.1-SNAPSHOT.jar`.
- Cache Maven verifikasi disimpan di `/tmp` karena batas tulis lingkungan. Pemakaian normal di luar sandbox dapat menggunakan cache default. `git diff --check` juga lulus.
- Langkah berikutnya: jelaskan file fondasi kepada pengguna, lalu rancang skema data dan konfigurasi MySQL/JPA sebelum CRUD laporan.
