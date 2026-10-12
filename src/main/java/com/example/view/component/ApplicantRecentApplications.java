package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.view.ApplicationsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ApplicantRecentApplications extends Div {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final Grid<JobApplication> recentApplicationsGrid = new Grid<>(JobApplication.class, false);
    private final Div emptyStateContainer = new Div();

    public ApplicantRecentApplications() {
        addClassName("applicant-section-card");

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

        add(header, emptyStateContainer, recentApplicationsGrid);
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

    public void updateData(List<JobApplication> applications) {
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
