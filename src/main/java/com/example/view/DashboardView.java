package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.JobApplicationRepository;
import com.example.repository.JobPostingRepository;
import com.example.repository.UserRepository;
import com.example.service.CompanyVerificationService;
import com.example.service.EmployerSettingsService;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.example.view.component.ApplicantDashboard;
import com.example.view.component.AdminDashboard;
import com.example.view.component.EmployerDashboard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
    private final EmployerSettingsService employerSettingsService;
    private final CompanyRepository companyRepository;
    private final CompanyVerificationService companyVerificationService;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public DashboardView(
            JobApplicationService jobApplicationService,
            JobPostingService jobPostingService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository,
            EmployerSettingsService employerSettingsService,
            CompanyRepository companyRepository,
            CompanyVerificationService companyVerificationService,
            JobPostingRepository jobPostingRepository,
            JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationService = jobApplicationService;
        this.jobPostingService = jobPostingService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;
        this.employerSettingsService = employerSettingsService;
        this.companyRepository = companyRepository;
        this.companyVerificationService = companyVerificationService;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;

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
        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");

        if (isAdmin) {
            AdminDashboard dashboard = new AdminDashboard();
            long platformUsers = userRepository.count();
            long registeredCompanies = companyRepository.count();
            List<Company> pendingCompanies = companyVerificationService.getPendingCompanies();
            long pendingVerification = pendingCompanies.size();
            long totalJobPostings = jobPostingRepository.count();
            long activeJobPostings = jobPostingService.findActiveJobPostings().size();
            long totalApplications = jobApplicationRepository.count();

            dashboard.updateData(
                    platformUsers,
                    registeredCompanies,
                    pendingVerification,
                    totalJobPostings,
                    activeJobPostings,
                    totalApplications,
                    pendingCompanies,
                    companyVerificationService
            );
            add(dashboard);
            return;
        }

        User currentUser = getCurrentUser();

        if (isPemberiLamaran) {
            Company company = currentUser.getCompany();

            // 1. Cek keberadaan Company
            if (company == null) {
                showNoCompanyState();
                return;
            }

            CompanyVerificationStatus verificationStatus = company.getVerificationStatus();

            // 2. Cek Company PENDING
            if (verificationStatus == CompanyVerificationStatus.PENDING) {
                showPendingVerificationState(company);
                return;
            }

            // 3. Cek Company REJECTED
            if (verificationStatus == CompanyVerificationStatus.REJECTED) {
                showCompanyRejectedState(currentUser, company);
                return;
            }

            // 4. Fail-closed untuk status verification selain VERIFIED
            if (verificationStatus != CompanyVerificationStatus.VERIFIED) {
                showNonVerifiedState(company);
                return;
            }

            // 5. Cek Membership Status (hanya dievaluasi jika Company sudah VERIFIED)
            CompanyMembershipStatus membershipStatus = currentUser.getMembershipStatus();

            if (membershipStatus == CompanyMembershipStatus.PENDING) {
                showMembershipPendingState(company);
                return;
            }

            if (membershipStatus == CompanyMembershipStatus.REJECTED) {
                showMembershipRejectedState(company);
                return;
            }

            if (membershipStatus != CompanyMembershipStatus.ACTIVE) {
                showMembershipNonActiveState(company);
                return;
            }

            // 6. Hanya jika Company VERIFIED dan Membership ACTIVE
            EmployerDashboard employerDashboard = new EmployerDashboard();
            List<JobPosting> jobPostings = jobPostingService.findByCompany(company);
            List<JobApplication> applications = jobApplicationService.findByCompany(company);
            employerDashboard.updateData(currentUser, jobPostings, applications);
            add(employerDashboard);
            return;
        }

        ApplicantDashboard applicantDashboard = new ApplicantDashboard();
        List<JobApplication> applications = jobApplicationService.findByApplicant(currentUser);
        applicantDashboard.updateData(currentUser, applications);
        add(applicantDashboard);
    }

    private void showPendingVerificationState(Company company) {
        Div card = createBaseStatusCard();

        Div iconWrapper = createIconWrapper("#FEF3C7");
        Icon clockIcon = VaadinIcon.CLOCK.create();
        clockIcon.setSize("24px");
        clockIcon.setColor("#D97706");
        iconWrapper.add(clockIcon);

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Company Verification: PENDING", "#FEF3C7", "#92400E");

        H2 title = new H2("Company Verification in Progress");
        applyTitleStyle(title);

        Paragraph subtitle = new Paragraph("Your company is currently under verification.");
        applySubtitleStyle(subtitle);

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
        applyDescStyle(desc);

        card.add(iconWrapper, companyMeta, title, subtitle, reviewBox, desc);
        add(card);
    }

    private void showCompanyRejectedState(User currentUser, Company company) {
        Div card = createBaseStatusCard();

        Div iconWrapper = createIconWrapper("#FEE2E2");
        Icon closeIcon = VaadinIcon.CLOSE_CIRCLE.create();
        closeIcon.setSize("24px");
        closeIcon.setColor("#DC2626");
        iconWrapper.add(closeIcon);

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Company Verification: REJECTED", "#FEE2E2", "#991B1B");

        H2 title = new H2("Company Verification Rejected");
        applyTitleStyle(title);

        Paragraph subtitle = new Paragraph("Perusahaan Anda tidak lolos verifikasi dari administrator aplikasi.");
        applySubtitleStyle(subtitle);

        Div reasonBox = new Div();
        reasonBox.getStyle()
                .set("background-color", "#FEF2F2")
                .set("border", "1px solid #FECACA")
                .set("border-radius", "8px")
                .set("padding", "14px 16px")
                .set("margin-bottom", "16px");

        Span reasonLabel = new Span("Rejection Reason:");
        reasonLabel.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("color", "#991B1B")
                .set("display", "block")
                .set("margin-bottom", "4px");

        String reasonContent = company.getRejectionReason();
        if (reasonContent == null || reasonContent.trim().isEmpty()) {
            reasonContent = "Alasan penolakan tidak dicantumkan oleh administrator.";
        }

        Paragraph reasonText = new Paragraph(reasonContent);
        reasonText.getStyle()
                .set("margin", "0")
                .set("font-size", "13px")
                .set("color", "#7F1D1D")
                .set("line-height", "1.5");

        reasonBox.add(reasonLabel, reasonText);

        boolean canEdit = employerSettingsService.canEditCompanyProfile(currentUser);

        Paragraph guidance = new Paragraph();
        applyDescStyle(guidance);

        if (canEdit) {
            guidance.setText("Silakan perbaiki informasi perusahaan sesuai alasan penolakan di atas. Setelah data diperbarui, Anda dapat mengajukan ulang perusahaan untuk diverifikasi.");

            Button editProfileButton = new Button("Perbaiki Company Profile", event -> UI.getCurrent().navigate(EmployerSettingsView.class));
            editProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editProfileButton.getStyle().set("margin-top", "16px");

            card.add(iconWrapper, companyMeta, title, subtitle, reasonBox, guidance, editProfileButton);
        } else {
            guidance.setText("Profil perusahaan perlu diperbaiki sebelum verifikasi dapat diajukan kembali. Silakan hubungi Company Admin.");
            card.add(iconWrapper, companyMeta, title, subtitle, reasonBox, guidance);
        }

        add(card);
    }

    private void showMembershipPendingState(Company company) {
        Div card = createBaseStatusCard();

        Div iconWrapper = createIconWrapper("#FEF3C7");
        Icon clockIcon = VaadinIcon.CLOCK.create();
        clockIcon.setSize("24px");
        clockIcon.setColor("#D97706");
        iconWrapper.add(clockIcon);

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Membership Status: PENDING", "#FEF3C7", "#92400E");

        H2 title = new H2("Membership Approval Pending");
        applyTitleStyle(title);

        Paragraph subtitle = new Paragraph("Your request to join this company is waiting for approval.");
        applySubtitleStyle(subtitle);

        Paragraph desc = new Paragraph("Please wait for approval from the company administrator. You will be able to access the Employer area once your membership is activated.");
        applyDescStyle(desc);

        card.add(iconWrapper, companyMeta, title, subtitle, desc);
        add(card);
    }

    private void showMembershipRejectedState(Company company) {
        Div card = createBaseStatusCard();

        Div iconWrapper = createIconWrapper("#FEE2E2");
        Icon closeIcon = VaadinIcon.CLOSE_CIRCLE.create();
        closeIcon.setSize("24px");
        closeIcon.setColor("#DC2626");
        iconWrapper.add(closeIcon);

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Membership Status: REJECTED", "#FEE2E2", "#991B1B");

        H2 title = new H2("Membership Request Rejected");
        applyTitleStyle(title);

        Paragraph subtitle = new Paragraph("Your request to join this company was rejected.");
        applySubtitleStyle(subtitle);

        Paragraph desc = new Paragraph("You currently do not have access to the Employer area. Silakan hubungi administrator perusahaan terkait.");
        applyDescStyle(desc);

        card.add(iconWrapper, companyMeta, title, subtitle, desc);
        add(card);
    }

    private void showMembershipNonActiveState(Company company) {
        Div card = createBaseStatusCard();

        Div iconWrapper = createIconWrapper("#F1F5F9");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("24px");
        infoIcon.setColor("#475569");
        iconWrapper.add(infoIcon);

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Membership Status: INACTIVE", "#F1F5F9", "#475569");

        H2 title = new H2("Status Keanggotaan Belum Aktif");
        applyTitleStyle(title);

        Paragraph desc = new Paragraph("Akun Anda belum memiliki akses aktif ke perusahaan ini. Silakan hubungi administrator perusahaan untuk mengaktifkan keanggotaan Anda.");
        applyDescStyle(desc);

        card.add(iconWrapper, companyMeta, title, desc);
        add(card);
    }

    private void showNoCompanyState() {
        Div card = createBaseStatusCard();

        H3 title = new H3("Data Perusahaan Tidak Ditemukan");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("color", "#0F172A");

        Paragraph msg = new Paragraph("Akun Anda belum terhubung dengan data perusahaan. Silakan hubungi administrator.");
        applyDescStyle(msg);

        card.add(title, msg);
        add(card);
    }

    private void showNonVerifiedState(Company company) {
        Div card = createBaseStatusCard();

        Div companyMeta = createCompanyMetaHeader(company.getName(), "Verification Status: UNVERIFIED", "#F1F5F9", "#475569");

        H3 title = new H3("Akses Belum Tersedia");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("color", "#0F172A");

        Paragraph msg = new Paragraph("Perusahaan Anda belum terverifikasi untuk mengakses area employer.");
        applyDescStyle(msg);

        card.add(companyMeta, title, msg);
        add(card);
    }

    private Div createBaseStatusCard() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "12px")
                .set("padding", "32px")
                .set("max-width", "560px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.05)");
        return card;
    }

    private Div createIconWrapper(String bgColor) {
        Div iconWrapper = new Div();
        iconWrapper.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "50%")
                .set("background-color", bgColor)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "16px");
        return iconWrapper;
    }

    private Div createCompanyMetaHeader(String companyName, String statusBadgeText, String badgeBgColor, String badgeTextColor) {
        Div metaLayout = new Div();
        metaLayout.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("margin-bottom", "12px");

        Span companyNameSpan = new Span(companyName);
        companyNameSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#0F172A");

        Span statusBadge = new Span(statusBadgeText);
        statusBadge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "2px 8px")
                .set("border-radius", "4px")
                .set("background-color", badgeBgColor)
                .set("color", badgeTextColor);

        metaLayout.add(companyNameSpan, statusBadge);
        return metaLayout;
    }

    private void applyTitleStyle(H2 title) {
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#0F172A");
    }

    private void applySubtitleStyle(Paragraph subtitle) {
        subtitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "14px")
                .set("color", "#64748B");
    }

    private void applyDescStyle(Paragraph desc) {
        desc.getStyle()
                .set("margin", "0")
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("line-height", "1.5");
    }
}
