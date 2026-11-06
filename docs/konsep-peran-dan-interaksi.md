# Konsep peran dan interaksi Foundly

Status dokumen: konsep produk yang sudah disepakati. Autentikasi session cookie dan
kepemilikan laporan sudah diimplementasikan pada V2. `Saran kecocokan` sudah menjadi
bagian ERD target, tetapi tabel dan API-nya belum diimplementasikan.

## Gambaran singkat

Foundly adalah pencarian barang berbasis komunitas dengan verifikasi oleh petugas.

- Komunitas membantu menyebarkan laporan, menemukan barang, dan menyarankan kecocokan.
- Petugas menjaga barang temuan, memeriksa bukti klaim, dan mencatat penyerahan.
- Pengguna tidak menentukan sendiri siapa pemilik yang sah hanya melalui percakapan.

Interaksi antarpengguna berlangsung melalui laporan dan saran kecocokan di dalam
sistem. Identitas dan kontak pribadi tidak dibuka ke pengguna lain.

## Siapa yang disebut pengguna?

`PENGGUNA` adalah akun mahasiswa, dosen, tenaga kependidikan, atau anggota komunitas
kampus yang memakai Foundly. Satu akun tidak memiliki posisi tetap dalam suatu kasus.
Pengguna yang sama dapat menjadi:

1. **Pelapor kehilangan** ketika barangnya hilang.
2. **Penemu/pelapor temuan** ketika menemukan barang orang lain.
3. **Pemberi saran** ketika melihat dua laporan yang mungkin berkaitan.
4. **Pengaju klaim** ketika merasa sebagai pemilik barang temuan.

Istilah tersebut adalah posisi dalam alur, bukan role akun tambahan. Role database
tetap `PENGGUNA`.

Pengguna dapat:

- melihat laporan publik;
- membuat laporan kehilangan atau penemuan setelah login;
- mengubah atau membatalkan laporan miliknya sesuai status;
- mencari berdasarkan kategori, lokasi, waktu, dan kata kunci;
- menyarankan kemungkinan kecocokan antara laporan kehilangan dan temuan;
- mengajukan klaim atas barang temuan;
- mengirim bukti privat kepada petugas;
- melihat status laporan dan klaim miliknya.

## Siapa petugas?

`PETUGAS` adalah akun resmi unit kampus yang ditunjuk menangani barang hilang dan
temuan. Pada versi awal, petugas juga menjalankan fungsi administrasi operasional;
belum ada role `ADMIN` terpisah.

Petugas dapat:

- menerima barang fisik dari penemu;
- menandai laporan temuan dari `MENUNGGU_PENITIPAN` menjadi `TERSEDIA`;
- melihat ciri privat barang dan bukti klaim;
- mengambil klaim untuk diperiksa;
- menyetujui atau menolak klaim dengan alasan;
- mencatat penyerahan kepada pengaju yang klaimnya disetujui;
- membatalkan laporan tidak valid dan menangani penyalahgunaan;
- melihat data akun secukupnya untuk pemeriksaan dan audit.

Petugas tidak boleh menyatakan pemilik hanya karena warna, kategori, lokasi, atau
satu ciri cocok. Bukti tetap diperiksa dengan barang fisik dan data privat.

## Apakah pengguna saling berinteraksi?

Ya, tetapi interaksinya direkomendasikan bersifat **tidak langsung dan terstruktur**.
Versi awal tidak memerlukan chat atau pertukaran nomor telepon.

### Bantuan pasif

Laporan aman ditampilkan kepada komunitas. Ketika Andi kehilangan dompet, pengguna
lain dapat melihat bahwa ada dompet hitam hilang di area kantin. Informasi seperti
isi dompet, nomor kartu, dan kontak Andi tetap tersembunyi.

### Bantuan aktif

Pengguna dapat menekan `Sarankan kecocokan` ketika melihat laporan kehilangan dan
laporan temuan yang kemungkinan berkaitan. Saran hanya memberi tahu pemilik laporan
dan tidak membuat klaim otomatis.

Contoh:

1. Andi membuat laporan kehilangan dompet.
2. Budi menemukan dompet dan membuat laporan temuan, lalu menitipkannya ke petugas.
3. Citra melihat kedua laporan dan menyarankan bahwa keduanya mungkin cocok.
4. Andi menerima saran di dashboard dan memeriksa laporan temuan.
5. Jika merasa barang itu miliknya, Andi mengajukan klaim dengan bukti privat.
6. Petugas membandingkan bukti dengan barang dan memutuskan klaim.
7. Andi mengambil barang dari petugas; Budi dan Citra tidak perlu menerima alamat,
   nomor telepon, atau bukti kepemilikan Andi.

Saran kecocokan dapat datang dari pengguna atau mesin pencocokan berbasis aturan.
Keduanya hanya rekomendasi, bukan keputusan kepemilikan.

## Apakah laporan anonim?

Rekomendasi Foundly adalah **anonim bagi pengguna lain, tetapi tidak anonim bagi
sistem dan petugas**.

| Pihak | Informasi yang terlihat |
| --- | --- |
| Pengunjung/pengguna lain | Data publik barang, lokasi umum, waktu, status, dan foto publik; nama serta kontak pelapor tidak ditampilkan |
| Pemilik laporan | Data publik dan privat yang ia masukkan sendiri |
| Pengaju klaim | Laporan publik serta klaim/bukti miliknya; tidak melihat ciri privat temuan |
| Petugas | Identitas akun yang diperlukan, ciri privat, dan bukti klaim |
| Sistem | ID akun untuk kepemilikan data, pencegahan spam, dan audit |

Konsekuensinya:

- Melihat dan mencari laporan publik dapat dibuka tanpa login.
- Membuat laporan, memberi saran, dan mengajukan klaim wajib login.
- Halaman publik dapat menulis `Dilaporkan oleh pengguna Foundly` tanpa nama.
- Email, nomor telepon, ciri privat, serta bukti klaim tidak boleh masuk DTO publik.
- Penyerahan dilakukan melalui petugas, bukan pertemuan langsung antar pengguna.

Model ini tetap menjaga akuntabilitas karena sistem mengetahui akun pembuat aksi,
sekaligus melindungi identitas mereka dari pengguna lain.

## Mengapa belum memakai chat langsung?

Chat menambah moderasi, spam, penyamaran sebagai pemilik, kebocoran kontak, dan
penyimpanan pesan. Alur inti Foundly dapat bekerja tanpa chat karena laporan, saran
kecocokan, klaim, dan petugas sudah menyediakan jalur komunikasi yang diperlukan.

Chat tersamarkan dapat dipertimbangkan setelah fitur utama selesai, tetapi bukan
bagian MVP UAS.

## Matriks tindakan

| Tindakan | Tanpa login | Pengguna login | Petugas |
| --- | --- | --- | --- |
| Melihat/mencari laporan publik | Ya | Ya | Ya |
| Membuat laporan | Tidak | Ya | Ya sebagai pengguna bila diperlukan |
| Mengubah/membatalkan laporan | Tidak | Hanya miliknya | Sesuai alasan operasional |
| Menyarankan kecocokan | Tidak | Ya | Ya |
| Mengajukan/membatalkan klaim | Tidak | Hanya klaim miliknya | Tidak sebagai pengaju dalam tugas petugas |
| Melihat ciri privat temuan | Tidak | Hanya pelapor temuan | Ya |
| Melihat bukti klaim | Tidak | Hanya pengaju terkait | Ya |
| Memutuskan klaim | Tidak | Tidak | Ya |
| Mencatat penyerahan | Tidak | Tidak | Ya |

## Keputusan autentikasi

Foundly menggunakan Spring Security dengan session cookie.

- Browser menyimpan ID sesi acak, bukan password.
- Password disimpan sebagai hash BCrypt.
- Cookie sesi menggunakan `HttpOnly`, `SameSite=Lax`, dan `Secure` saat HTTPS.
- CSRF protection tetap aktif.
- React mengirim request terautentikasi dengan credentials.
- Logout menghapus sesi melalui endpoint `POST`.

Endpoint registrasi, login, logout, informasi akun aktif, dan pengambilan token CSRF
sudah dibuat pada V2. Detail pemakaian tersedia di `tahap-2-autentikasi.md`.

## Dampak terhadap ERD

ERD target sekarang memuat tabel `saran_kecocokan` dengan relasi:

```text
pengguna 1 ── 0..* saran_kecocokan
laporan kehilangan 1 ── 0..* saran_kecocokan
laporan penemuan 1 ── 0..* saran_kecocokan
```

Tabel tersebut direncanakan menyimpan pemberi saran, pasangan laporan, sumber saran
(`PENGGUNA`/`SISTEM`), status saran, dan waktu pembuatan. Penambahan ERD ini belum
membuat tabel atau endpoint. Kepemilikan tetap hanya diputuskan melalui klaim dan
pemeriksaan petugas.
