package com.example.view;

import com.example.entity.ApplicationStatus;
import com.example.entity.ApplicationTipe;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Route(value = "pelamar/job", layout = MainLayout.class)
@PageTitle("Job Detail | Job Application Tracker")
@RolesAllowed({"PELAMAR", "ADMIN"})
public class JobDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private JobPosting currentJob;

    public JobDetailView(
            JobPostingService jobPostingService,
            JobApplicationService jobApplicationService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        addClassName("applicant-job-detail");
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

        this.currentJob = job;
        removeAll();
        buildView();
    }

    private void buildView() {
        if (currentJob == null) {
            return;
        }

        createBreadcrumb();
        createBackButton();
        createMainGrid();
    }

    private void createBreadcrumb() {
        Div breadcrumb = new Div();
        breadcrumb.addClassName("applicant-job-detail-breadcrumb");

        Span browseLink = new Span("Browse Jobs");
        browseLink.addClassName("applicant-job-detail-breadcrumb-link");
        browseLink.addClickListener(event -> UI.getCurrent().navigate(PelamarView.class));

        Span sep1 = new Span("/");
        sep1.addClassName("applicant-job-detail-breadcrumb-sep");

        String companyText = currentJob.getCompanyName() != null && !currentJob.getCompanyName().isBlank()
                ? currentJob.getCompanyName()
                : "-";
        Span companySpan = new Span(companyText);
        companySpan.addClassName("applicant-job-detail-breadcrumb-item");

        Span sep2 = new Span("/");
        sep2.addClassName("applicant-job-detail-breadcrumb-sep");

        String positionText = currentJob.getPosition() != null && !currentJob.getPosition().isBlank()
                ? currentJob.getPosition()
                : "-";
        Span positionSpan = new Span(positionText);
        positionSpan.addClassName("applicant-job-detail-breadcrumb-current");

        breadcrumb.add(browseLink, sep1, companySpan, sep2, positionSpan);
        add(breadcrumb);
    }

    private void createBackButton() {
        Html backIcon = new Html("<iconify-icon icon=\"lucide:arrow-left\" class=\"applicant-job-detail-back-icon\"></iconify-icon>");
        Button backButton = new Button(backIcon, event -> UI.getCurrent().navigate(PelamarView.class));
        backButton.addClassName("applicant-job-detail-back");
        backButton.setAriaLabel("Back to Browse Jobs");
        backButton.setTooltipText("Back to Browse Jobs");
        add(backButton);
    }

    private void createMainGrid() {
        Div gridContainer = new Div();
        gridContainer.addClassName("applicant-job-detail-grid");

        Div mainColumn = createMainColumn();
        Div sidebarColumn = createSidebarColumn();

        gridContainer.add(mainColumn, sidebarColumn);
        add(gridContainer);
    }

    private Div createMainColumn() {
        Div mainColumn = new Div();
        mainColumn.addClassName("applicant-job-detail-main");

        // Single primary content surface
        Div contentSurface = new Div();
        contentSurface.addClassName("applicant-job-detail-content");

        // 1. Header (Company info, position title, pills)
        Div companyRow = new Div();
        companyRow.addClassName("applicant-job-detail-company-row");

        String companyName = currentJob.getCompanyName() != null && !currentJob.getCompanyName().isBlank()
                ? currentJob.getCompanyName()
                : "-";
        Span avatar = new Span(getCompanyInitials(companyName));
        avatar.addClassName("applicant-job-detail-avatar");

        Div companyInfo = new Div();
        companyInfo.addClassName("applicant-job-detail-company-info");

        Span nameSpan = new Span(companyName);
        nameSpan.addClassName("applicant-job-detail-company");

        String locationText = currentJob.getLocation() != null && !currentJob.getLocation().isBlank()
                ? currentJob.getLocation()
                : "-";
        String dateText = formatPostedDate(currentJob.getPostedDate());
        Span metaSpan = new Span(locationText + " · " + dateText);
        metaSpan.addClassName("applicant-job-detail-meta");

        companyInfo.add(nameSpan, metaSpan);
        companyRow.add(avatar, companyInfo);

        H1 positionTitle = new H1(currentJob.getPosition() != null ? currentJob.getPosition() : "-");
        positionTitle.addClassName("applicant-job-detail-title");

        Div tagsRow = new Div();
        tagsRow.addClassName("applicant-job-detail-tags");

        String salaryText = formatSalaryBadge(currentJob.getSalaryMin(), currentJob.getSalaryMax());
        if (salaryText != null) {
            Span salaryBadge = new Span(salaryText);
            salaryBadge.addClassName("applicant-job-salary");
            tagsRow.add(salaryBadge);
        }

        Span workTypeBadge = new Span(formatWorkType(currentJob.getTipeKerja()));
        workTypeBadge.addClassName("applicant-job-work-type");

        Span jobTypeBadge = new Span(formatJobType(currentJob.getJobTipe()));
        jobTypeBadge.addClassName("applicant-job-type");

        tagsRow.add(workTypeBadge, jobTypeBadge);

        contentSurface.add(companyRow, positionTitle, tagsRow);

        // 2. Job Description Section
        Div descSection = new Div();
        descSection.addClassName("applicant-job-detail-section");

        H2 descHeading = new H2("Job Description");
        descHeading.addClassName("applicant-job-detail-section-title");

        Paragraph descParagraph = new Paragraph();
        descParagraph.addClassName("applicant-job-detail-body");
        if (currentJob.getDescription() != null && !currentJob.getDescription().isBlank()) {
            descParagraph.setText(currentJob.getDescription());
        } else {
            descParagraph.setText("No description provided.");
        }

        descSection.add(descHeading, descParagraph);
        contentSurface.add(descSection);

        // Subtle divider between Job Description and Requirements
        Div midDivider = new Div();
        midDivider.addClassName("applicant-job-detail-divider");
        contentSurface.add(midDivider);

        // 3. Requirements Section
        Div reqSection = new Div();
        reqSection.addClassName("applicant-job-detail-section");

        H2 reqHeading = new H2("Requirements");
        reqHeading.addClassName("applicant-job-detail-section-title");

        reqSection.add(reqHeading, createRequirementsContent(currentJob.getRequirements()));
        contentSurface.add(reqSection);

        mainColumn.add(contentSurface);
        return mainColumn;
    }

    private Component createRequirementsContent(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            Paragraph fallback = new Paragraph("No specific requirements listed.");
            fallback.addClassName("applicant-job-detail-body");
            return fallback;
        }

        String[] lines = requirements.split("\\r?\\n");
        List<String> validLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.startsWith("• ")) {
                    trimmed = trimmed.substring(2).trim();
                } else if (trimmed.startsWith("- ")) {
                    trimmed = trimmed.substring(2).trim();
                } else if (trimmed.startsWith("* ")) {
                    trimmed = trimmed.substring(2).trim();
                }
                if (!trimmed.isEmpty()) {
                    validLines.add(trimmed);
                }
            }
        }

        if (validLines.size() > 1) {
            UnorderedList list = new UnorderedList();
            list.addClassName("applicant-job-detail-req-list");
            for (String itemText : validLines) {
                ListItem item = new ListItem(itemText);
                item.addClassName("applicant-job-detail-req-item");
                list.add(item);
            }
            return list;
        } else if (validLines.size() == 1) {
            Paragraph singleReq = new Paragraph(validLines.get(0));
            singleReq.addClassName("applicant-job-detail-body");
            return singleReq;
        } else {
            Paragraph fallback = new Paragraph("No specific requirements listed.");
            fallback.addClassName("applicant-job-detail-body");
            return fallback;
        }
    }

    private Div createSidebarColumn() {
        Div sidebarColumn = new Div();
        sidebarColumn.addClassName("applicant-job-detail-sidebar");

        // Single sidebar surface
        Div sidebarCard = new Div();
        sidebarCard.addClassName("applicant-job-detail-sidebar-card");

        // 1. Application Action Section
        Div actionSection = new Div();
        actionSection.addClassName("applicant-job-detail-sidebar-section");

        Div actionHeader = new Div();
        actionHeader.addClassName("applicant-job-detail-action-header");

        H3 actionTitle = new H3("Application Action");
        actionTitle.addClassName("applicant-job-detail-action-title");

        Span refSpan = new Span("Ref: #" + currentJob.getId());
        refSpan.addClassName("applicant-job-detail-ref");

        actionHeader.add(actionTitle, refSpan);
        actionSection.add(actionHeader);

        boolean isPelamar = authenticationContext.hasRole("PELAMAR");
        if (isPelamar) {
            User currentUser = getCurrentUser();
            boolean sudahMelamar = jobApplicationService.hasApplied(currentUser, currentJob);

            Button applyButton = new Button();
            if (sudahMelamar) {
                applyButton.setText("Applied");
                applyButton.setEnabled(false);
                applyButton.addClassName("applicant-job-applied-button");
                applyButton.addClassName("applicant-job-detail-apply-btn");
            } else {
                applyButton.setText("Apply Now");
                applyButton.addClassName("applicant-job-apply-button");
                applyButton.addClassName("applicant-job-detail-apply-btn");
                applyButton.addClickListener(event -> handleApply(currentJob, applyButton));
            }
            actionSection.add(applyButton);
        }

        sidebarCard.add(actionSection);

        // Divider
        Div div1 = new Div();
        div1.addClassName("applicant-job-detail-divider");
        sidebarCard.add(div1);

        // 2. Job Overview Section
        Div overviewSection = new Div();
        overviewSection.addClassName("applicant-job-detail-sidebar-section");

        H3 overviewTitle = new H3("Job Overview");
        overviewTitle.addClassName("applicant-job-detail-card-title");
        overviewSection.add(overviewTitle);

        overviewSection.add(createOverviewRow("Employment Type", formatJobType(currentJob.getJobTipe())));

        String workModelText = formatWorkType(currentJob.getTipeKerja());
        if (currentJob.getLocation() != null && !currentJob.getLocation().isBlank()) {
            workModelText += " (" + currentJob.getLocation() + ")";
        }
        overviewSection.add(createOverviewRow("Working Model", workModelText));

        String compensationText = formatSalaryBadge(currentJob.getSalaryMin(), currentJob.getSalaryMax());
        if (compensationText == null) {
            compensationText = "Not specified";
        }
        overviewSection.add(createOverviewRow("Compensation", compensationText));

        overviewSection.add(createOverviewRow("Date Posted", formatReadableDate(currentJob.getPostedDate())));
        sidebarCard.add(overviewSection);

        // Divider
        Div div2 = new Div();
        div2.addClassName("applicant-job-detail-divider");
        sidebarCard.add(div2);

        // 3. About Company Section
        Div companySection = new Div();
        companySection.addClassName("applicant-job-detail-sidebar-section");

        H3 compHeading = new H3("About Company");
        compHeading.addClassName("applicant-job-detail-card-title");

        String compNameText = currentJob.getCompanyName() != null && !currentJob.getCompanyName().isBlank()
                ? currentJob.getCompanyName()
                : "-";
        Paragraph compName = new Paragraph(compNameText);
        compName.addClassName("applicant-job-detail-company-name-large");

        String compLocText = currentJob.getLocation() != null && !currentJob.getLocation().isBlank()
                ? currentJob.getLocation()
                : "-";
        Paragraph compLocation = new Paragraph(compLocText);
        compLocation.addClassName("applicant-job-detail-company-location");

        companySection.add(compHeading, compName, compLocation);
        sidebarCard.add(companySection);

        sidebarColumn.add(sidebarCard);
        return sidebarColumn;
    }

    private Div createOverviewRow(String label, String value) {
        Div row = new Div();
        row.addClassName("applicant-job-detail-overview-row");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("applicant-job-detail-overview-label");

        Span valSpan = new Span(value);
        valSpan.addClassName("applicant-job-detail-overview-val");

        row.add(labelSpan, valSpan);
        return row;
    }

    private void handleApply(JobPosting job, Button applyButton) {
        if (!job.isActive()) {
            Notification.show("Lowongan ini sudah tidak aktif");
            return;
        }

        User currentUser = getCurrentUser();
        if (jobApplicationService.hasApplied(currentUser, job)) {
            Notification.show("Anda sudah melamar lowongan ini");
            applyButton.setText("Applied");
            applyButton.setEnabled(false);
            applyButton.removeClassName("applicant-job-apply-button");
            applyButton.addClassName("applicant-job-applied-button");
            return;
        }

        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicant(currentUser);
        jobApplication.setJobPosting(job);
        jobApplication.setCompanyName(job.getCompanyName());
        jobApplication.setPosition(job.getPosition());
        jobApplication.settipeKerja(job.getTipeKerja());
        jobApplication.setApplicationDate(LocalDate.now());
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        jobApplicationService.simpanLamaran(jobApplication);

        applyButton.setText("Applied");
        applyButton.setEnabled(false);
        applyButton.removeClassName("applicant-job-apply-button");
        applyButton.addClassName("applicant-job-applied-button");

        Notification.show("Lamaran berhasil dikirim");
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

    private String formatPostedDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return "Posted " + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }

    private String formatReadableDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }
}
