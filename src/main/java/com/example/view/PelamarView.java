package com.example.view;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.example.view.component.JobPostingGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;

@Route(value = "pelamar", layout = MainLayout.class)
@PageTitle("Applicant Area | Job Tracker")
@RolesAllowed({"PELAMAR", "ADMIN"})
public class PelamarView extends VerticalLayout {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private final JobPostingGrid tableLowongan = new JobPostingGrid();

    public PelamarView(
            JobPostingService jobPostingService,
            JobApplicationService jobApplicationService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        H1 title = new H1("Applicant Area");
        Paragraph description = new Paragraph("Lowongan yang tersedia");

        boolean isPelamar = authenticationContext.hasRole("PELAMAR");
        if (isPelamar) {
            tableLowongan.addComponentColumn(this::createApplyButton)
                    .setHeader("Aksi");
        }

        List<JobPosting> lowonganAktif = jobPostingService.findActiveJobPostings();
        tableLowongan.setItems(lowonganAktif);

        add(title, description, tableLowongan);
    }

    private User getCurrentUser() {
        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }

    private Button createApplyButton(JobPosting jobPosting) {
        User currentUser = getCurrentUser();
        boolean sudahMelamar = jobApplicationService.hasApplied(currentUser, jobPosting);

        Button applyButton = new Button("Apply");
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        if (sudahMelamar) {
            applyButton.setText("Sudah Dilamar");
            applyButton.setEnabled(false);
            return applyButton;
        }

        applyButton.addClickListener(event -> {
            if (!jobPosting.isActive()) {
                Notification.show("Lowongan ini sudah tidak aktif");
                return;
            }

            if (jobApplicationService.hasApplied(currentUser, jobPosting)) {
                Notification.show("Anda sudah melamar lowongan ini");
                applyButton.setText("Sudah Dilamar");
                applyButton.setEnabled(false);
                return;
            }

            JobApplication jobApplication = new JobApplication();
            jobApplication.setApplicant(currentUser);
            jobApplication.setJobPosting(jobPosting);
            jobApplication.setCompanyName(jobPosting.getCompanyName());
            jobApplication.setPosition(jobPosting.getPosition());
            jobApplication.settipeKerja(jobPosting.getTipeKerja());
            jobApplication.setApplicationDate(LocalDate.now());
            jobApplication.setStatus(ApplicationStatus.APPLIED);

            jobApplicationService.simpanLamaran(jobApplication);

            applyButton.setText("Sudah Dilamar");
            applyButton.setEnabled(false);

            Notification.show("Lamaran berhasil dikirim");
        });

        return applyButton;
    }
}

