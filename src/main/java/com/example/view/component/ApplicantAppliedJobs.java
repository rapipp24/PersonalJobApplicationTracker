package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.ApplicationTipe;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.User;
import com.example.service.JobApplicationService;
import com.example.view.JobDetailView;
import com.example.view.PelamarView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ApplicantAppliedJobs extends Div {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final User currentUser;
    private final JobApplicationService jobApplicationService;
    private final Consumer<Integer> onAppliedCountChanged;

    private final TextField searchField = new TextField();
    private final Select<ApplicationStatus> statusSelect = new Select<>();
    private final Span resultCountSpan = new Span();
    private final Div toolbar = new Div();
    private final Div cardsContainer = new Div();
    private final Div emptyStateContainer = new Div();

    private List<JobApplication> allApplications = new ArrayList<>();

    public ApplicantAppliedJobs(
            User currentUser,
            JobApplicationService jobApplicationService,
            Consumer<Integer> onAppliedCountChanged) {
        this.currentUser = currentUser;
        this.jobApplicationService = jobApplicationService;
        this.onAppliedCountChanged = onAppliedCountChanged;

        addClassName("applicant-applied-jobs");
        setWidthFull();

        createToolbar();
        cardsContainer.addClassName("applicant-applied-cards-stack");
        emptyStateContainer.setWidthFull();

        add(toolbar, cardsContainer, emptyStateContainer);

        loadDataAndRender();
    }

    public void loadDataAndRender() {
        if (currentUser == null) {
            allApplications = new ArrayList<>();
        } else {
            allApplications = new ArrayList<>(jobApplicationService.findByApplicant(currentUser));
        }
        applyFilter();
        if (onAppliedCountChanged != null) {
            onAppliedCountChanged.accept(allApplications.size());
        }
    }

    private void createToolbar() {
        toolbar.addClassName("applicant-applied-toolbar");

        searchField.setPlaceholder("Search by company or job position...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("applicant-applied-search-input");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> applyFilter());

        statusSelect.addClassName("applicant-applied-status-select");
        statusSelect.setItems(ApplicationStatus.values());
        statusSelect.setEmptySelectionAllowed(true);
        statusSelect.setEmptySelectionCaption("Status: All");
        statusSelect.setItemLabelGenerator(status -> {
            if (status == null) {
                return "Status: All";
            }
            switch (status) {
                case APPLIED:
                    return "Applied";
                case SCREENING:
                    return "Screening";
                case TECHNICAL_TEST:
                    return "Technical Test";
                case INTERVIEW:
                    return "Interview";
                case OFFERED:
                    return "Offered";
                case REJECTED:
                    return "Rejected";
                default:
                    return status.name();
            }
        });
        statusSelect.addValueChangeListener(event -> applyFilter());

        resultCountSpan.addClassName("applicant-applied-count");

        Anchor exportLink = createExportButton();

        Div leftGroup = new Div(searchField, statusSelect);
        leftGroup.addClassName("applicant-applied-toolbar-left");

        Div rightGroup = new Div(resultCountSpan, exportLink);
        rightGroup.addClassName("applicant-applied-toolbar-right");

        toolbar.add(leftGroup, rightGroup);
    }

    private Anchor createExportButton() {
        Button exportButton = new Button("Export CSV");
        exportButton.addClassName("applicant-applied-export-btn");
        exportButton.setIcon(new Html("<iconify-icon icon=\"lucide:download\" class=\"applicant-applied-export-icon\"></iconify-icon>"));

        StreamResource csvResource = new StreamResource("job-applications.csv", () -> {
            List<JobApplication> dataExport;
            if (currentUser != null) {
                dataExport = jobApplicationService.findByApplicant(currentUser);
            } else {
                dataExport = new ArrayList<>();
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Perusahaan,Posisi,Status,Tanggal Melamar,Ekspektasi Gaji,Catatan\n");

            for (JobApplication jobApplication : dataExport) {
                csv.append(jobApplication.getCompanyName() != null ? jobApplication.getCompanyName() : "")
                        .append(",")
                        .append(jobApplication.getPosition() != null ? jobApplication.getPosition() : "")
                        .append(",")
                        .append(jobApplication.getStatus() != null ? jobApplication.getStatus() : "")
                        .append(",")
                        .append(jobApplication.getApplicationDate() != null ? jobApplication.getApplicationDate() : "")
                        .append(",")
                        .append(jobApplication.getExpectedSalary() != null ? jobApplication.getExpectedSalary() : "")
                        .append(",")
                        .append(jobApplication.getNotes() != null ? jobApplication.getNotes() : "")
                        .append("\n");
            }

            return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
        });

        Anchor exportLink = new Anchor(csvResource, "");
        exportLink.getElement().setAttribute("download", true);
        exportLink.add(exportButton);

        return exportLink;
    }

    private void applyFilter() {
        cardsContainer.removeAll();
        emptyStateContainer.removeAll();

        if (allApplications.isEmpty()) {
            toolbar.setVisible(false);
            cardsContainer.setVisible(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.add(createGlobalEmptyState());
            return;
        }

        toolbar.setVisible(true);
        cardsContainer.setVisible(true);
        emptyStateContainer.setVisible(false);

        String keyword = searchField.getValue() != null ? searchField.getValue().trim() : "";
        ApplicationStatus status = statusSelect.getValue();

        List<JobApplication> filtered;
        if (currentUser != null) {
            filtered = new ArrayList<>(jobApplicationService.findByApplicant(currentUser, keyword, status));
        } else {
            filtered = new ArrayList<>();
        }

        // Newest application date first, null dates last
        filtered.sort((a, b) -> {
            if (a.getApplicationDate() == null && b.getApplicationDate() == null) return 0;
            if (a.getApplicationDate() == null) return 1;
            if (b.getApplicationDate() == null) return -1;
            return b.getApplicationDate().compareTo(a.getApplicationDate());
        });

        resultCountSpan.setText("Showing " + filtered.size() + " " + (filtered.size() == 1 ? "application" : "applications"));

        if (filtered.isEmpty()) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.add(createFilterEmptyState());
        } else {
            for (JobApplication jobApplication : filtered) {
                cardsContainer.add(createCard(jobApplication));
            }
        }
    }

    private Div createCard(JobApplication jobApplication) {
        JobPosting job = jobApplication.getJobPosting();
        Div card = new Div();
        card.addClassName("applicant-applied-card");

        boolean isJobClosed = job != null && !job.isActive();
        if (isJobClosed) {
            card.addClassName("closed");
        }

        String companyText = "Company";
        if (jobApplication.getCompanyName() != null && !jobApplication.getCompanyName().isBlank()) {
            companyText = jobApplication.getCompanyName().trim();
        } else if (job != null && job.getCompanyName() != null && !job.getCompanyName().isBlank()) {
            companyText = job.getCompanyName().trim();
        }

        String positionText = "Job Opening";
        if (jobApplication.getPosition() != null && !jobApplication.getPosition().isBlank()) {
            positionText = jobApplication.getPosition().trim();
        } else if (job != null && job.getPosition() != null && !job.getPosition().isBlank()) {
            positionText = job.getPosition().trim();
        }

        // Left section: Avatar + Information Block (Vertically Centered)
        Div leftBlock = new Div();
        leftBlock.addClassName("applicant-applied-card-left");

        Span avatar = new Span(getCompanyInitials(companyText));
        avatar.addClassName("applicant-applied-company-avatar");
        leftBlock.add(avatar);

        Div info = new Div();
        info.addClassName("applicant-applied-card-info");

        // 1. Position Title (Primary text)
        H3 positionTitle = new H3(positionText);
        positionTitle.addClassName("applicant-applied-card-position");
        info.add(positionTitle);

        // 2. Company + Location
        Div companyLocRow = new Div();
        companyLocRow.addClassName("applicant-applied-card-company-loc");

        Span companySpan = new Span(companyText);
        companySpan.addClassName("applicant-applied-card-company");
        companyLocRow.add(companySpan);

        if (job != null && job.getLocation() != null && !job.getLocation().isBlank()) {
            Span dot = new Span("·");
            dot.addClassName("applicant-applied-separator");

            Span locItem = new Span();
            locItem.addClassName("applicant-applied-loc-item");
            Html locIcon = new Html("<iconify-icon icon=\"lucide:map-pin\" class=\"applicant-applied-meta-icon\"></iconify-icon>");
            Span locText = new Span(job.getLocation().trim());
            locItem.add(locIcon, locText);

            companyLocRow.add(dot, locItem);
        }
        info.add(companyLocRow);

        // 3. Compact Metadata row: Work Type, Job Type, Expected Salary, Applied Date
        Div metaRow = new Div();
        metaRow.addClassName("applicant-applied-card-meta");

        ApplicationTipe workType = jobApplication.gettipeKerja();
        if (workType == null && job != null) {
            workType = job.getTipeKerja();
        }
        if (workType != null) {
            Span workTypeBadge = new Span(formatWorkType(workType));
            workTypeBadge.addClassName("applicant-applied-badge");
            metaRow.add(workTypeBadge);
        }

        if (job != null && job.getJobTipe() != null) {
            Span jobTypeBadge = new Span(formatJobType(job.getJobTipe()));
            jobTypeBadge.addClassName("applicant-applied-badge");
            metaRow.add(jobTypeBadge);
        }

        if (jobApplication.getExpectedSalary() != null) {
            Span salaryBadge = new Span(formatExpectedSalary(jobApplication.getExpectedSalary()));
            salaryBadge.addClassName("applicant-applied-salary-badge");
            metaRow.add(salaryBadge);
        }

        if (jobApplication.getApplicationDate() != null) {
            Span appliedDateSpan = new Span("Applied " + jobApplication.getApplicationDate().format(DATE_FORMATTER));
            appliedDateSpan.addClassName("applicant-applied-meta-date");
            metaRow.add(appliedDateSpan);
        }

        info.add(metaRow);
        leftBlock.add(info);

        // Right section: Status Badges (top) + View Details Action (bottom)
        Div rightBlock = new Div();
        rightBlock.addClassName("applicant-applied-card-right");

        Div badgeGroup = new Div();
        badgeGroup.addClassName("applicant-applied-card-badges");

        Span statusBadge = createStatusBadge(jobApplication.getStatus());
        badgeGroup.add(statusBadge);

        if (isJobClosed) {
            Span closedBadge = new Span("Closed");
            closedBadge.addClassName("applicant-applied-closed-badge");
            badgeGroup.add(closedBadge);
        }
        rightBlock.add(badgeGroup);

        if (job != null && job.getId() != null) {
            Button viewDetailsBtn = new Button("View Details");
            viewDetailsBtn.addClassName("applicant-applied-detail-btn");
            if (job.isActive()) {
                viewDetailsBtn.addClickListener(event -> UI.getCurrent().navigate(JobDetailView.class, job.getId()));
            } else {
                viewDetailsBtn.setEnabled(false);
                viewDetailsBtn.setTooltipText("Lowongan sudah ditutup");
            }
            rightBlock.add(viewDetailsBtn);
        }

        card.add(leftBlock, rightBlock);
        return card;
    }

    private Div createGlobalEmptyState() {
        Div emptyCard = new Div();
        emptyCard.addClassName("applicant-applied-empty-state");

        Html icon = new Html("<iconify-icon icon=\"lucide:file-text\" class=\"applicant-applied-empty-icon\"></iconify-icon>");
        H3 title = new H3("No Applications Yet");
        title.addClassName("applicant-applied-empty-title");

        Paragraph desc = new Paragraph("You haven't applied to any jobs yet.");
        desc.addClassName("applicant-applied-empty-desc");

        Button browseBtn = new Button("Browse Jobs", event -> UI.getCurrent().navigate(PelamarView.class));
        browseBtn.addClassName("applicant-applied-browse-btn");

        emptyCard.add(icon, title, desc, browseBtn);
        return emptyCard;
    }

    private Div createFilterEmptyState() {
        Div emptyCard = new Div();
        emptyCard.addClassName("applicant-applied-search-empty");

        H3 title = new H3("No applications match your filters.");
        title.addClassName("applicant-applied-search-empty-title");

        Paragraph desc = new Paragraph("Try adjusting your search or status filter.");
        desc.addClassName("applicant-applied-search-empty-desc");

        emptyCard.add(title, desc);
        return emptyCard;
    }

    private Span createStatusBadge(ApplicationStatus status) {
        if (status == null) {
            Span badge = new Span("-");
            badge.addClassName("applicant-applied-status-badge");
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
        badge.addClassName("applicant-applied-status-badge");
        badge.addClassName("applicant-applied-status-" + status.name().toLowerCase().replace('_', '-'));
        return badge;
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

    private String formatExpectedSalary(Long salary) {
        if (salary == null) {
            return "";
        }
        return "Expected: Rp " + String.format(Locale.US, "%,d", salary).replace(',', '.');
    }

    private String getCompanyInitials(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return "CO";
        }
        String trimmed = companyName.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
