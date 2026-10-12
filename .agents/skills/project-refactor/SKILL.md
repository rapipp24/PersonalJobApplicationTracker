---
name: project-refactor
description: Refactor dan rapikan project Java 21 Spring Boot + Vaadin secara aman tanpa mengubah behavior yang sudah berjalan. Gunakan saat user meminta refactor class/view/service, memecah file besar, mengurangi duplikasi, memindahkan logic ke layer yang tepat, merapikan struktur modular, memperbaiki readability, atau cleanup setelah fitur selesai. Selalu audit source dan git diff terlebih dahulu, pertahankan route/role/data flow, gunakan pola existing project, hindari overengineering, dan jaga kode tetap mudah dibaca developer pemula.
---

# Project Refactor — Java Spring Boot + Vaadin

Gunakan skill ini untuk **refactor**, bukan untuk menambah fitur baru secara diam-diam.

Project utama menggunakan:
- Java 21
- Spring Boot
- Vaadin Flow
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven Wrapper
- Jakarta Validation

Jika source aktual berbeda dari baseline ini, ikuti source aktual dan laporkan perbedaannya.

## Prinsip utama

1. Baca source sebelum menyimpulkan.
2. Pisahkan **SOURCE FACT** dari **ENGINEERING INTERPRETATION**.
3. Jika sesuatu tidak terbukti dari source, tulis `UNKNOWN` atau `NOT PROVEN`.
4. Refactor harus mempertahankan behavior kecuali user secara eksplisit meminta perubahan behavior.
5. Gunakan struktur dan pola yang sudah ada di project sebelum membuat pola baru.
6. Utamakan perubahan paling kecil yang menyelesaikan masalah.
7. Jaga kode tetap mudah dijelaskan ke developer pemula.
8. Jangan memperluas scope karena asumsi sendiri.

Baca [references/project-refactor-rules.md](references/project-refactor-rules.md) ketika perlu detail layer, role, Vaadin, CSS, git, dan checklist project.

# Workflow wajib

## 1. Tentukan mode kerja

**Jika user hanya meminta audit/review:**
- Jangan ubah file.
- Jangan format ulang file.
- Jangan create/delete/rename file.
- Jangan stage, commit, push, restore, reset, checkout, atau clean.
- Berikan temuan berdasarkan source aktual.

**Jika user meminta refactor/implementasi:**
- Lanjutkan workflow di bawah.

## 2. Audit kondisi awal

Sebelum edit:

1. Jalankan `git status`.
2. Catat perubahan yang sudah ada sebelum task.
3. Baca file target dan dependency langsungnya.
4. Cari pemanggil method/class yang akan diubah.
5. Cari route, role, test, CSS selector, service, repository, dan entity yang terkait.
6. Jangan menganggap modified file yang sudah ada sebagai milik task ini.

Jika ada perubahan pre-existing, jangan restore atau memasukkannya ke refactor tanpa instruksi user.

## 3. Nyatakan behavior yang harus tetap

Sebelum edit, identifikasi kontrak yang harus dipertahankan, misalnya:
- URL / `@Route`
- `@RolesAllowed` / akses role
- navigasi Vaadin
- field yang disimpan
- status yang digunakan
- validasi
- duplicate check
- ownership check
- sorting/filtering/pagination
- text/button state yang merupakan behavior penting
- service/repository call

Jika kontrak belum jelas, audit dulu. Jangan refactor dari tebakan.

## 4. Tentukan target refactor minimal

Refactor hanya bagian yang benar-benar bermasalah.

Contoh target yang valid:
- method terlalu panjang → pecah menjadi helper sederhana
- logic bisnis berada di View dan sudah dipakai beberapa tempat → pertimbangkan pindah ke Service
- komponen UI berulang dan kompleks → ekstrak komponen reusable bila benar-benar digunakan ulang
- CSS selector bocor → scope ke halaman/role
- duplikasi formatter sederhana → helper lokal bila meningkatkan keterbacaan
- nama variabel/method membingungkan → rename dengan usage update lengkap

Jangan mengubah arsitektur seluruh project untuk memperbaiki satu method.

## 5. Pilih layer yang tepat

Gunakan alur project sebagai default:

```text
Vaadin View / Component
        ↓
Service
        ↓
Repository
        ↓
Entity / Database
```

Gunakan tanggung jawab berikut:

### View / Component
Taruh:
- layout UI
- event listener
- navigation
- binding nilai ke komponen
- presentational formatting sederhana
- state UI lokal

Jangan pindahkan semua helper UI ke Service.

### Service
Taruh:
- business rule
- ownership validation
- operasi yang mengoordinasikan repository
- logic domain/application yang dipakai lebih dari satu View
- perubahan status/data yang perlu validasi konsisten

Jangan membuat Service baru hanya untuk membungkus satu getter atau satu `repository.save()` tanpa manfaat nyata.

### Repository
Taruh:
- query persistence
- lookup database
- exists/count/find/delete yang memang query data

Jangan taruh UI formatting atau navigation di Repository.

### Entity
Taruh:
- state yang memang dipersist
- relation JPA
- validation yang sesuai model data

Jangan menambah field Entity hanya karena desain UI menampilkan input sementara.

## 6. Pertahankan kesederhanaan

Prefer:
- `if / else`
- `for` loop biasa
- helper `private` kecil
- nama method yang jelas
- dependency injection constructor yang sudah dipakai project
- service/repository existing
- Java collection API sederhana

Hindari kecuali source dan requirement benar-benar membutuhkan:
- Strategy
- Factory
- Facade
- Manager class
- mapper layer baru
- DTO baru
- usecase layer baru
- generic abstraction
- reflection
- nested ternary
- stream chain kompleks
- utility class untuk logic yang hanya dipakai satu tempat

Refactor bukan perlombaan mengurangi jumlah baris.

## 7. Aturan duplikasi

Jangan menghapus setiap duplikasi secara paksa.

Ekstrak shared logic hanya jika:
- behavior memang sama,
- dipakai di beberapa tempat,
- ownership layer-nya jelas,
- hasilnya lebih mudah dibaca daripada duplikasi kecil.

Duplikasi kecil di dua View boleh dipertahankan jika alternatifnya membuat abstraction yang lebih sulit dipahami.

## 8. Aturan Vaadin

Saat refactor View:
- pertahankan `@Route`, `@PageTitle`, dan security annotation kecuali scope meminta perubahan.
- pertahankan parameter route dan `HasUrlParameter` behavior.
- jangan mengubah `MainLayout` untuk memperbaiki satu halaman jika masalah bisa diselesaikan lokal.
- jangan membuat custom navigation framework.
- gunakan komponen Vaadin sederhana yang sudah dipakai project.
- jangan menambah JavaScript bila Vaadin/CSS sudah cukup.

Jika View punya role-specific behavior, audit role aktual sebelum memindah logic.

## 9. Aturan CSS

Untuk refactor styling:
- pertahankan visual yang sudah approved kecuali user meminta redesign.
- scope selector dengan root halaman/role, misalnya `.applicant-*` atau class halaman terkait.
- hindari selector global seperti `button`, `div`, `span`, `h1`, `vaadin-*` tanpa scope.
- jangan merapikan seluruh `styles.css` hanya karena sedang menyentuh satu section.
- hapus selector hanya jika terbukti tidak digunakan.

## 10. Refactor UI-only tidak boleh melebar ke backend

Jika task murni:
- spacing
- layout
- icon
- card composition
- field presentation
- navigation visual

maka jangan otomatis mengubah:
- Entity
- Repository
- database
- migration
- service business logic

Jika perubahan backend memang diperlukan agar behavior target bekerja, jelaskan dulu kenapa dan batasi ke perubahan minimal.

## 11. Refactor backend tidak boleh mengubah kontrak UI diam-diam

Saat memindahkan logic dari View ke Service:
- pertahankan nilai input/output
- pertahankan error/validation behavior yang relevan
- pertahankan duplicate/ownership checks
- pertahankan data mapping
- update semua caller

Jangan mengubah text/UX/status flow sebagai efek samping kecuali diminta.

## 12. Jangan menambah dependency tanpa kebutuhan

Sebelum menambah library:
1. Cek apakah Java/Spring/Vaadin existing sudah menyediakan solusi.
2. Cek dependency project saat ini.
3. Tambahkan dependency hanya jika requirement tidak bisa dipenuhi secara wajar dengan stack existing.

Refactor biasanya tidak membutuhkan dependency baru.

## 13. Lakukan perubahan bertahap

Untuk refactor lebih dari satu concern:
1. Selesaikan satu concern.
2. Pastikan compile/test tetap masuk akal.
3. Lanjut ke concern berikutnya.

Jangan melakukan rename besar, pindah package besar, cleanup CSS besar, dan behavior change sekaligus dalam satu task kecil.

## 14. Verifikasi setelah refactor

Minimal:
1. Cek compile/test dengan Maven Wrapper.
   - Windows PowerShell: `./mvnw.cmd test` atau perintah wrapper yang memang digunakan project.
   - Shell Unix: `./mvnw test`.
2. Jika project/user memakai `./mvnw` atau `mvnw` dengan pola tertentu, ikuti pola existing.
3. Jalankan test relevan bila tersedia.
4. Jalankan `git diff --check` bila memungkinkan.
5. Jalankan `git status` setelah selesai.
6. Bandingkan final status dengan initial status.

Jangan memperbaiki failure yang tidak terkait secara diam-diam.

## 15. Git safety

Tanpa instruksi eksplisit user, jangan:
- `git add .`
- stage file
- commit
- push
- reset
- restore
- checkout
- clean
- rebase
- amend

Jika user meminta commit, stage file secara eksplisit agar perubahan pre-existing yang tidak terkait tidak ikut.

## 16. Format laporan refactor

Gunakan format ringkas berikut bila user tidak memberi format sendiri:

```text
1. Initial Git Status
2. Source behavior yang diverifikasi
3. Masalah yang direfactor
4. Files changed
5. Perubahan per file
6. Behavior yang dipertahankan
7. Hal yang sengaja tidak diubah
8. Test result
9. Final Git Status
10. Scope confirmation
```

Untuk klaim penting, bedakan:
- `SOURCE FACT`
- `ENGINEERING INTERPRETATION`
- `NOT PROVEN`

# Pola refactor yang dianjurkan

## Method terlalu panjang di View

Urutan:
1. Pisahkan pembuatan section UI ke helper `private` yang namanya jelas.
2. Pertahankan event handler dekat dengan UI jika logic-nya hanya UI.
3. Jika handler memuat business rule/persistence kompleks yang dipakai ulang, evaluasi Service.
4. Jangan membuat controller/facade baru hanya untuk mengurangi ukuran View.

## Business rule terduplikasi di beberapa View

Urutan:
1. Pastikan logic benar-benar sama.
2. Pastikan rule bukan presentational concern.
3. Pindahkan rule ke Service existing bila layer tersebut memang pemiliknya.
4. Buat View tetap bertugas mengumpulkan input dan menampilkan feedback.

## Reusable UI

Ekstrak component bila:
- benar-benar digunakan lebih dari satu View, atau
- satu View terlalu sulit dibaca karena blok UI kompleks.

Jangan membuat shared component untuk dua baris layout sederhana.

## Filter / sort / pagination

Pertahankan urutan data flow yang benar:

```text
load data
→ filter
→ sort
→ pagination
→ render
```

Jangan mengubah ke server-side architecture tanpa requirement.

# Definition of done

Refactor dianggap selesai hanya jika:
- source aktual sudah diaudit,
- scope tidak melebar,
- behavior target tetap,
- code lebih mudah dibaca atau boundary lebih jelas,
- tidak ada abstraction yang tidak perlu,
- test/compile relevan lolos atau failure dijelaskan,
- final git status dilaporkan,
- tidak ada commit/push kecuali diminta.
