# Rencana Refactoring

Branch yang digunakan:

`refactor/modular-architecture`

## Bagian MainView yang Akan Dipisahkan

### 1. Form Lamaran

Bagian ini digunakan untuk mengisi data lamaran, seperti:

- Nama perusahaan
- Posisi
- Tanggal melamar
- Status
- Tipe kerja
- Ekspektasi gaji
- Catatan
- Tombol Simpan / Update

Rencananya bagian ini akan dipisahkan dari `MainView`.

- [ ] `ApplicationForm`

### 2. Dashboard

Bagian ini digunakan untuk menampilkan jumlah:

- Total lamaran
- Lamaran yang masih diproses
- Lamaran diterima
- Lamaran ditolak

Saat ini kode untuk membuat dan menghitung dashboard masih berada di `MainView`.

Rencananya bagian ini akan dipisahkan.

- [ ] Buat `ApplicationDashboard`

### 3. Pencarian dan Filter

Bagian ini terdiri dari:

- Cari Lamaran
- Filter Status

Saat user mengetik pada pencarian atau memilih status, data pada tabel akan menyesuaikan.

Rencananya bagian ini akan dipisahkan.

- [ ] Buat `ApplicationToolbar`

## Kode Filter yang Berulang

Saya menemukan kode untuk mencari dan memfilter data ditulis beberapa kali, yaitu:

1. Saat isi `Cari Lamaran` berubah.
2. Saat `Filter Status` berubah.
3. Setelah data disimpan atau diubah.

Ketiga bagian tersebut melakukan proses yang hampir sama:

- Mengambil isi pencarian.
- Mengambil status yang dipilih.
- Mencari data yang sesuai.
- Menampilkan hasilnya ke tabel.

Rencananya kode yang sama tersebut akan dibuat satu kali agar tidak perlu ditulis berulang.

- [ ] Rapikan kode filter yang berulang.

## Yang Akan Dikerjakan

- [ ] Pisahkan Form Lamaran dari `MainView`
- [ ] Pisahkan Dashboard dari `MainView`
- [ ] Pisahkan Pencarian dan Filter dari `MainView`
- [ ] Rapikan kode filtering yang ditulis berulang
- [ ] Cek kembali semua fitur setelah perubahan