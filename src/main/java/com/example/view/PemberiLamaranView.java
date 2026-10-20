package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobPostingService;
import com.example.view.component.JobPostingForm;
import com.example.view.component.JobPostingGrid;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.component.notification.Notification;


import jakarta.annotation.security.RolesAllowed;

@Route(value = "pemberi-lamaran", layout = MainLayout.class)
@PageTitle("Area Pemberi Lamaran | Job Tracker")
@RolesAllowed({"PEMBERI_LAMARAN", "ADMIN"})

public class PemberiLamaranView extends VerticalLayout {

    private final JobPostingService jobPostingService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private final JobPostingForm form = new JobPostingForm();
    private final JobPostingGrid tableLowongan = new JobPostingGrid();

    public PemberiLamaranView(
            JobPostingService jobPostingService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.jobPostingService = jobPostingService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        boolean isAdmin =
                authenticationContext.hasRole("ADMIN");

        boolean isPemberiLamaran =
                authenticationContext.hasRole("PEMBERI_LAMARAN");

        H1 title = new H1("Area Pemberi Lamaran");

        Paragraph description = new Paragraph(
                "Halaman ini hanya dapat diakses oleh Pemberi Lamaran dan Admin."
        );

        if (isPemberiLamaran) {
            description.setText(
                    "Kelola lowongan pekerjaan yang Anda buat."
            );

            User currentUser = getCurrentUser();

            // Guard: PEMBERI_LAMARAN lama yang belum mempunyai Company
            // tidak diizinkan membuat lowongan baru untuk menghindari companyName kosong.
            if (currentUser.getCompany() == null) {
                Paragraph noCompanyInfo = new Paragraph(
                        "Akun Anda belum terhubung dengan data perusahaan. "
                        + "Silakan hubungi administrator."
                );

                Button backButton = new Button(
                        "Kembali ke Halaman Utama",
                        event -> UI.getCurrent().navigate("")
                );

                tableLowongan.addStatusColumn();

                tableLowongan.setItems(
                        jobPostingService.findByEmployer(currentUser)
                );

                add(title, description, noCompanyInfo, tableLowongan, backButton);
                return;
            }

            Company company = currentUser.getCompany();
            CompanyVerificationStatus status = company.getVerificationStatus();

            // Guard: Company berstatus PENDING belum boleh mengakses fitur operasional lowongan
            if (status == CompanyVerificationStatus.PENDING) {
                Paragraph pendingInfo = new Paragraph(
                        "Perusahaan Anda sedang dalam proses verifikasi. "
                        + "Fitur pengelolaan lowongan pekerjaan belum dapat diakses."
                );

                Button backButton = new Button(
                        "Kembali ke Halaman Utama",
                        event -> UI.getCurrent().navigate("")
                );

                add(title, pendingInfo, backButton);
                return;
            }

            // Guard: Fail-closed untuk status non-VERIFIED lainnya (REJECTED atau null)
            if (status != CompanyVerificationStatus.VERIFIED) {
                Paragraph nonVerifiedInfo = new Paragraph(
                        "Perusahaan Anda belum terverifikasi. "
                        + "Fitur pengelolaan lowongan pekerjaan belum dapat diakses."
                );

                Button backButton = new Button(
                        "Kembali ke Halaman Utama",
                        event -> UI.getCurrent().navigate("")
                );

                add(title, nonVerifiedInfo, backButton);
                return;
            }

            // Guard: Membership harus ACTIVE untuk mengakses fitur operasional lowongan
            CompanyMembershipStatus membershipStatus = currentUser.getMembershipStatus();
            if (membershipStatus != CompanyMembershipStatus.ACTIVE) {
                Paragraph membershipInfo = new Paragraph(
                        "Status keanggotaan perusahaan Anda belum aktif. "
                        + "Fitur pengelolaan lowongan pekerjaan belum dapat diakses."
                );

                Button backButton = new Button(
                        "Kembali ke Halaman Utama",
                        event -> UI.getCurrent().navigate("")
                );

                add(title, membershipInfo, backButton);
                return;
            }

            configureEmployerForm();
            configureEmployerGrid();

            resetForm();
            refreshTableLowongan();
        }

        if (isAdmin) {
            description.setText(
                    "Kelola seluruh lowongan pekerjaan sebagai Admin."
            );

            configureAdminGrid();
            refreshTableLowongan();
        }

        Button backButton = new Button(
                "Kembali ke Halaman Utama",
                event -> UI.getCurrent().navigate("")
        );

        add(
                title,
                description
        );

        if (isPemberiLamaran) {
            add(form);
        }

        add(tableLowongan, backButton);

    }

    private void configureEmployerForm() {
        form.setSaveListener(this::handleSave);
        form.setCancelListener(this::handleCancel);
        form.setDeleteListener(this::handleDelete);
        form.setViewApplicantsListener(this::handleViewApplicants);
    }

    private void configureEmployerGrid() {
        User currentUser = getCurrentUser();

        tableLowongan.addItemClickListener(event -> {
            JobPosting selectedJobPosting = event.getItem();
            if (!canManageJobPosting(selectedJobPosting, currentUser)) {
                Notification.show("Hanya pembuat lowongan atau Admin Perusahaan yang dapat mengedit lowongan ini.");
                resetForm();
                return;
            }
            form.setJobPosting(selectedJobPosting);
        });

        tableLowongan.addStatusColumn();

        tableLowongan.addComponentColumn(jobPosting -> {
            if (!canManageJobPosting(jobPosting, currentUser)) {
                Span noAction = new Span("-");
                return noAction;
            }

            Button actionButton = new Button();
            actionButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

            if (jobPosting.isActive()) {
                actionButton.setText("Deactivate");
                actionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            } else {
                actionButton.setText("Activate");
                actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }

            actionButton.addClickListener(event -> handleToggleActive(jobPosting));
            return actionButton;
        }).setHeader("Aksi");

        tableLowongan.addApplicantsActionColumn(this::handleViewApplicants);
    }

    private void handleViewApplicants(JobPosting posting) {
        if (posting != null && posting.getId() != null) {
            UI.getCurrent().navigate(JobApplicantsView.class, posting.getId());
        }
    }

    private void configureAdminGrid() {
        tableLowongan.addEmployerColumn();
        tableLowongan.addStatusColumn();
    }

    private void handleSave(JobPosting posting) {
        User currentUser = getCurrentUser();

        if (posting.getId() != null && !canManageJobPosting(posting, currentUser)) {
            Notification.show("Anda tidak memiliki hak untuk mengubah lowongan ini.");
            return;
        }

        try {
            jobPostingService.simpanLowongan(posting, currentUser);
            Notification.show("Lowongan berhasil disimpan");
            resetForm();
            refreshTableLowongan();
        } catch (SecurityException se) {
            Notification.show("Akses ditolak: " + se.getMessage());
        } catch (Exception ex) {
            Notification.show("Gagal menyimpan lowongan: " + ex.getMessage());
        }
    }

    private void handleCancel() {
        resetForm();
        refreshTableLowongan();
    }

    private void handleDelete(JobPosting posting) {
        if (posting == null || posting.getId() == null) {
            Notification.show("Pilih lowongan yang ingin dihapus terlebih dahulu.");
            return;
        }

        User currentUser = getCurrentUser();

        // Validasi hak pengelolaan lowongan
        if (!canManageJobPosting(posting, currentUser)) {
            Notification.show("Anda tidak memiliki hak untuk menghapus lowongan ini.");
            return;
        }

        // Validasi apakah lowongan sudah pernah dilamar
        if (jobPostingService.hasApplications(posting)) {
            Notification.show(
                    "Lowongan ini tidak dapat dihapus karena sudah memiliki lamaran. "
                    + "Nonaktifkan lowongan jika tidak ingin menerima lamaran baru."
            );
            return;
        }

        // Konfirmasi sebelum menghapus secara permanen
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Hapus Lowongan");
        dialog.setText("Lowongan " + posting.getPosition() + " akan dihapus permanen.");
        dialog.setCancelable(true);
        dialog.setCancelText("Batal");
        dialog.setConfirmText("Hapus");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            jobPostingService.hapusLowongan(posting, currentUser);
            Notification.show("Lowongan berhasil dihapus.");
            resetForm();
            refreshTableLowongan();
        });

        dialog.open();
    }

    private User getCurrentUser() {

        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() ->
                        new IllegalStateException("User belum login")
                );

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("User tidak ditemukan")
                );
    }

    private void resetForm() {
        User currentUser = getCurrentUser();

        JobPosting jobPosting = new JobPosting();
        jobPosting.setEmployer(currentUser);

        if (currentUser.getCompany() != null) {
            jobPosting.setCompany(currentUser.getCompany());
            jobPosting.setCompanyName(currentUser.getCompany().getName());
        }

        form.setJobPosting(jobPosting);
    }

    private void refreshTableLowongan() {

        boolean isAdmin =
                authenticationContext.hasRole("ADMIN");

        boolean isPemberiLamaran =
                authenticationContext.hasRole("PEMBERI_LAMARAN");

        if (isPemberiLamaran) {
            User currentUser = getCurrentUser();
            tableLowongan.setItems(
                    jobPostingService.findByCompany(currentUser.getCompany())
            );
        }

        if (isAdmin) {
            tableLowongan.setItems(
                    jobPostingService.findAll()
            );
        }
    }

    private void handleToggleActive(JobPosting jobPosting) {
        User currentUser = getCurrentUser();
        if (!canManageJobPosting(jobPosting, currentUser)) {
            Notification.show("Anda tidak memiliki hak untuk mengubah status lowongan ini.");
            return;
        }

        if (jobPosting.isActive()) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Nonaktifkan Lowongan");
            dialog.setText("Anda yakin ingin menonaktifkan lowongan " + jobPosting.getPosition() + "? Lowongan ini tidak akan muncul lagi untuk pelamar.");
            dialog.setCancelable(true);
            dialog.setCancelText("Batal");
            dialog.setConfirmText("Nonaktifkan");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(event -> {
                jobPosting.setActive(false);
                jobPostingService.simpanLowongan(jobPosting, currentUser);
                Notification.show("Job posting dinonaktifkan.");
                resetForm();
                refreshTableLowongan();
            });

            dialog.open();
        } else {
            jobPosting.setActive(true);
            jobPostingService.simpanLowongan(jobPosting, currentUser);
            Notification.show("Job posting diaktifkan.");
            resetForm();
            refreshTableLowongan();
        }
    }

    private boolean canManageJobPosting(JobPosting jobPosting, User currentUser) {
        if (jobPosting == null || currentUser == null) {
            return false;
        }
        if (jobPosting.getEmployer() != null && currentUser.getId() != null
                && jobPosting.getEmployer().getId().equals(currentUser.getId())) {
            return true;
        }
        return currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN;
    }
}
