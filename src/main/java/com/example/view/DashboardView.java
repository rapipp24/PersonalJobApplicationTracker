package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.example.view.component.ApplicantDashboard;
import com.example.view.component.AdminDashboard;
import com.example.view.component.EmployerDashboard;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
            AdminDashboard dashboard = new AdminDashboard();
            List<JobApplication> data = jobApplicationService.findAll();
            dashboard.updateData(data);
            add(title, dashboard);
            return;
        }

        User currentUser = getCurrentUser();

        if (isPemberiLamaran) {
            Company company = currentUser.getCompany();

            if (company == null) {
                showNoCompanyState();
                return;
            }

            CompanyVerificationStatus status = company.getVerificationStatus();

            if (status == CompanyVerificationStatus.PENDING) {
                showPendingVerificationState();
                return;
            }

            if (status != CompanyVerificationStatus.VERIFIED) {
                showNonVerifiedState();
                return;
            }

            EmployerDashboard employerDashboard = new EmployerDashboard();
            List<JobPosting> jobPostings = jobPostingService.findByEmployer(currentUser);
            List<JobApplication> applications = jobApplicationService.findByEmployer(currentUser);
            employerDashboard.updateData(currentUser, jobPostings, applications);
            add(employerDashboard);
            return;
        }

        ApplicantDashboard applicantDashboard = new ApplicantDashboard();
        List<JobApplication> applications = jobApplicationService.findByApplicant(currentUser);
        applicantDashboard.updateData(currentUser, applications);
        add(applicantDashboard);
    }

    private void showPendingVerificationState() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "12px")
                .set("padding", "32px")
                .set("max-width", "560px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.05)");

        Div iconWrapper = new Div();
        iconWrapper.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "50%")
                .set("background-color", "#FEF3C7")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "16px");

        Icon clockIcon = VaadinIcon.CLOCK.create();
        clockIcon.setSize("24px");
        clockIcon.setColor("#D97706");
        iconWrapper.add(clockIcon);

        H2 title = new H2("Company Verification in Progress");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#0F172A");

        Paragraph subtitle = new Paragraph("Your company is currently under verification.");
        subtitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        Div reviewBox = new Div();
        reviewBox.getStyle()
                .set("background-color", "#F8FAFC")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "8px")
                .set("padding", "12px 16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("margin-bottom", "16px");

        Icon boxIcon = VaadinIcon.HOURGLASS.create();
        boxIcon.setSize("16px");
        boxIcon.setColor("#64748B");

        Span reviewText = new Span("Estimated Review Time: 1–3 Business Days");
        reviewText.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", "#334155");

        reviewBox.add(boxIcon, reviewText);

        Paragraph desc = new Paragraph("Please wait for approval from the application administrator. No further action is required at this time.");
        desc.getStyle()
                .set("margin", "0")
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        card.add(iconWrapper, title, subtitle, reviewBox, desc);
        add(card);
    }

    private void showNoCompanyState() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "8px")
                .set("padding", "24px")
                .set("max-width", "560px");

        H3 title = new H3("Data Perusahaan Tidak Ditemukan");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("color", "#0F172A");

        Paragraph msg = new Paragraph("Akun Anda belum terhubung dengan data perusahaan. Silakan hubungi administrator.");
        msg.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        card.add(title, msg);
        add(card);
    }

    private void showNonVerifiedState() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "8px")
                .set("padding", "24px")
                .set("max-width", "560px");

        H3 title = new H3("Akses Belum Tersedia");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("color", "#0F172A");

        Paragraph msg = new Paragraph("Perusahaan Anda belum terverifikasi untuk mengakses area employer.");
        msg.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        card.add(title, msg);
        add(card);
    }
}
