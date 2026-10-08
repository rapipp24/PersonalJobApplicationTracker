package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.User;
import com.example.view.ApplicationsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ApplicantDashboard extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    // KPI Card Elements
    private final Span totalAppsValue = new Span("0");
    private final Span totalAppsSubtext = new Span("All recorded submissions");

    private final Span inProgressValue = new Span("0");
    private final Span inProgressSubtext = new Span("Across active recruitment stages");

    private final Span offeredValue = new Span("0");
    private final Span offeredSubtext = new Span("Applications with an offer");

    private final Span rejectedValue = new Span("0");
    private final Span rejectedSubtext = new Span("Closed applications");

    // Active Applications Gauge Elements
    private final Div gaugeSvgContainer = new Div();
    private final Span gaugeCenterValue = new Span("0");
    private final Span gaugeCenterLabel = new Span("Active Applications");
    private final Span appliedCountSpan = new Span("0");
    private final Span screeningCountSpan = new Span("0");
    private final Span techTestCountSpan = new Span("0");
    private final Span interviewCountSpan = new Span("0");

    // Recent Applications Elements
    private final Grid<JobApplication> recentApplicationsGrid = new Grid<>(JobApplication.class, false);
    private final Div emptyStateContainer = new Div();

    public ApplicantDashboard() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("applicant-dashboard");

        createHeaderSection();
        createKpiCardsSection();
        createActiveApplicationsSection();
        createRecentApplicationsSection();
    }

    private void createHeaderSection() {
        Div headerLayout = new Div();
        headerLayout.addClassName("applicant-header-layout");

        H2 title = new H2("Dashboard");
        title.addClassName("applicant-dashboard-title");

        Span subtitle = new Span("Your job search at a glance.");
        subtitle.addClassName("applicant-dashboard-subtitle");

        headerLayout.add(title, subtitle);
        add(headerLayout);
    }

    private void createKpiCardsSection() {
        Div kpiRow = new Div();
        kpiRow.addClassName("applicant-kpi-row");

        // Card 1: Total Applications (clean, no decorative icon)
        Div totalCard = createKpiCard("Total Applications", totalAppsValue, "", totalAppsSubtext);

        // Card 2: In Progress (clean, no decorative icon)
        Div inProgressCard = createKpiCard("In Progress", inProgressValue, "", inProgressSubtext);

        // Card 3: Offered (subtle green accent on number)
        Div offeredCard = createKpiCard("Offered", offeredValue, "offered-color", offeredSubtext);

        // Card 4: Rejected (clean, neutral, no large error icon)
        Div rejectedCard = createKpiCard("Rejected", rejectedValue, "", rejectedSubtext);

        kpiRow.add(totalCard, inProgressCard, offeredCard, rejectedCard);
        add(kpiRow);
    }

    private Div createKpiCard(String label, Span valueSpan, String valueColorClass, Span subtextSpan) {
        Div card = new Div();
        card.addClassName("applicant-kpi-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("applicant-kpi-label");

        valueSpan.addClassName("applicant-kpi-value");
        if (valueColorClass != null && !valueColorClass.isBlank()) {
            valueSpan.addClassName(valueColorClass);
        }

        subtextSpan.addClassName("applicant-kpi-subtext");

        card.add(labelSpan, valueSpan, subtextSpan);
        return card;
    }

    private void createActiveApplicationsSection() {
        Div section = new Div();
        section.addClassName("applicant-section-card");

        // Header: Title on left, subtitle (tanpa badge In Progress redundan)
        Div header = new Div();
        header.addClassName("applicant-section-header");

        Div titleGroup = new Div();
        titleGroup.addClassName("applicant-section-title-group");

        H3 title = new H3("Active Applications");
        title.addClassName("applicant-section-title");

        Span subtitle = new Span("See where your active applications currently stand.");
        subtitle.addClassName("applicant-section-subtitle");

        titleGroup.add(title, subtitle);
        header.add(titleGroup);

        // Content layout: Left = Gauge Area (35-40%), Right = Breakdown Area (60-65%)
        Div contentLayout = new Div();
        contentLayout.addClassName("applicant-gauge-content-layout");

        // 1. Gauge Area (Left, ~35-40%)
        Div gaugeArea = new Div();
        gaugeArea.addClassName("applicant-gauge-area");

        Div gaugeBox = new Div();
        gaugeBox.addClassName("applicant-gauge-box");

        gaugeSvgContainer.addClassName("applicant-gauge-svg-container");

        Div centerTextGroup = new Div();
        centerTextGroup.addClassName("applicant-gauge-center-text");

        gaugeCenterValue.addClassName("applicant-gauge-center-number");
        gaugeCenterLabel.addClassName("applicant-gauge-center-label");

        centerTextGroup.add(gaugeCenterValue, gaugeCenterLabel);
        gaugeBox.add(gaugeSvgContainer, centerTextGroup);
        gaugeArea.add(gaugeBox);

        // 2. Breakdown Area (Right, ~60-65%, 2x2 grid)
        Div breakdownArea = new Div();
        breakdownArea.addClassName("applicant-breakdown-area");

        Div appliedItem = createLegendItem("Applied", appliedCountSpan, "legend-applied");
        Div screeningItem = createLegendItem("Screening", screeningCountSpan, "legend-screening");
        Div techTestItem = createLegendItem("Technical Test", techTestCountSpan, "legend-tech-test");
        Div interviewItem = createLegendItem("Interview", interviewCountSpan, "legend-interview");

        breakdownArea.add(appliedItem, screeningItem, techTestItem, interviewItem);

        contentLayout.add(gaugeArea, breakdownArea);
        section.add(header, contentLayout);
        add(section);
    }

    private Div createLegendItem(String stageLabel, Span countSpan, String colorClass) {
        Div item = new Div();
        item.addClassName("applicant-legend-item");

        Div dot = new Div();
        dot.addClassNames("applicant-legend-dot", colorClass);

        Span label = new Span(stageLabel);
        label.addClassName("applicant-legend-label");

        countSpan.addClassName("applicant-legend-count");

        item.add(dot, label, countSpan);
        return item;
    }

    private void createRecentApplicationsSection() {
        Div section = new Div();
        section.addClassName("applicant-section-card");

        // Header with "View all applications" text link
        Div header = new Div();
        header.addClassName("applicant-section-header");

        Div titleGroup = new Div();
        titleGroup.addClassName("applicant-section-title-group");

        H3 title = new H3("Recent Applications");
        title.addClassName("applicant-section-title");

        Span subtitle = new Span("Your latest job applications and their current recruitment status.");
        subtitle.addClassName("applicant-section-subtitle");

        titleGroup.add(title, subtitle);

        Span viewAllLink = new Span("View all applications");
        viewAllLink.addClassName("applicant-view-all-link");
        viewAllLink.addClickListener(event -> UI.getCurrent().navigate(ApplicationsView.class));

        header.add(titleGroup, viewAllLink);

        // Empty state container
        emptyStateContainer.addClassName("applicant-empty-state");
        Paragraph emptyTitle = new Paragraph("Belum ada lamaran.");
        emptyTitle.addClassName("applicant-empty-title");
        Paragraph emptyDesc = new Paragraph("Saat Anda melamar lowongan pekerjaan, data lamaran Anda akan muncul di sini.");
        emptyDesc.addClassName("applicant-empty-desc");
        emptyStateContainer.add(emptyTitle, emptyDesc);

        configureRecentApplicationsGrid();

        section.add(header, emptyStateContainer, recentApplicationsGrid);
        add(section);
    }

    private void configureRecentApplicationsGrid() {
        recentApplicationsGrid.addClassName("applicant-grid");
        recentApplicationsGrid.setWidthFull();
        recentApplicationsGrid.setAllRowsVisible(true);

        // Column 1: COMPANY (Avatar initials + Name + Location)
        recentApplicationsGrid.addComponentColumn(jobApplication -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            String company = "Perusahaan Tidak Tersedia";
            String location = null;

            if (jobApplication.getJobPosting() != null) {
                if (jobApplication.getJobPosting().getCompanyName() != null && !jobApplication.getJobPosting().getCompanyName().isBlank()) {
                    company = jobApplication.getJobPosting().getCompanyName();
                }
                location = jobApplication.getJobPosting().getLocation();
            } else if (jobApplication.getCompanyName() != null && !jobApplication.getCompanyName().isBlank()) {
                company = jobApplication.getCompanyName();
            }

            Span avatar = new Span(getInitials(company));
            avatar.addClassName("applicant-company-avatar");

            Div textGroup = new Div();
            textGroup.addClassName("applicant-company-text-group");

            Span nameSpan = new Span(company);
            nameSpan.addClassName("applicant-company-name");
            textGroup.add(nameSpan);

            if (location != null && !location.isBlank()) {
                Span locationSpan = new Span(location);
                locationSpan.addClassName("applicant-company-location");
                textGroup.add(locationSpan);
            }

            layout.add(avatar, textGroup);
            return layout;
        }).setHeader("COMPANY").setFlexGrow(2);

        // Column 2: POSITION
        recentApplicationsGrid.addComponentColumn(jobApplication -> {
            String position = "-";
            if (jobApplication.getJobPosting() != null && jobApplication.getJobPosting().getPosition() != null) {
                position = jobApplication.getJobPosting().getPosition();
            } else if (jobApplication.getPosition() != null) {
                position = jobApplication.getPosition();
            }

            Span posSpan = new Span(position);
            posSpan.addClassName("applicant-table-position");
            return posSpan;
        }).setHeader("POSITION").setFlexGrow(2);

        // Column 3: WORK TYPE
        recentApplicationsGrid.addComponentColumn(jobApplication -> {
            String workType = "-";
            if (jobApplication.getJobPosting() != null && jobApplication.getJobPosting().getTipeKerja() != null) {
                workType = formatWorkType(jobApplication.getJobPosting().getTipeKerja().name());
            } else if (jobApplication.gettipeKerja() != null) {
                workType = formatWorkType(jobApplication.gettipeKerja().name());
            }

            Span badge = new Span(workType);
            badge.addClassName("applicant-work-type-badge");
            return badge;
        }).setHeader("WORK TYPE").setAutoWidth(true).setFlexGrow(1);

        // Column 4: APPLIED DATE
        recentApplicationsGrid.addComponentColumn(jobApplication -> {
            String dateText = "-";
            if (jobApplication.getApplicationDate() != null) {
                dateText = jobApplication.getApplicationDate().format(DATE_FORMATTER);
            }

            Span dateSpan = new Span(dateText);
            dateSpan.addClassName("applicant-table-date");
            return dateSpan;
        }).setHeader("APPLIED DATE").setAutoWidth(true).setFlexGrow(1);

        // Column 5: STATUS
        recentApplicationsGrid.addComponentColumn(this::createStatusBadge)
                .setHeader("STATUS")
                .setAutoWidth(true)
                .setFlexGrow(1);
    }

    public void updateData(User currentUser, List<JobApplication> applications) {
        int totalApplications = 0;
        int inProgress = 0;
        int offered = 0;
        int rejected = 0;

        int appliedCount = 0;
        int screeningCount = 0;
        int technicalTestCount = 0;
        int interviewCount = 0;

        if (applications != null) {
            totalApplications = applications.size();

            for (JobApplication application : applications) {
                ApplicationStatus status = application.getStatus();

                if (status == ApplicationStatus.APPLIED) {
                    appliedCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.SCREENING) {
                    screeningCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.TECHNICAL_TEST) {
                    technicalTestCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.INTERVIEW) {
                    interviewCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.OFFERED) {
                    offered++;
                } else if (status == ApplicationStatus.REJECTED) {
                    rejected++;
                }
            }
        }

        // 1. Update KPI Cards
        totalAppsValue.setText(String.valueOf(totalApplications));
        inProgressValue.setText(String.valueOf(inProgress));
        offeredValue.setText(String.valueOf(offered));
        rejectedValue.setText(String.valueOf(rejected));

        // 2. Update Active Applications (Gauge & Legend)
        gaugeCenterValue.setText(String.valueOf(inProgress));
        if (inProgress == 1) {
            gaugeCenterLabel.setText("Active Application");
        } else {
            gaugeCenterLabel.setText("Active Applications");
        }

        appliedCountSpan.setText(String.valueOf(appliedCount));
        screeningCountSpan.setText(String.valueOf(screeningCount));
        techTestCountSpan.setText(String.valueOf(technicalTestCount));
        interviewCountSpan.setText(String.valueOf(interviewCount));

        updateGauge(appliedCount, screeningCount, technicalTestCount, interviewCount, inProgress);

        // 3. Update Recent Applications (up to 5, latest first)
        if (applications == null || applications.isEmpty()) {
            emptyStateContainer.setVisible(true);
            recentApplicationsGrid.setVisible(false);
        } else {
            emptyStateContainer.setVisible(false);
            recentApplicationsGrid.setVisible(true);

            List<JobApplication> recentList = applications.stream()
                    .sorted((a, b) -> {
                        if (a.getApplicationDate() == null && b.getApplicationDate() == null) return 0;
                        if (a.getApplicationDate() == null) return 1;
                        if (b.getApplicationDate() == null) return -1;
                        return b.getApplicationDate().compareTo(a.getApplicationDate());
                    })
                    .limit(5)
                    .toList();

            recentApplicationsGrid.setItems(recentList);
        }
    }

    private void updateGauge(int applied, int screening, int techTest, int interview, int inProgress) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 190 105\" width=\"100%\" height=\"100%\" class=\"applicant-gauge-svg\" style=\"overflow: visible;\">");

        // Panjang busur setengah lingkaran dengan radius 75: PI * 75 ~= 235.62
        double arcLength = Math.PI * 75.0;

        if (inProgress == 0) {
            // State kosong (0 active): satu busur abu-abu netral
            svg.append("<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"#E2E8F0\" stroke-width=\"11\" stroke-linecap=\"round\"/>");
        } else {
            // Track dasar abu-abu sangat muda
            svg.append("<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"#F1F5F9\" stroke-width=\"11\" stroke-linecap=\"round\"/>");

            // Hitung berapa tahap yang memiliki count > 0
            int nonZeroCount = 0;
            if (applied > 0) nonZeroCount++;
            if (screening > 0) nonZeroCount++;
            if (techTest > 0) nonZeroCount++;
            if (interview > 0) nonZeroCount++;

            // Jika lebih dari 1 tahap, buat celah 3.0px antar segmen
            double gap = (nonZeroCount > 1) ? 3.0 : 0.0;
            double totalGap = (nonZeroCount > 1) ? (nonZeroCount - 1) * gap : 0.0;
            double usableLength = arcLength - totalGap;

            boolean isSingleSegment = (nonZeroCount == 1);
            double currentOffset = 0.0;

            // 1. Applied (Biru #3B82F6)
            if (applied > 0) {
                double segLength = ((double) applied / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#3B82F6", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 2. Screening (Ungu #8B5CF6)
            if (screening > 0) {
                double segLength = ((double) screening / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#8B5CF6", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 3. Technical Test (Amber #F59E0B)
            if (techTest > 0) {
                double segLength = ((double) techTest / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#F59E0B", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 4. Interview (Cyan #06B6D4)
            if (interview > 0) {
                double segLength = ((double) interview / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#06B6D4", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }
        }

        svg.append("</svg>");
        gaugeSvgContainer.getElement().setProperty("innerHTML", svg.toString());
    }

    private String createGaugeSegmentPath(String colorHex, double offset, double length, boolean isSingleSegment) {
        String linecap = isSingleSegment ? "round" : "butt";
        return String.format(
                Locale.US,
                "<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"%s\" stroke-width=\"11\" stroke-dasharray=\"0 %.2f %.2f 1000\" stroke-linecap=\"%s\"/>",
                colorHex,
                offset,
                length,
                linecap
        );
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

    private String formatWorkType(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "-";
        }
        if (typeName.equalsIgnoreCase("WFH")) {
            return "Remote";
        } else if (typeName.equalsIgnoreCase("WFO")) {
            return "Onsite";
        } else if (typeName.equalsIgnoreCase("HYBRID")) {
            return "Hybrid";
        }
        return typeName;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "CO";
        }
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
