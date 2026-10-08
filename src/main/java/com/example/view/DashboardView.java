package com.example.view;

import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.example.view.component.ApplicantDashboard;
import com.example.view.component.AdminDashboard;
import com.example.view.component.EmployerDashboard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Job Application Tracker")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final JobApplicationService jobApplicationService;
    private final JobPostingService jobPostingService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public DashboardView(
            JobApplicationService jobApplicationService,
            JobPostingService jobPostingService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.jobApplicationService = jobApplicationService;
        this.jobPostingService = jobPostingService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        addClassName("view-container");
        setWidthFull();

        createDashboard();
    }

    private User getCurrentUser() {
        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }

    private void createDashboard() {
        H2 title = new H2("Dashboard Ringkasan");

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");

        if (isAdmin) {
            // ADMIN: tetap sesuai perilaku existing
            AdminDashboard dashboard = new AdminDashboard();
            List<JobApplication> data = jobApplicationService.findAll();
            dashboard.updateData(data);
            add(title, dashboard);
            return;
        }

        User currentUser = getCurrentUser();

        if (isPemberiLamaran) {
            // PEMBERI_LAMARAN: Dashboard dinamis khusus Employer
            EmployerDashboard employerDashboard = new EmployerDashboard();
            List<JobPosting> jobPostings = jobPostingService.findByEmployer(currentUser);
            List<JobApplication> applications = jobApplicationService.findByEmployer(currentUser);
            employerDashboard.updateData(currentUser, jobPostings, applications);
            add(employerDashboard);
        } else {
            // PELAMAR: Dashboard khusus Pelamar berdasarkan Stitch design
            ApplicantDashboard applicantDashboard = new ApplicantDashboard();
            List<JobApplication> applications = jobApplicationService.findByApplicant(currentUser);
            applicantDashboard.updateData(currentUser, applications);
            add(applicantDashboard);
        }
    }
}
