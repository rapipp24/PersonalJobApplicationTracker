package com.example.view;

import com.example.entity.ApplicantProfile;
import com.example.entity.ApplicationStatus;
import com.example.entity.ApplicationTipe;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.ApplicantProfileService;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Route(value = "pelamar/job/apply", layout = MainLayout.class)
@PageTitle("Apply for Job | Job Application Tracker")
@RolesAllowed("PELAMAR")
public class ApplyJobView extends VerticalLayout implements HasUrlParameter<Long> {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;
    private final ApplicantProfileService applicantProfileService;

    private JobPosting currentJob;
    private User currentUser;

    private TextField phoneField;
    private TextField linkedInField;
    private TextField portfolioField;
    private String uploadedFileName;
    private Div fileBadgeContainer;

    public ApplyJobView(
            JobPostingService jobPostingService,
            JobApplicationService jobApplicationService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository,
            ApplicantProfileService applicantProfileService) {
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;
        this.applicantProfileService = applicantProfileService;

        addClassName("applicant-apply-page");
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }

    @Override
    public void setParameter(BeforeEvent event, Long jobPostingId) {
        if (jobPostingId == null) {
            event.forwardTo(PelamarView.class);
            return;
        }

        Optional<JobPosting> postingOptional = jobPostingService.findById(jobPostingId);
        if (postingOptional.isEmpty()) {
            event.forwardTo(PelamarView.class);
            return;
        }

        JobPosting job = postingOptional.get();
        if (!job.isActive()) {
            event.forwardTo(PelamarView.class);
            return;
        }

        User user = getCurrentUser();
        if (jobApplicationService.hasApplied(user, job)) {
            event.forwardTo(JobDetailView.class, jobPostingId);
            return;
        }

        this.currentJob = job;
        this.currentUser = user;
        this.uploadedFileName = null;

        removeAll();
        buildView();
    }

    private void buildView() {
        if (currentJob == null || currentUser == null) {
            return;
        }

        createNavigationHeader();
        createPageTitle();
        createMainGrid();
    }

    private void createNavigationHeader() {
        Div navRow = new Div();
        navRow.addClassName("applicant-apply-nav-row");

        // 1. Back Action: lucide:arrow-left returning to JobDetailView (icon-only)
        Html backIcon = new Html("<iconify-icon icon=\"lucide:arrow-left\" class=\"applicant-apply-back-icon\"></iconify-icon>");
        Button backButton = new Button(backIcon, event -> UI.getCurrent().navigate(JobDetailView.class, currentJob.getId()));
        backButton.addClassName("applicant-apply-back-btn");
        backButton.setAriaLabel("Back to Job Details");
        backButton.setTooltipText("Back to Job Details");

        // 2. Breadcrumb: Browse Jobs / Company / Position / Apply
        Div breadcrumb = new Div();
        breadcrumb.addClassName("applicant-apply-breadcrumb");

        Span browseLink = new Span("Browse Jobs");
        browseLink.addClassName("applicant-apply-breadcrumb-link");
        browseLink.addClickListener(event -> UI.getCurrent().navigate(PelamarView.class));

        Span sep1 = new Span("/");
        sep1.addClassName("applicant-apply-breadcrumb-sep");

        String compName = currentJob.getCompanyName() != null && !currentJob.getCompanyName().isBlank()
                ? currentJob.getCompanyName()
                : "-";
        Span companySpan = new Span(compName);
        companySpan.addClassName("applicant-apply-breadcrumb-item");

        Span sep2 = new Span("/");
        sep2.addClassName("applicant-apply-breadcrumb-sep");

        String posName = currentJob.getPosition() != null && !currentJob.getPosition().isBlank()
                ? currentJob.getPosition()
                : "-";
        Span positionSpan = new Span(posName);
        positionSpan.addClassName("applicant-apply-breadcrumb-link");
        positionSpan.addClickListener(event -> UI.getCurrent().navigate(JobDetailView.class, currentJob.getId()));

        Span sep3 = new Span("/");
        sep3.addClassName("applicant-apply-breadcrumb-sep");

        Span currentSpan = new Span("Apply");
        currentSpan.addClassName("applicant-apply-breadcrumb-current");

        breadcrumb.add(browseLink, sep1, companySpan, sep2, positionSpan, sep3, currentSpan);

        navRow.add(backButton, breadcrumb);
        add(navRow);
    }

    private void createPageTitle() {
        Div titleBlock = new Div();
        titleBlock.addClassName("applicant-apply-title-block");

        H1 title = new H1("Submit Your Application");
        title.addClassName("applicant-apply-title");

        String position = currentJob.getPosition() != null ? currentJob.getPosition() : "this role";
        Paragraph subtitle = new Paragraph("Review your information before submitting your application for " + position + ".");
        subtitle.addClassName("applicant-apply-subtitle");

        titleBlock.add(title, subtitle);
        add(titleBlock);
    }

    private void createMainGrid() {
        Div grid = new Div();
        grid.addClassName("applicant-apply-grid");

        Div mainContent = createMainContent();
        Div jobSummary = createJobSummary();

        grid.add(mainContent, jobSummary);
        add(grid);
    }

    private Div createMainContent() {
        Div content = new Div();
        content.addClassName("applicant-apply-content");

        // SECTION 1: Applicant Identity & Contact
        Div section1 = new Div();
        section1.addClassName("applicant-apply-section");

        Div sec1Header = new Div();
        sec1Header.addClassName("applicant-apply-sec-header");

        Span numBadge1 = new Span("1");
        numBadge1.addClassName("applicant-apply-step-badge");

        H2 sec1Title = new H2("Applicant Identity & Contact");
        sec1Title.addClassName("applicant-apply-sec-title");

        sec1Header.add(numBadge1, sec1Title);

        Paragraph sec1Subtitle = new Paragraph("Review your personal information before submitting your application.");
        sec1Subtitle.addClassName("applicant-apply-sec-subtitle");

        // 2-Column Field Grid
        Div fieldGrid = new Div();
        fieldGrid.addClassName("applicant-apply-field-grid");

        // 1. Full Name (Locked)
        Div nameWrapper = createFieldWrapper(
                "Full Name *",
                "Locked",
                "applicant-apply-badge-locked",
                createReadOnlyField(currentUser.getName() != null ? currentUser.getName() : "-", "lucide:user")
        );

        // 2. Email Address (Locked)
        Div emailWrapper = createFieldWrapper(
                "Email Address *",
                "Locked",
                "applicant-apply-badge-locked",
                createReadOnlyField(currentUser.getEmail() != null ? currentUser.getEmail() : "-", "lucide:mail")
        );

        // Prefill contact data from ApplicantProfile
        ApplicantProfile profile = applicantProfileService.getOrCreateProfile(currentUser);

        // 3. Phone Number (Editable, Required in UI, max 30)
        phoneField = new TextField();
        phoneField.addClassName("applicant-apply-input");
        phoneField.addClassName("applicant-apply-input-editable");
        phoneField.setPlaceholder("+62 8xx xxxx xxxx");
        phoneField.setMaxLength(30);
        phoneField.setPrefixComponent(new Html("<iconify-icon icon=\"lucide:phone\" class=\"applicant-apply-field-icon\"></iconify-icon>"));
        phoneField.setWidthFull();
        phoneField.setValue(profile.getPhone() != null ? profile.getPhone() : "");

        Div phoneWrapper = createFieldWrapper(
                "Phone Number *",
                null,
                null,
                phoneField
        );

        // 4. LinkedIn URL (Editable, Optional, max 255)
        linkedInField = new TextField();
        linkedInField.addClassName("applicant-apply-input");
        linkedInField.addClassName("applicant-apply-input-editable");
        linkedInField.setPlaceholder("https://linkedin.com/in/username");
        linkedInField.setMaxLength(255);
        linkedInField.setPrefixComponent(new Html("<iconify-icon icon=\"lucide:linkedin\" class=\"applicant-apply-field-icon\"></iconify-icon>"));
        linkedInField.setWidthFull();
        linkedInField.setValue(profile.getLinkedIn() != null ? profile.getLinkedIn() : "");

        Div linkedInWrapper = createFieldWrapper(
                "LinkedIn URL",
                "Optional",
                "applicant-apply-badge-optional",
                linkedInField
        );

        // 5. Portfolio URL (Editable, Optional, max 255)
        portfolioField = new TextField();
        portfolioField.addClassName("applicant-apply-input");
        portfolioField.addClassName("applicant-apply-input-editable");
        portfolioField.setPlaceholder("https://yourportfolio.com");
        portfolioField.setMaxLength(255);
        portfolioField.setPrefixComponent(new Html("<iconify-icon icon=\"lucide:globe\" class=\"applicant-apply-field-icon\"></iconify-icon>"));
        portfolioField.setWidthFull();
        portfolioField.setValue(profile.getPortfolio() != null ? profile.getPortfolio() : "");

        Div portfolioWrapper = createFieldWrapper(
                "Portfolio URL",
                "Optional",
                "applicant-apply-badge-optional",
                portfolioField
        );
        portfolioWrapper.getStyle().set("grid-column", "1 / -1");

        fieldGrid.add(nameWrapper, emailWrapper, phoneWrapper, linkedInWrapper, portfolioWrapper);
        section1.add(sec1Header, sec1Subtitle, fieldGrid);
        content.add(section1);

        // Divider
        Div divider1 = new Div();
        divider1.addClassName("applicant-apply-divider");
        content.add(divider1);

        // SECTION 2: Resume / CV
        Div section2 = new Div();
        section2.addClassName("applicant-apply-section");

        Div sec2Header = new Div();
        sec2Header.addClassName("applicant-apply-sec-header");

        Span numBadge2 = new Span("2");
        numBadge2.addClassName("applicant-apply-step-badge");

        H2 sec2Title = new H2("Resume / CV *");
        sec2Title.addClassName("applicant-apply-sec-title");

        sec2Header.add(numBadge2, sec2Title);

        Paragraph sec2Subtitle = new Paragraph("Upload your latest CV or resume.");
        sec2Subtitle.addClassName("applicant-apply-sec-subtitle");

        // Upload Area
        Div uploadArea = createUploadArea();

        section2.add(sec2Header, sec2Subtitle, uploadArea);
        content.add(section2);

        // Divider
        Div divider2 = new Div();
        divider2.addClassName("applicant-apply-divider");
        content.add(divider2);

        // ACTION BUTTONS (Cancel and Submit Application)
        Div actionArea = new Div();
        actionArea.addClassName("applicant-apply-actions");

        Button cancelButton = new Button("Cancel", event -> UI.getCurrent().navigate(JobDetailView.class, currentJob.getId()));
        cancelButton.addClassName("applicant-apply-btn-cancel");

        Button submitButton = new Button("Submit Application", event -> handleSubmit());
        submitButton.addClassName("applicant-apply-btn-submit");

        actionArea.add(cancelButton, submitButton);
        content.add(actionArea);

        return content;
    }

    private Div createFieldWrapper(String labelText, String badgeText, String badgeClass, TextField field) {
        Div wrapper = new Div();
        wrapper.addClassName("applicant-apply-field-item");

        Div labelRow = new Div();
        labelRow.addClassName("applicant-apply-label-row");

        Span label = new Span(labelText);
        label.addClassName("applicant-apply-label");
        labelRow.add(label);

        if (badgeText != null && !badgeText.isBlank()) {
            Span badge = new Span(badgeText);
            badge.addClassName("applicant-apply-badge");
            if (badgeClass != null) {
                badge.addClassName(badgeClass);
            }
            labelRow.add(badge);
        }

        wrapper.add(labelRow, field);
        return wrapper;
    }

    private TextField createReadOnlyField(String value, String iconName) {
        TextField tf = new TextField();
        tf.setValue(value);
        tf.setReadOnly(true);
        tf.addClassName("applicant-apply-input");
        tf.addClassName("applicant-apply-input-locked");
        tf.setPrefixComponent(new Html("<iconify-icon icon=\"" + iconName + "\" class=\"applicant-apply-field-icon\"></iconify-icon>"));
        tf.setWidthFull();
        return tf;
    }

    private Div createUploadArea() {
        Div uploadContainer = new Div();
        uploadContainer.addClassName("applicant-apply-upload-container");

        Div uploadDropBox = new Div();
        uploadDropBox.addClassName("applicant-apply-upload-box");

        Html cloudIcon = new Html("<iconify-icon icon=\"lucide:upload-cloud\" class=\"applicant-apply-upload-cloud-icon\"></iconify-icon>");

        Paragraph uploadTitle = new Paragraph("Click to upload your CV or drag and drop");
        uploadTitle.addClassName("applicant-apply-upload-title");

        Paragraph uploadHint = new Paragraph("PDF / DOCX, max 5 MB");
        uploadHint.addClassName("applicant-apply-upload-hint");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".pdf", ".docx", "application/pdf");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.addClassName("applicant-apply-upload-component");

        fileBadgeContainer = new Div();
        fileBadgeContainer.addClassName("applicant-apply-file-badge");
        fileBadgeContainer.setVisible(false);

        upload.addSucceededListener(event -> {
            uploadedFileName = event.getFileName();
            fileBadgeContainer.removeAll();

            Html fileIcon = new Html("<iconify-icon icon=\"lucide:file-text\" class=\"applicant-apply-file-icon\"></iconify-icon>");
            Span fileNameSpan = new Span(uploadedFileName);
            fileNameSpan.addClassName("applicant-apply-file-name");

            Span readyBadge = new Span("✓ File selected");
            readyBadge.addClassName("applicant-apply-file-ready");

            fileBadgeContainer.add(fileIcon, fileNameSpan, readyBadge);
            fileBadgeContainer.setVisible(true);
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("File tidak valid: " + event.getErrorMessage());
        });

        uploadDropBox.add(cloudIcon, uploadTitle, uploadHint, upload, fileBadgeContainer);
        uploadContainer.add(uploadDropBox);

        return uploadContainer;
    }

    private Div createJobSummary() {
        Div summaryCard = new Div();
        summaryCard.addClassName("applicant-apply-job-summary");

        H3 title = new H3("Job Summary");
        title.addClassName("applicant-apply-summary-title");
        summaryCard.add(title);

        // Company Identity
        Div companyRow = new Div();
        companyRow.addClassName("applicant-apply-summary-company-row");

        String compName = currentJob.getCompanyName() != null && !currentJob.getCompanyName().isBlank()
                ? currentJob.getCompanyName()
                : "-";
        Span avatar = new Span(getCompanyInitials(compName));
        avatar.addClassName("applicant-apply-summary-avatar");

        Div compInfo = new Div();
        compInfo.addClassName("applicant-apply-summary-comp-info");

        Span nameSpan = new Span(compName);
        nameSpan.addClassName("applicant-apply-summary-comp-name");
        compInfo.add(nameSpan);

        companyRow.add(avatar, compInfo);
        summaryCard.add(companyRow);

        // Position Title
        H3 posTitle = new H3(currentJob.getPosition() != null ? currentJob.getPosition() : "-");
        posTitle.addClassName("applicant-apply-summary-position");
        summaryCard.add(posTitle);

        // Salary Badge
        String salaryText = formatSalaryBadge(currentJob.getSalaryMin(), currentJob.getSalaryMax());
        if (salaryText != null) {
            Span salaryBadge = new Span(salaryText);
            salaryBadge.addClassName("applicant-job-salary");
            salaryBadge.addClassName("applicant-apply-summary-salary");
            summaryCard.add(salaryBadge);
        }

        // Divider
        Div divider = new Div();
        divider.addClassName("applicant-apply-divider");
        summaryCard.add(divider);

        // Overview Rows
        String loc = currentJob.getLocation() != null && !currentJob.getLocation().isBlank()
                ? currentJob.getLocation()
                : "-";
        summaryCard.add(createSummaryRow("lucide:map-pin", "Location", loc));

        String workModelText = formatWorkType(currentJob.getTipeKerja());
        if (currentJob.getLocation() != null && !currentJob.getLocation().isBlank()) {
            workModelText += " (" + currentJob.getLocation() + ")";
        }
        summaryCard.add(createSummaryRow("lucide:briefcase", "Working Model", workModelText));

        summaryCard.add(createSummaryRow("lucide:building", "Employment", formatJobType(currentJob.getJobTipe())));

        summaryCard.add(createSummaryRow("lucide:calendar", "Date Posted", formatReadableDate(currentJob.getPostedDate())));

        return summaryCard;
    }

    private Div createSummaryRow(String iconName, String label, String value) {
        Div row = new Div();
        row.addClassName("applicant-apply-summary-row");

        Div labelGroup = new Div();
        labelGroup.addClassName("applicant-apply-summary-row-label-group");

        Html icon = new Html("<iconify-icon icon=\"" + iconName + "\" class=\"applicant-apply-summary-row-icon\"></iconify-icon>");
        Span labelSpan = new Span(label);
        labelSpan.addClassName("applicant-apply-summary-row-label");

        labelGroup.add(icon, labelSpan);

        Span valSpan = new Span(value);
        valSpan.addClassName("applicant-apply-summary-row-val");

        row.add(labelGroup, valSpan);
        return row;
    }

    private void handleSubmit() {
        if (!currentJob.isActive()) {
            Notification.show("Lowongan ini sudah tidak aktif");
            UI.getCurrent().navigate(PelamarView.class);
            return;
        }

        if (jobApplicationService.hasApplied(currentUser, currentJob)) {
            Notification.show("Anda sudah melamar lowongan ini");
            UI.getCurrent().navigate(JobDetailView.class, currentJob.getId());
            return;
        }

        String rawPhone = phoneField.getValue();
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            phoneField.setInvalid(true);
            phoneField.setErrorMessage("Phone number is required");
            Notification.show("Silakan isi nomor telepon Anda");
            return;
        }
        phoneField.setInvalid(false);

        String trimmedPhone = rawPhone.trim();
        if (trimmedPhone.length() > 30) {
            phoneField.setInvalid(true);
            phoneField.setErrorMessage("Nomor telepon tidak boleh melebihi 30 karakter");
            Notification.show("Nomor telepon tidak boleh melebihi 30 karakter");
            return;
        }

        String finalLinkedIn = normalizeOptionalField(linkedInField.getValue(), 255);
        String finalPortfolio = normalizeOptionalField(portfolioField.getValue(), 255);

        // Save application using existing backend mapping
        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicant(currentUser);
        jobApplication.setJobPosting(currentJob);
        jobApplication.setCompanyName(currentJob.getCompanyName());
        jobApplication.setPosition(currentJob.getPosition());
        jobApplication.settipeKerja(currentJob.getTipeKerja());
        jobApplication.setApplicationDate(LocalDate.now());
        jobApplication.setStatus(ApplicationStatus.APPLIED);
        jobApplication.setPhone(trimmedPhone);
        jobApplication.setLinkedIn(finalLinkedIn);
        jobApplication.setPortfolio(finalPortfolio);

        jobApplicationService.simpanLamaran(jobApplication);

        Notification.show("Lamaran berhasil dikirim");
        UI.getCurrent().navigate(ApplicationsView.class);
    }

    private String normalizeOptionalField(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    private User getCurrentUser() {
        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }

    private String getCompanyInitials(String companyName) {
        if (companyName == null || companyName.isBlank() || "-".equals(companyName.trim())) {
            return "CO";
        }
        String trimmed = companyName.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String formatSalaryBadge(Long min, Long max) {
        if (min != null && max != null) {
            return "IDR " + formatNumber(min) + " – " + formatNumber(max) + " / month";
        } else if (min != null) {
            return "From IDR " + formatNumber(min) + " / month";
        } else if (max != null) {
            return "Up to IDR " + formatNumber(max) + " / month";
        }
        return null;
    }

    private String formatNumber(Long number) {
        if (number == null) {
            return "0";
        }
        return String.format(Locale.US, "%,d", number);
    }

    private String formatWorkType(ApplicationTipe type) {
        if (type == null) {
            return "-";
        }
        switch (type) {
            case WFO:
                return "Onsite";
            case WFH:
                return "Remote";
            case HYBRID:
                return "Hybrid";
            default:
                return type.name();
        }
    }

    private String formatJobType(JobTipe type) {
        if (type == null) {
            return "-";
        }
        switch (type) {
            case FULL_TIME:
                return "Full-time";
            case PART_TIME:
                return "Part-time";
            case CONTRACT:
                return "Contract";
            case INTERNSHIP:
                return "Internship";
            case FREELANCE:
                return "Freelance";
            default:
                return type.name();
        }
    }

    private String formatReadableDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }
}
