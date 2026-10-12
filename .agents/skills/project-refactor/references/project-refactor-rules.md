# Project Refactor Rules

## Baseline project

Gunakan ini sebagai baseline saat source aktual belum menunjukkan perubahan:

- Java 21
- Spring Boot 3.x
- Vaadin Flow 24.x
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Jakarta Validation
- BCrypt

Source aktual selalu lebih authoritative daripada reference ini.

## Role baseline

Role yang dikenal:
- `PELAMAR`
- `PEMBERI_LAMARAN`
- `ADMIN`

Jangan membuat `SUPER_ADMIN` atau role baru tanpa requirement eksplisit.

Audit annotation seperti `@RolesAllowed` dan conditional role check sebelum refactor.

## Struktur package yang diharapkan

Project tidak wajib mengikuti Flutter-style `data/domain/presentation`.

Gunakan pola existing Java project, secara konseptual:

```text
src/main/java/com/example/
├── config/
├── entity/ atau model/
├── repository/
├── security/
├── service/
├── view/
└── component/ bila memang ada

src/main/resources/
└── META-INF/resources/styles.css
```

Jangan memindahkan seluruh project ke feature folders jika user hanya meminta refactor lokal.

## Boundary praktis

### View
Contoh concern:
- Vaadin component tree
- listener
- dialog
- notification
- route navigation
- formatting presentational sederhana

### Service
Contoh concern:
- application save/update dengan rule
- employer ownership check
- application duplicate rule
- status transition/update
- aggregation data dashboard bila bukan sekadar formatting UI

### Repository
Contoh concern:
- `findBy...`
- `existsBy...`
- `countBy...`
- query persistence

### Entity
Contoh concern:
- field persistent
- enum
- JPA relation
- validation model

## Project conventions yang perlu dijaga

- `MainLayout` adalah app shell/navigation layout.
- View route memakai Vaadin `@Route`.
- Role access memakai Spring Security/Vaadin annotation dan/atau `AuthenticationContext` sesuai pola existing.
- Service biasanya injected lewat constructor.
- Repository menggunakan Spring Data JPA.
- CSS role/page sebaiknya scoped untuk mencegah leakage.

## Beberapa data/domain baseline

Nama yang mungkin ada dan harus diverifikasi sebelum digunakan:
- `JobPosting`
- `JobApplication`
- `User`
- `Company`
- `JobPostingService`
- `JobApplicationService`
- `UserRepository`
- `PelamarView`
- `JobDetailView`
- `ApplicationsView`
- `PemberiLamaranView`
- `JobApplicantsView`
- `DashboardView`

Jangan menyatakan class/method tetap ada hanya berdasarkan reference ini. Cari source aktual.

## Refactor safety checklist

Sebelum edit:
- [ ] `git status` dicatat
- [ ] file target dibaca
- [ ] caller/usage dicari
- [ ] route/role dicek
- [ ] test terkait dicari
- [ ] pre-existing diff dipisahkan

Saat edit:
- [ ] behavior tetap
- [ ] tidak menambah fitur
- [ ] tidak menambah dependency tanpa alasan
- [ ] tidak menambah abstraction yang tidak perlu
- [ ] tidak mengubah entity untuk task UI-only
- [ ] CSS tetap scoped

Setelah edit:
- [ ] test/compile dijalankan
- [ ] `git diff --check` bila memungkinkan
- [ ] `git status` dibandingkan dengan kondisi awal
- [ ] file task dan pre-existing changes dibedakan
- [ ] tidak commit/push tanpa instruksi

## Kapan TIDAK perlu refactor

Jangan refactor hanya karena:
- ada dua helper kecil yang mirip,
- method masih pendek dan jelas,
- class memakai pola sederhana yang sudah konsisten,
- abstraction baru hanya mengurangi beberapa baris,
- user sedang meminta visual fix kecil,
- perubahan tidak memberi peningkatan readability, boundary, testability, atau maintainability yang nyata.

## Contoh keputusan

### Kasus: dua View punya formatter salary identik
Boleh helper lokal tetap terpisah jika hanya beberapa baris dan refactor shared utility membuat dependency baru yang tidak perlu.

### Kasus: dua View melakukan creation + validation JobApplication identik
Audit dulu. Jika logic merupakan business rule yang harus konsisten di banyak entry point, pertimbangkan pindah ke `JobApplicationService`. Jangan otomatis membuat `ApplicationManager`.

### Kasus: Job Detail spacing bermasalah
Perbaiki CSS scoped Job Detail. Jangan menyentuh `MainLayout`, Entity, atau Service.

### Kasus: filtering Browse Jobs terlalu panjang
Pecah helper lokal yang jelas terlebih dahulu. Jangan pindah menjadi Specification/QueryDSL/server-side filtering tanpa requirement.
