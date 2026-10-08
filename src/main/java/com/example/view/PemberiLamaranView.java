package com.example.view;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobPostingService;
import com.example.entity.JobPosting;
import com.example.view.component.JobPostingForm;
import com.example.view.component.JobPostingGrid;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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
        tableLowongan.addItemClickListener(event -> {
            JobPosting selectedJobPosting = event.getItem();
            form.setJobPosting(selectedJobPosting);
        });

        tableLowongan.addStatusColumn();
        tableLowongan.addActiveActionColumn(this::handleToggleActive);
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
        jobPostingService.simpanLowongan(posting);
        Notification.show("Lowongan berhasil disimpan");
        resetForm();
        refreshTableLowongan();
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

        // Validasi kepemilikan lowongan
        if (posting.getEmployer() == null || !posting.getEmployer().getId().equals(currentUser.getId())) {
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
            tableLowongan.setItems(
                    jobPostingService.findByEmployer(getCurrentUser())
            );
        }

        if (isAdmin) {
            tableLowongan.setItems(
                    jobPostingService.findAll()
            );
        }
    }

    private void handleToggleActive(JobPosting jobPosting) {
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
                jobPostingService.simpanLowongan(jobPosting);
                Notification.show("Job posting dinonaktifkan.");
                resetForm();
                refreshTableLowongan();
            });

            dialog.open();
        } else {
            jobPosting.setActive(true);
            jobPostingService.simpanLowongan(jobPosting);
            Notification.show("Job posting diaktifkan.");
            resetForm();
            refreshTableLowongan();
        }
    }
}
