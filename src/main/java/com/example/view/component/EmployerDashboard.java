package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.view.JobApplicantsView;
import com.example.view.PemberiLamaranView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EmployerDashboard extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    // Company Card Elements
    private final Span companyNameSpan = new Span();
    private final Span companySubtextSpan = new Span("Employer Portal");

    // KPI Cards Elements
    private final Span totalJobsValue = new Span("0");
    private final Span totalJobsSubtext = new Span("0 Active · 0 Inactive");

    private final Span activeJobsValue = new Span("0");
    private final Span activeJobsSubtext = new Span("Open and visible to applicants");

    private final Span newAppsValue = new Span("0");
    private final Span newAppsSubtext = new Span("No applications awaiting review");

    private final Span totalAppsValue = new Span("0");
    private final Span totalAppsSubtext = new Span("Total candidate submissions received");

    // Funnel & Status Summary Elements
    private final Div funnelBar = new Div();
    private final Span newAppsSummaryValue = new Span("0");
    private final Span newAppsBadge = new Span("Needs Review");
    private final Span newAppsDesc = new Span("No applications awaiting review");
    private final Span inProcessSummaryValue = new Span("0");
    private final Span offeredSummaryValue = new Span("0");
    private final Span rejectedSummaryValue = new Span("0");

    // Recent Applicants Elements
    private final Grid<JobApplication> recentApplicantsGrid = new Grid<>(JobApplication.class, false);
    private final Div emptyStateContainer = new Div();

    public EmployerDashboard() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("employer-dashboard");

        createHeaderSection();
        createCompanyCardSection();
        createKpiCardsSection();
        createStatusSummarySection();
        createRecentApplicantsSection();
    }

    private void createHeaderSection() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.addClassName("employer-header-layout");
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        Div textGroup = new Div();
        textGroup.addClassName("employer-header-text-group");

        H2 title = new H2("Dashboard");
        title.addClassName("employer-dashboard-title");

        Span subtitle = new Span("Monitor your company's active postings and incoming candidate applications.");
        subtitle.addClassName("employer-dashboard-subtitle");

        textGroup.add(title, subtitle);

        Button createJobBtn = new Button("Create Job", VaadinIcon.PLUS.create(), event -> {
            UI.getCurrent().navigate(PemberiLamaranView.class);
        });
        createJobBtn.addClassName("employer-btn-create-job");

        headerLayout.add(textGroup, createJobBtn);
        add(headerLayout);
    }

    private void createCompanyCardSection() {
        Div companyCard = new Div();
        companyCard.addClassName("employer-company-card");

        Div iconBox = new Div(VaadinIcon.BUILDING.create());
        iconBox.addClassName("employer-company-icon-box");

        Div textGroup = new Div();
        textGroup.addClassName("employer-company-info-text");

        companyNameSpan.addClassName("employer-company-name");
        companySubtextSpan.addClassName("employer-company-subtext");

        textGroup.add(companyNameSpan, companySubtextSpan);
        companyCard.add(iconBox, textGroup);

        add(companyCard);
    }

    private void createKpiCardsSection() {
        Div kpiRow = new Div();
        kpiRow.addClassName("employer-kpi-row");

        // Card 1: Total Job Postings (Clean informational - no icon)
        Div totalJobsCard = createKpiCard(
                "Total Job Postings",
                null,
                totalJobsValue,
                "",
                totalJobsSubtext
        );

        // Card 2: Active Job Postings (Subtle light Lucide circle-check icon)
        Html checkIcon = new Html("<iconify-icon icon=\"lucide:circle-check\" class=\"kpi-lucide-icon kpi-lucide-check\"></iconify-icon>");
        Div activeJobsCard = createKpiCard(
                "Active Job Postings",
                checkIcon,
                activeJobsValue,
                "active-color",
                activeJobsSubtext
        );

        // Card 3: New Applications (Subtle light Lucide bell icon)
        Html bellIcon = new Html("<iconify-icon icon=\"lucide:bell\" class=\"kpi-lucide-icon kpi-lucide-bell\"></iconify-icon>");
        Div newAppsCard = createKpiCard(
                "New Applications",
                bellIcon,
                newAppsValue,
                "",
                newAppsSubtext
        );

        // Card 4: Total Applications (Clean informational - no icon)
        Div totalAppsCard = createKpiCard(
                "Total Applications",
                null,
                totalAppsValue,
                "",
                totalAppsSubtext
        );

        kpiRow.add(totalJobsCard, activeJobsCard, newAppsCard, totalAppsCard);
        add(kpiRow);
    }

    private Div createKpiCard(
            String label,
            Component iconComponent,
            Span valueSpan,
            String valueColorClass,
            Span subtextSpan) {

        Div card = new Div();
        card.addClassName("employer-kpi-card");

        Div topRow = new Div();
        topRow.addClassName("employer-kpi-top");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("employer-kpi-label");

        topRow.add(labelSpan);

        if (iconComponent != null) {
            topRow.add(iconComponent);
        }

        valueSpan.addClassName("employer-kpi-value");
        if (valueColorClass != null && !valueColorClass.isBlank()) {
            valueSpan.addClassName(valueColorClass);
        }

        subtextSpan.addClassName("employer-kpi-subtext");

        card.add(topRow, valueSpan, subtextSpan);
        return card;
    }

    private void createStatusSummarySection() {
        Div section = new Div();
        section.addClassName("employer-section-card");

        Div header = new Div();
        header.addClassName("employer-section-header");

        Div titleGroup = new Div();
        titleGroup.addClassName("employer-section-title-group");

        H3 title = new H3("Application Status Summary");
        title.addClassName("employer-section-title");

        Span subtitle = new Span("Overview of application progress across your job postings.");
        subtitle.addClassName("employer-section-subtitle");

        titleGroup.add(title, subtitle);
        header.add(titleGroup);

        funnelBar.addClassName("employer-funnel-bar");

        Div statusRow = new Div();
        statusRow.addClassName("employer-status-row");

        // Only New Applications has a badge, and it is conditionally visible when count > 0
        newAppsBadge.addClassNames("employer-mini-badge", "needs-review");
        newAppsBadge.setVisible(false);

        Div newAppsCard = createStatusCard("New Applications", newAppsBadge, newAppsSummaryValue, newAppsDesc);
        Div inProcessCard = createStatusCard("In Process", null, inProcessSummaryValue, new Span("Screening, Technical Test & Interview stages"));
        Div offeredCard = createStatusCard("Offered", null, offeredSummaryValue, new Span("Candidates with formal job offers"));
        Div rejectedCard = createStatusCard("Rejected", null, rejectedSummaryValue, new Span("Closed applications not proceeding"));

        statusRow.add(newAppsCard, inProcessCard, offeredCard, rejectedCard);

        Paragraph footerNote = new Paragraph("Status counts update automatically as applicants advance through recruitment stages.");
        footerNote.addClassName("employer-status-footer-note");

        section.add(header, funnelBar, statusRow, footerNote);
        add(section);
    }

    private Div createStatusCard(String title, Span badge, Span valueSpan, Span descSpan) {
        Div card = new Div();
        card.addClassName("employer-status-card");

        Div topRow = new Div();
        topRow.addClassName("employer-status-card-header");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("employer-status-card-title");
        topRow.add(titleSpan);

        if (badge != null) {
            topRow.add(badge);
        }

        valueSpan.addClassName("employer-status-card-value");
        descSpan.addClassName("employer-status-card-desc");

        card.add(topRow, valueSpan, descSpan);
        return card;
    }

    private void createRecentApplicantsSection() {
        Div section = new Div();
        section.addClassName("employer-section-card");

        Div header = new Div();
        header.addClassName("employer-section-header");

        Div titleGroup = new Div();
        titleGroup.addClassName("employer-section-title-group");

        H3 title = new H3("Recent Applicants");
        title.addClassName("employer-section-title");

        Span subtitle = new Span("Latest 5 candidate submissions received for your job postings.");
        subtitle.addClassName("employer-section-subtitle");

        titleGroup.add(title, subtitle);
        header.add(titleGroup);

        configureRecentApplicantsGrid();

        // Empty state container
        emptyStateContainer.addClassName("employer-empty-state");
        Paragraph emptyTitle = new Paragraph("Belum ada pelamar terbaru.");
        emptyTitle.addClassName("employer-empty-title");
        Paragraph emptyDesc = new Paragraph("Saat pelamar mendaftar ke lowongan aktif Anda, data lamaran mereka akan muncul di sini.");
        emptyDesc.addClassName("employer-empty-desc");
        emptyStateContainer.add(emptyTitle, emptyDesc);

        section.add(header, emptyStateContainer, recentApplicantsGrid);
        add(section);
    }

    private void configureRecentApplicantsGrid() {
        recentApplicantsGrid.addClassName("employer-grid");
        recentApplicantsGrid.setWidthFull();
        recentApplicantsGrid.setAllRowsVisible(true);

        // Column 1: APPLICANT (Avatar initials circle + Full Name)
        recentApplicantsGrid.addComponentColumn(jobApplication -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            String applicantName = "Data Pelamar Tidak Tersedia";
            if (jobApplication.getApplicant() != null && jobApplication.getApplicant().getName() != null) {
                applicantName = jobApplication.getApplicant().getName();
            }

            Span avatar = new Span(getInitials(applicantName));
            avatar.addClassName("employer-applicant-avatar");

            Span nameSpan = new Span(applicantName);
            nameSpan.addClassName("employer-applicant-name");

            layout.add(avatar, nameSpan);
            return layout;
        }).setHeader("APPLICANT").setFlexGrow(2);

        // Column 2: POSITION
        recentApplicantsGrid.addComponentColumn(jobApplication -> {
            String position = "-";
            if (jobApplication.getJobPosting() != null && jobApplication.getJobPosting().getPosition() != null) {
                position = jobApplication.getJobPosting().getPosition();
            } else if (jobApplication.getPosition() != null) {
                position = jobApplication.getPosition();
            }

            Span posSpan = new Span(position);
            posSpan.addClassName("employer-applicant-position");
            return posSpan;
        }).setHeader("POSITION").setFlexGrow(2);

        // Column 3: STATUS (Badge Pill)
        recentApplicantsGrid.addComponentColumn(this::createStatusBadge)
                .setHeader("STATUS")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Column 4: APPLIED DATE
        recentApplicantsGrid.addComponentColumn(jobApplication -> {
            String dateText = "-";
            if (jobApplication.getApplicationDate() != null) {
                dateText = jobApplication.getApplicationDate().format(DATE_FORMATTER);
            }

            Span dateSpan = new Span(dateText);
            dateSpan.addClassName("employer-applicant-date");
            return dateSpan;
        }).setHeader("APPLIED DATE").setAutoWidth(true).setFlexGrow(1);

        // Column 5: ACTIONS (Icon button leading to JobApplicantsView)
        recentApplicantsGrid.addComponentColumn(jobApplication -> {
            if (jobApplication.getJobPosting() != null && jobApplication.getJobPosting().getId() != null) {
                Long postingId = jobApplication.getJobPosting().getId();
                Button reviewButton = new Button(
                        new Html("<iconify-icon icon=\"lucide:file-search\" class=\"employer-action-icon\"></iconify-icon>"),
                        event -> UI.getCurrent().navigate(JobApplicantsView.class, postingId)
                );
                reviewButton.addClassName("employer-action-btn");
                reviewButton.setTooltipText("Review applicant");
                reviewButton.setAriaLabel("Review applicant");
                return reviewButton;
            }
            Span noAction = new Span("-");
            noAction.addClassName("employer-applicant-date");
            return noAction;
        }).setHeader("ACTIONS").setAutoWidth(true).setFlexGrow(0);
    }

    public void updateData(User currentUser, List<JobPosting> jobPostings, List<JobApplication> applications) {
        // 1. Company Information
        if (currentUser != null && currentUser.getCompany() != null && currentUser.getCompany().getName() != null) {
            companyNameSpan.setText(currentUser.getCompany().getName());
        } else {
            companyNameSpan.setText("PT Test Employer");
        }

        // 2. Job Postings KPI
        int totalJobPostings = jobPostings != null ? jobPostings.size() : 0;
        int activeJobPostings = 0;
        int inactiveJobPostings = 0;

        if (jobPostings != null) {
            for (JobPosting jobPosting : jobPostings) {
                if (jobPosting.isActive()) {
                    activeJobPostings++;
                } else {
                    inactiveJobPostings++;
                }
            }
        }

        totalJobsValue.setText(String.valueOf(totalJobPostings));
        totalJobsSubtext.setText(activeJobPostings + " Active · " + inactiveJobPostings + " Inactive");

        activeJobsValue.setText(String.valueOf(activeJobPostings));

        // 3. Applications KPI & Status Mapping
        int totalApplications = applications != null ? applications.size() : 0;
        totalAppsValue.setText(String.valueOf(totalApplications));

        int newAppsCount = 0;
        int inProcessCount = 0;
        int offeredCount = 0;
        int rejectedCount = 0;

        if (applications != null) {
            for (JobApplication jobApplication : applications) {
                ApplicationStatus status = jobApplication.getStatus();
                if (status == ApplicationStatus.APPLIED) {
                    newAppsCount++;
                } else if (status == ApplicationStatus.SCREENING
                        || status == ApplicationStatus.TECHNICAL_TEST
                        || status == ApplicationStatus.INTERVIEW) {
                    inProcessCount++;
                } else if (status == ApplicationStatus.OFFERED) {
                    offeredCount++;
                } else if (status == ApplicationStatus.REJECTED) {
                    rejectedCount++;
                }
            }
        }

        newAppsValue.setText(String.valueOf(newAppsCount));
        if (newAppsCount > 0) {
            newAppsSubtext.setText(newAppsCount + (newAppsCount == 1 ? " application awaiting review" : " applications awaiting review"));
            newAppsBadge.setVisible(true);
            newAppsDesc.setText(newAppsCount + (newAppsCount == 1 ? " application awaiting review" : " applications awaiting review"));
        } else {
            newAppsSubtext.setText("No applications awaiting review");
            newAppsBadge.setVisible(false);
            newAppsDesc.setText("No applications awaiting review");
        }

        // Update Status Summary numbers
        newAppsSummaryValue.setText(String.valueOf(newAppsCount));
        inProcessSummaryValue.setText(String.valueOf(inProcessCount));
        offeredSummaryValue.setText(String.valueOf(offeredCount));
        rejectedSummaryValue.setText(String.valueOf(rejectedCount));

        // Update Funnel Bar
        updateFunnelBar(newAppsCount, inProcessCount, offeredCount, rejectedCount, totalApplications);

        // 4. Recent Applicants (Maximum 5, sorted latest to oldest)
        if (applications == null || applications.isEmpty()) {
            emptyStateContainer.setVisible(true);
            recentApplicantsGrid.setVisible(false);
        } else {
            emptyStateContainer.setVisible(false);
            recentApplicantsGrid.setVisible(true);

            List<JobApplication> recentList = applications.stream()
                    .sorted((a, b) -> {
                        if (a.getApplicationDate() == null && b.getApplicationDate() == null) return 0;
                        if (a.getApplicationDate() == null) return 1;
                        if (b.getApplicationDate() == null) return -1;
                        return b.getApplicationDate().compareTo(a.getApplicationDate());
                    })
                    .limit(5)
                    .toList();

            recentApplicantsGrid.setItems(recentList);
        }
    }

    private void updateFunnelBar(int newApps, int inProcess, int offered, int rejected, int total) {
        funnelBar.removeAll();
        if (total == 0) {
            Div emptySegment = new Div();
            emptySegment.addClassNames("employer-funnel-segment", "empty");
            funnelBar.add(emptySegment);
            return;
        }

        if (newApps > 0) {
            Div seg = new Div();
            seg.addClassNames("employer-funnel-segment", "new-apps");
            seg.getStyle().set("width", String.format(Locale.US, "%.2f%%", ((double) newApps / total) * 100));
            funnelBar.add(seg);
        }
        if (inProcess > 0) {
            Div seg = new Div();
            seg.addClassNames("employer-funnel-segment", "in-process");
            seg.getStyle().set("width", String.format(Locale.US, "%.2f%%", ((double) inProcess / total) * 100));
            funnelBar.add(seg);
        }
        if (offered > 0) {
            Div seg = new Div();
            seg.addClassNames("employer-funnel-segment", "offered");
            seg.getStyle().set("width", String.format(Locale.US, "%.2f%%", ((double) offered / total) * 100));
            funnelBar.add(seg);
        }
        if (rejected > 0) {
            Div seg = new Div();
            seg.addClassNames("employer-funnel-segment", "rejected");
            seg.getStyle().set("width", String.format(Locale.US, "%.2f%%", ((double) rejected / total) * 100));
            funnelBar.add(seg);
        }
    }

    private Span createStatusBadge(JobApplication jobApplication) {
        ApplicationStatus status = jobApplication.getStatus();
        if (status == null) {
            Span badge = new Span("-");
            badge.addClassName("status-badge");
            return badge;
        }

        String labelText;
        switch (status) {
            case APPLIED:
                labelText = "Applied";
                break;
            case SCREENING:
                labelText = "Screening";
                break;
            case TECHNICAL_TEST:
                labelText = "Technical Test";
                break;
            case INTERVIEW:
                labelText = "Interview";
                break;
            case OFFERED:
                labelText = "Offered";
                break;
            case REJECTED:
                labelText = "Rejected";
                break;
            default:
                labelText = status.name();
                break;
        }

        Span badge = new Span(labelText);
        badge.addClassName("status-badge");
        badge.addClassName(status.name().toLowerCase().replace('_', '-'));
        return badge;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "NA";
        }
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
