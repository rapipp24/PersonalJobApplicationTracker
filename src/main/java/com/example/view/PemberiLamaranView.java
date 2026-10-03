package com.example.view;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobPostingService;
import com.example.entity.JobPosting;
import com.example.view.component.JobPostingForm;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "pemberi-lamaran", layout = MainLayout.class)
@PageTitle("Area Pemberi Lamaran | Job Tracker")
@RolesAllowed({"PEMBERI_LAMARAN", "ADMIN"})

public class PemberiLamaranView extends VerticalLayout {

    private final JobPostingService jobPostingService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private final JobPostingForm form = new JobPostingForm();

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

            JobPosting jobPosting = new JobPosting();
            jobPosting.setEmployer(getCurrentUser());
            form.setJobPosting(jobPosting);

            form.setSaveListener(posting -> {
                jobPostingService.simpanLowongan(posting);
            });
        }

        if (isAdmin) {
            description.setText(
                    "Kelola seluruh lowongan pekerjaan sebagai Admin."
            );
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

        add(backButton);

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
}