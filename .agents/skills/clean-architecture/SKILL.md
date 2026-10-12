---
name: clean-architecture
description: Menjaga struktur dan boundary project Personal Job Application Tracker berbasis Java 21, Spring Boot, Vaadin Flow, Spring Security, Spring Data JPA, PostgreSQL, dan Maven. Gunakan saat membuat fitur baru, menambah View/Component/Service/Repository/Entity, merapikan struktur project, memindahkan logic antar layer, menambah route atau security, atau mereview apakah implementasi mengikuti arsitektur project tanpa overengineering.
---

# Arsitektur Project Java Spring Boot + Vaadin

Gunakan skill ini sebagai pedoman saat membuat, menambah, atau merapikan fitur pada project Personal Job Application Tracker.

Project ini bukan Flutter dan tidak menggunakan struktur `data/domain/presentation` ala Riverpod. Ikuti struktur dan pola yang memang sudah dipakai project Java Spring Boot + Vaadin ini.

## Prinsip utama

1. Audit source yang relevan sebelum mengubah kode.
2. Jangan menebak struktur, method, route, role, atau field yang belum terbukti dari source.
3. Pertahankan pola existing jika sudah ada dan masih cocok.
4. Pilih implementasi paling sederhana yang memenuhi kebutuhan.
5. Hindari abstraksi baru hanya untuk terlihat lebih “clean”.
6. Pisahkan tanggung jawab View, Service, Repository, Entity, dan Component dengan jelas.
7. Jangan mengubah backend/entity/database jika kebutuhan hanya UI.
8. Jangan memperluas scope berdasarkan asumsi sendiri.
9. Utamakan kode yang mudah dibaca dan dijelaskan oleh developer pemula.

Jika fakta belum terbukti dari source, tulis `UNKNOWN`, `NOT PROVEN`, atau jelaskan bahwa hal tersebut belum terbukti.

## Stack project

Gunakan asumsi stack berikut hanya setelah sesuai dengan source project yang aktif:

- Java 21
- Spring Boot
- Vaadin Flow
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Jakarta Validation

Untuk detail konvensi dan struktur project, baca `references/project-architecture.md` bila task menyentuh struktur folder, role, entity, route, security, atau alur data.

## Struktur layer yang digunakan

Gunakan alur utama:

```text
Vaadin View / UI Component
        ↓
Service
        ↓
Repository
        ↓
Entity / Database
```

Security dan configuration bersifat lintas layer, bukan business layer baru.

### View

Tempatkan halaman Vaadin pada package/folder `view` sesuai pola project.

View bertanggung jawab untuk:

- route dan page title
- role access annotation
- membuat layout halaman
- menerima input user
- memanggil Service
- menampilkan hasil atau Notification
- navigasi antar View
- state UI sederhana seperti filter, sort, pagination in-memory

View boleh melakukan mapping UI sederhana yang langsung terlihat, misalnya:

- enum ke label UI
- format tanggal
- format currency
- membuat badge status
- menyusun card dari entity yang sudah diterima

View jangan menjadi tempat query database langsung atau business rule kompleks.

### Component

Gunakan component reusable hanya jika memang dipakai ulang atau secara nyata membuat View lebih mudah dibaca.

Contoh yang cocok:

- card yang dipakai di beberapa View
- grid reusable
- form reusable
- stat summary card

Jangan membuat component baru hanya untuk membungkus 2–3 baris kode yang hanya dipakai sekali.

### Service

Service bertanggung jawab untuk logic aplikasi dan akses Repository.

Contoh:

- simpan entity
- cari data berdasarkan user/employer/posting
- validasi ownership
- update status
- duplicate check
- aturan yang tidak seharusnya bergantung pada komponen Vaadin

Service tidak boleh membuat komponen UI, Notification, Dialog, atau melakukan navigation.

Jika kebutuhan baru cukup memakai method Service yang sudah ada, pakai ulang method tersebut. Jangan membuat Service baru hanya sebagai wrapper satu baris tanpa alasan nyata.

### Repository

Repository adalah pintu akses persistence.

Gunakan Spring Data JPA repository sesuai pola existing project.

Repository bertanggung jawab untuk:

- query persistence
- `findBy...`
- `existsBy...`
- `delete...`
- method turunan Spring Data
- custom query jika benar-benar diperlukan

Jangan menaruh formatting UI atau aturan navigasi di Repository.

Jangan membuat repository abstraction + implementation tambahan seperti pola Flutter/domain repository jika project ini sudah menggunakan interface Spring Data JPA secara langsung.

### Entity

Entity merepresentasikan data persistence/JPA.

Ubah Entity hanya jika requirement memang membutuhkan data baru atau perubahan model data.

Sebelum menambah field:

1. cek apakah field serupa sudah ada
2. cek semua penggunaan getter/setter-nya
3. cek form, view, service, export, test, dan query yang bergantung pada field tersebut
4. nilai dampak database/schema

Jangan menambah field hanya untuk membuat desain UI terlihat lengkap.

## Boundary yang harus dijaga

Gunakan aturan sederhana berikut:

```text
View -> Service -> Repository -> Database
```

Boleh:

- View menerima Entity/hasil dari Service
- Service menerima Entity atau parameter sederhana dari View
- Service memanggil Repository
- Repository mengembalikan Entity

Hindari secara default:

- View memanggil Repository langsung, kecuali pola existing yang sangat spesifik dan task tidak meminta refactor
- Repository memanggil View
- Service mengakses komponen Vaadin
- Entity mengetahui Service atau View
- membuat DTO/usecase/mapper hanya karena pola Clean Architecture generik menyarankannya

Project ini tidak mewajibkan DTO, usecase class, mapper, provider, datasource, atau repository implementation layer terpisah untuk fitur internal Vaadin/JPA.

Tambahkan abstraction tersebut hanya jika ada kebutuhan nyata dan user memang meminta arsitektur itu.

## Workflow sebelum implementasi fitur

Ikuti urutan ini.

### 1. Cek git status

Jalankan:

```bash
git status
```

Catat perubahan existing. Jangan restore, reset, checkout, clean, stage, commit, atau push tanpa instruksi user.

### 2. Audit source relevan

Cari View, Service, Repository, Entity, Component, CSS, route, dan test yang berkaitan.

Jawab minimal:

- alur sekarang bagaimana
- file apa yang terlibat
- method existing apa yang bisa dipakai ulang
- data apa yang memang tersedia
- security/role apa yang berlaku
- perubahan minimum ada di layer mana

### 3. Klasifikasikan kebutuhan

Gunakan klasifikasi:

```text
UI only
UI + existing Service
Service logic
Repository/query
Entity/database
Security
```

Pilih layer paling rendah yang benar-benar perlu diubah.

Contoh:

- perubahan spacing/filter/sort in-memory -> View + CSS saja
- halaman baru memakai data existing -> View + existing Service
- duplicate check reusable -> Service
- query data baru -> Repository + Service
- field persistence baru -> Entity + database impact + layer pengguna field

### 4. Implementasi minimum

Mulai dari existing pattern terdekat.

Jangan refactor seluruh project hanya karena sedang menambah satu fitur.

### 5. Test

Jalankan:

```bash
./mvnw test
```

Pada Windows PowerShell gunakan:

```powershell
.\mvnw test
```

Laporkan jumlah test, failure, error, skipped, dan BUILD result.

### 6. Cek git status lagi

Pastikan hanya file yang memang seharusnya berubah.

Jangan commit/push kecuali diminta.

## Workflow penambahan View baru

Saat menambah halaman Vaadin:

1. Cari View terdekat sebagai referensi pola project.
2. Gunakan `@Route` dengan `MainLayout.class` bila sesuai pola halaman authenticated.
3. Gunakan `@PageTitle`.
4. Gunakan annotation security yang sesuai, misalnya `@RolesAllowed` atau annotation existing project.
5. Inject dependency yang benar-benar diperlukan melalui constructor.
6. Ambil data melalui Service, bukan query baru di View jika Service sudah menyediakan method.
7. Buat layout dengan komponen Vaadin sederhana.
8. Tambahkan root CSS class khusus halaman.
9. Scope CSS di bawah root class halaman tersebut.
10. Uji navigation, role, empty state, data state, dan responsive behavior bila task menyentuh UI.

Jangan membuat Controller REST hanya untuk membuka halaman Vaadin.

## Workflow penambahan fitur pada View existing

Sebelum menambah kode:

1. cari method refresh/load/filter existing
2. gunakan ulang flow existing
3. tempatkan logic UI di titik yang paling dekat dengan state yang dipakai
4. jangan menduplikasi seluruh filtering/rendering jika cukup memanggil method existing

Untuk search/filter/sort/pagination in-memory, urutan yang disarankan:

```text
load data
↓
filter
↓
sort
↓
pagination
↓
render
```

Jangan sort hanya data satu page jika user bermaksud mengurutkan seluruh hasil.

## Workflow perubahan Service

Sebelum membuat method Service baru:

1. cari method existing dengan konteks yang sama
2. cek apakah bisa dipakai ulang tanpa mengubah semantics
3. cek ownership/security rule yang relevan
4. baru tambah method jika memang ada logic aplikasi baru

Nama method harus menjelaskan operasi, bukan sekadar mengikuti nama tombol UI.

Contoh yang sesuai pola project:

- `findByApplicant(...)`
- `findByEmployer(...)`
- `findByJobPosting(...)`
- `hasApplied(...)`
- `updateStatus(...)`
- `simpanLamaran(...)` jika nama existing memang begitu

Jangan mengganti nama method existing hanya untuk konsistensi bahasa jika task tidak meminta refactor.

## Workflow perubahan Repository

Tambahkan query Repository hanya ketika data yang dibutuhkan tidak dapat diperoleh secara wajar dari method existing.

Prioritaskan:

1. method Spring Data derived query
2. custom JPQL/query hanya jika diperlukan

Jangan memindahkan filter UI kecil ke Repository hanya karena “lebih clean” jika dataset dan pola project memang memakai in-memory filtering.

## Workflow perubahan Entity

Perubahan Entity adalah perubahan berdampak besar.

Sebelum melakukan perubahan:

1. cari seluruh penggunaan field
2. cek validation annotation
3. cek nullable/non-null relation
4. cek form existing
5. cek table/grid/export
6. cek service search/filter
7. cek test
8. jelaskan dampak schema/database

Jika requirement hanya desain visual, jangan ubah Entity.

## Security dan role

Gunakan role yang benar-benar ada di source project.

Project saat ini mengenal role utama:

- `PELAMAR`
- `PEMBERI_LAMARAN`
- `ADMIN`

Jangan membuat role baru seperti `SUPER_ADMIN` tanpa requirement eksplisit.

Gunakan annotation role pada View dan validasi ownership di Service jika akses data bergantung pada pemilik data.

Jangan menganggap tombol yang disembunyikan di UI sebagai satu-satunya security protection.

## Current-user pattern

Ikuti pola current user yang sudah digunakan project bila masih berlaku:

```text
AuthenticationContext
↓
principal email/name
↓
UserRepository / lookup user existing
```

Jangan membuat custom session manager, auth facade, atau user context service tanpa kebutuhan nyata.

Jika source terbaru sudah berubah, ikuti source terbaru.

## CSS dan UI rules

Untuk halaman Applicant/Employer yang sudah memiliki custom shell/style:

- gunakan root class spesifik, misalnya `.applicant-*` atau `.employer-*`
- scope selector di bawah halaman/fitur terkait
- hindari global `button`, `div`, `span`, `h1`, `input` selectors
- pertahankan style yang sudah disetujui jika task hanya perubahan kecil
- jangan redesign halaman lain ketika memperbaiki satu halaman

Untuk icon, gunakan mekanisme icon existing project. Jangan menambah icon library baru jika Iconify/Lucide existing sudah cukup.

## Gaya kode wajib

Tulis kode supaya mudah dibaca developer pemula.

Prioritaskan:

```java
if (condition) {
    ...
} else {
    ...
}
```

Gunakan:

- nama variable yang jelas
- method kecil dengan tujuan jelas
- loop biasa jika lebih mudah dibaca
- `Optional` secara sederhana sesuai pola Service/Repository
- constructor injection

Hindari kecuali benar-benar diperlukan:

- nested ternary
- stream chain panjang
- generic abstraction
- Strategy
- Factory
- Facade
- Manager
- command bus
- event architecture
- mapper layer yang tidak dibutuhkan
- DTO internal tanpa alasan
- helper class untuk logic yang hanya dipakai sekali

## Aturan reusable vs duplication

Jangan menghilangkan setiap duplication secara agresif.

Buat shared abstraction hanya jika:

- logic benar-benar sama
- dipakai beberapa tempat
- ownership/tanggung jawabnya jelas
- abstraction membuat kode lebih mudah dipahami

Jika menghilangkan 10 baris duplication membutuhkan 4 class baru, pertahankan implementasi sederhana kecuali ada alasan kuat.

## Audit dan laporan

Saat user meminta audit saja:

- jangan ubah file
- jangan membuat file
- jangan stage/commit/push
- pisahkan `SOURCE FACT` dan `ENGINEERING INTERPRETATION`
- jika tidak terbukti, gunakan `NOT PROVEN`
- sertakan file, class, method, dan flow yang relevan
- bandingkan git status sebelum dan sesudah audit

Saat user meminta implementasi:

laporkan minimal:

1. initial git status
2. source behavior yang diverifikasi
3. file dibuat
4. file diubah
5. perubahan per layer
6. test result
7. final git status
8. konfirmasi tidak ada scope tambahan

## Larangan scope creep

Jangan melakukan hal berikut tanpa instruksi eksplisit:

- menambah field database karena ada di gambar referensi UI
- menambah upload storage hanya karena ada upload box pada desain
- menambah role baru
- menambah route baru di luar kebutuhan task
- merombak MainLayout untuk memperbaiki satu halaman
- mengganti repository architecture project
- memecah fitur menjadi banyak layer/class hanya demi teori Clean Architecture
- mengubah bahasa UI secara global
- refactor file unrelated
- commit/push otomatis

Jika requirement membutuhkan perubahan di luar scope yang diberikan, berhenti dan jelaskan file/layer tambahan yang dibutuhkan sebelum melanjutkan.

## Contoh keputusan arsitektur

### Contoh A — tambah sorting Browse Jobs

Kebutuhan:

```text
Default Newest + pilihan sort
```

Jika semua active job sudah dimuat oleh View dan pagination/filter berlangsung in-memory:

```text
PelamarView
+ optional scoped CSS
```

Tidak perlu:

```text
Repository baru
Service baru
Entity baru
SortStrategy
```

### Contoh B — tambah halaman Apply Job memakai data existing

Gunakan:

```text
ApplyJobView
JobPostingService existing
JobApplicationService existing
current User existing
```

Jika Phone/CV hanya mockup UI dan belum menjadi requirement persistence, jangan menambah field Entity.

### Contoh C — Employer perlu menyimpan data Company baru

Ini bukan UI-only.

Audit minimal:

```text
Company entity
User relation
Registration flow
Company repository/service
Employer form/view
database impact
```

Baru tentukan perubahan setelah source terbukti.

## Referensi project

Baca `references/project-architecture.md` ketika perlu mengetahui struktur, role, entity utama, routing/security convention, atau aturan project-specific lainnya.
