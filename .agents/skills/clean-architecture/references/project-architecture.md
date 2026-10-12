# Project Architecture Reference

## Tujuan

Referensi ini menyimpan konvensi project Personal Job Application Tracker agar perubahan tetap konsisten dengan source dan tidak mengikuti template Flutter/Clean Architecture generik.

Selalu verifikasi source terbaru. Informasi di file ini adalah baseline project, bukan pengganti audit source.

## Stack baseline

- Java 21
- Spring Boot 3.x
- Vaadin Flow 24.x
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Jakarta Validation
- BCrypt untuk password

## Struktur package konseptual

Project menggunakan layered Spring/Vaadin application, bukan folder `features/data/domain/presentation`.

Struktur yang umum:

```text
src/main/java/com/example/
├── config/          # configuration / initializer bila ada
├── entity/          # JPA entity + enum terkait
├── repository/      # Spring Data JPA repository
├── security/        # authentication/security classes
├── service/         # application/business service
├── view/            # Vaadin routes/pages dan reusable UI components sesuai existing source
└── ...              # package existing lain mengikuti source aktual

src/main/resources/
└── META-INF/resources/styles.css

src/main/frontend/
└── index.html       # frontend bootstrap/external icon script bila existing
```

Jangan membuat package baru hanya agar cocok dengan template arsitektur generik. Ikuti source aktual.

## Layer flow

```text
User
↓
Vaadin View
↓
Service
↓
Spring Data Repository
↓
JPA Entity / PostgreSQL
```

### Vaadin View

Contoh tanggung jawab:

- `@Route`
- `@PageTitle`
- `@RolesAllowed` / annotation security existing
- membuat component/layout
- click listener
- simple UI state
- memanggil Service
- `UI.getCurrent().navigate(...)`
- `Notification.show(...)`

### Service

Contoh tanggung jawab:

- save
- duplicate check
- ownership check
- status update
- query orchestration dari Repository

### Repository

Gunakan Spring Data JPA interfaces. Tidak perlu membuat repository interface domain + repository implementation terpisah jika project tidak menggunakan pola itu.

### Entity

Entity adalah persistence model. Jangan membuat domain entity kedua yang menduplikasi JPA entity kecuali ada kebutuhan eksplisit.

## Role baseline

Role yang ada:

```text
PELAMAR
PEMBERI_LAMARAN
ADMIN
```

Tidak ada `SUPER_ADMIN` sebagai baseline.

## Entity baseline

### User

Baseline penting:

- user memiliki role
- user dapat berelasi dengan Company
- Company tidak harus ada untuk semua role

Pelamar/Admin umumnya tidak membutuhkan Company. Employer/PEMBERI_LAMARAN menggunakan Company sesuai flow source.

### Company

Verifikasi field aktual sebelum menggunakan. Jangan mengarang company description, industry, website, size, atau field lain jika belum ada di entity.

### JobPosting

Baseline field yang pernah digunakan project:

```text
id
position
companyName
location
tipeKerja
jobTipe
salaryMin
salaryMax
description
requirements
postedDate
active
employer
```

Verifikasi source terbaru sebelum menambah/menghapus field.

`active` digunakan untuk menentukan lowongan aktif/nonaktif.

### JobApplication

Baseline field yang pernah digunakan project:

```text
id
companyName
position
expectedSalary
applicationDate
status
tipeKerja
notes
applicant
jobPosting
```

Catatan:

- field yang tidak dipakai Pelamar belum tentu aman dihapus
- `expectedSalary` dan `notes` dapat masih digunakan oleh Admin/manual tracker
- selalu search seluruh penggunaan sebelum menghapus field

## Enum baseline

### ApplicationStatus

```text
APPLIED
SCREENING
TECHNICAL_TEST
INTERVIEW
OFFERED
REJECTED
```

### ApplicationTipe

```text
WFO
WFH
HYBRID
```

Display Applicant yang pernah dipakai:

```text
WFO    -> Onsite
WFH    -> Remote
HYBRID -> Hybrid
```

### JobTipe

```text
FULL_TIME
PART_TIME
CONTRACT
INTERNSHIP
FREELANCE
```

## Route/view baseline

Selalu verifikasi source terbaru, namun baseline yang pernah ada:

```text
/                         Dashboard dispatcher
/pelamar                  Browse Jobs
/pelamar/job/{id}         Job Detail
/applications             Applications
/pemberi-lamaran/...      Employer area
/admin...                  Admin area sesuai source
```

Apply page baru dapat mengikuti pola `HasUrlParameter<Long>` bila source aktual memang memakai pola tersebut.

## Security baseline

Gunakan role annotation sesuai halaman.

Contoh prinsip:

```text
Applicant-only page -> PELAMAR
Employer-owned data -> PEMBERI_LAMARAN + ownership validation
Admin page -> ADMIN
```

Jika sebuah halaman sengaja bisa diakses beberapa role, pertahankan behavior existing kecuali requirement meminta perubahan.

Security tidak cukup hanya dengan menyembunyikan button. Ownership/data mutation rule perlu divalidasi di layer yang sesuai.

## Current-user baseline

Project pernah menggunakan pola:

```text
AuthenticationContext
↓
get principal email
↓
UserRepository.findByEmail(...)
↓
User currentUser
```

Jangan mengganti pola ini dengan auth facade/session abstraction tanpa kebutuhan nyata.

## UI conventions

### Applicant

Gunakan scoped class dengan prefix seperti:

```text
.applicant-...
```

### Employer

Gunakan scoped class dengan prefix seperti:

```text
.employer-...
```

### Icons

Project menggunakan Iconify di frontend dan Lucide icon names dapat dipakai melalui mekanisme existing.

Jangan menambah library icon baru tanpa kebutuhan.

### CSS

Prefer root class per page/feature:

```css
.applicant-job-detail ...
.applicant-apply-page ...
```

Hindari global selector yang dapat merusak halaman lain.

## Existing feature patterns

### Browse Jobs

Baseline behavior yang pernah ada:

- active jobs only
- keyword search
- location search
- Working Type filter
- Job Type filter
- sort pilihan
- in-memory pagination
- page size 6
- job cards
- View Details
- Apply/Applied state

Untuk list processing gunakan:

```text
all data
→ filter
→ sort
→ paginate
→ render
```

### Job Detail

Full-page Applicant Job Detail menggunakan data JobPosting existing. Hindari membuat content palsu seperti company profile lengkap jika entity belum menyediakannya.

### Apply Job

UI dapat memiliki data tambahan design-only, tetapi persistence hanya boleh dibuat jika data model/storage sudah disepakati.

Jangan menganggap upload CV sudah persisted hanya karena komponen Upload tampil di UI.

## Coding style

Utamakan keterbacaan pemula.

Bagus:

```java
if (isPelamar) {
    add(applicantDashboard);
} else {
    add(otherContent);
}
```

Hindari expression kompleks seperti:

```java
var result = items.stream()
        .filter(...)
        .map(...)
        .collect(...);
```

jika loop biasa jauh lebih mudah dibaca untuk task tersebut.

Tidak dilarang menggunakan Stream, tetapi jangan menggunakannya hanya demi terlihat ringkas.

## Testing dan git

Gunakan Maven Wrapper:

```powershell
.\mvnw test
```

Jangan otomatis melakukan:

```text
git add .
git commit
git push
git reset
git restore
git clean
```

kecuali user meminta.

Jika user ingin commit file tertentu, stage file secara eksplisit bila ada perubahan unrelated.
