package com.example.view.component;

import com.example.entity.ApplicationTipe;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.SavedJob;
import com.example.entity.User;
import com.example.service.JobApplicationService;
import com.example.service.SavedJobService;
import com.example.view.ApplyJobView;
import com.example.view.JobDetailView;
import com.example.view.PelamarView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ApplicantSavedJobs extends Div {

    private final User currentUser;
    private final SavedJobService savedJobService;
    private final JobApplicationService jobApplicationService;
    private final Consumer<Integer> onSavedCountChanged;

    private final TextField searchField = new TextField();
    private final Span resultCountSpan = new Span();
    private final Div toolbar = new Div();
    private final Div cardsContainer = new Div();
    private final Div emptyStateContainer = new Div();

    private List<SavedJob> allSavedJobs = new ArrayList<>();

    public ApplicantSavedJobs(
            User currentUser,
            SavedJobService savedJobService,
            JobApplicationService jobApplicationService,
            Consumer<Integer> onSavedCountChanged) {
        this.currentUser = currentUser;
        this.savedJobService = savedJobService;
        this.jobApplicationService = jobApplicationService;
        this.onSavedCountChanged = onSavedCountChanged;

        addClassName("applicant-saved-jobs");
        setWidthFull();

        createToolbar();
        cardsContainer.addClassName("applicant-saved-cards-stack");
        emptyStateContainer.setWidthFull();

        add(toolbar, cardsContainer, emptyStateContainer);

        loadDataAndRender();
    }

    public void loadDataAndRender() {
        if (currentUser == null) {
            allSavedJobs = new ArrayList<>();
        } else {
            allSavedJobs = new ArrayList<>(savedJobService.getSavedJobs(currentUser));
        }
        applyFilter();
        if (onSavedCountChanged != null) {
            onSavedCountChanged.accept(allSavedJobs.size());
        }
    }

    private void createToolbar() {
        toolbar.addClassName("applicant-saved-toolbar");

        searchField.setPlaceholder("Search saved jobs...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("applicant-saved-search-input");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> applyFilter());

        resultCountSpan.addClassName("applicant-saved-count");

        toolbar.add(searchField, resultCountSpan);
    }

    private void applyFilter() {
        cardsContainer.removeAll();
        emptyStateContainer.removeAll();

        if (allSavedJobs.isEmpty()) {
            toolbar.setVisible(false);
            cardsContainer.setVisible(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.add(createGlobalEmptyState());
            return;
        }

        toolbar.setVisible(true);
        cardsContainer.setVisible(true);
        emptyStateContainer.setVisible(false);

        String keyword = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        List<SavedJob> filtered = new ArrayList<>();

        for (SavedJob savedJob : allSavedJobs) {
            JobPosting job = savedJob.getJobPosting();
            if (job == null) {
                continue;
            }

            if (keyword.isEmpty()) {
                filtered.add(savedJob);
            } else {
                boolean matchPosition = job.getPosition() != null && job.getPosition().toLowerCase().contains(keyword);
                boolean matchCompany = job.getCompanyName() != null && job.getCompanyName().toLowerCase().contains(keyword);
                if (matchPosition || matchCompany) {
                    filtered.add(savedJob);
                }
            }
        }

        resultCountSpan.setText("Showing " + filtered.size() + " saved " + (filtered.size() == 1 ? "opportunity" : "opportunities"));

        if (filtered.isEmpty()) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.add(createSearchEmptyState());
        } else {
            for (SavedJob savedJob : filtered) {
                cardsContainer.add(createCard(savedJob));
            }
        }
    }

    private Div createCard(SavedJob savedJob) {
        JobPosting job = savedJob.getJobPosting();
        Div card = new Div();
        card.addClassName("applicant-saved-card");

        boolean isActive = job != null && job.isActive();
        if (!isActive) {
            card.addClassName("closed");
        }

        String companyText = job != null && job.getCompanyName() != null && !job.getCompanyName().isBlank()
                ? job.getCompanyName()
                : "Company";

        String positionText = job != null && job.getPosition() != null && !job.getPosition().isBlank()
                ? job.getPosition()
                : "Job Opening";

        // Top body section: Avatar on left, Info on right
        Div body = new Div();
        body.addClassName("applicant-saved-card-body");

        // Small company initials box
        Span avatar = new Span(getCompanyInitials(companyText));
        avatar.addClassName("applicant-saved-company-avatar");
        body.add(avatar);

        // Info container
        Div info = new Div();
        info.addClassName("applicant-saved-card-info");

        // 1. Position Title + Closed badge (if inactive)
        Div titleRow = new Div();
        titleRow.addClassName("applicant-saved-card-title-row");

        H3 positionTitle = new H3(positionText);
        positionTitle.addClassName("applicant-saved-card-position");
        titleRow.add(positionTitle);

        if (!isActive) {
            Span closedBadge = new Span("Closed");
            closedBadge.addClassName("applicant-saved-closed-badge");
            titleRow.add(closedBadge);
        }
        info.add(titleRow);

        // 2. Company + Location sub-info
        Div companyLocRow = new Div();
        companyLocRow.addClassName("applicant-saved-card-company-loc");

        Span companySpan = new Span(companyText);
        companySpan.addClassName("applicant-saved-card-company");
        companyLocRow.add(companySpan);

        if (job != null && job.getLocation() != null && !job.getLocation().isBlank()) {
            Span dot = new Span("·");
            dot.addClassName("applicant-saved-separator");

            Span locItem = new Span();
            locItem.addClassName("applicant-saved-loc-item");
            Html locIcon = new Html("<iconify-icon icon=\"lucide:map-pin\" class=\"applicant-saved-meta-icon\"></iconify-icon>");
            Span locText = new Span(job.getLocation());
            locItem.add(locIcon, locText);

            companyLocRow.add(dot, locItem);
        }
        info.add(companyLocRow);

        // 3. Compact Badges row: Work Type, Job Type, Salary
        Div metaRow = new Div();
        metaRow.addClassName("applicant-saved-card-meta");

        if (job != null && job.getTipeKerja() != null) {
            Span workTypeBadge = new Span(formatWorkType(job.getTipeKerja()));
            workTypeBadge.addClassName("applicant-saved-badge");
            metaRow.add(workTypeBadge);
        }

        if (job != null && job.getJobTipe() != null) {
            Span jobTypeBadge = new Span(formatJobType(job.getJobTipe()));
            jobTypeBadge.addClassName("applicant-saved-badge");
            metaRow.add(jobTypeBadge);
        }

        if (job != null) {
            String salaryText = formatSalaryBadge(job.getSalaryMin(), job.getSalaryMax());
            if (salaryText != null) {
                Span salaryBadge = new Span(salaryText);
                salaryBadge.addClassName("applicant-saved-salary-badge");
                metaRow.add(salaryBadge);
            }
        }
        info.add(metaRow);
        body.add(info);

        // Bottom Footer row: Saved Date on left, Action buttons on right
        Div footer = new Div();
        footer.addClassName("applicant-saved-card-footer");

        if (savedJob.getSavedAt() != null) {
            Span savedDateSpan = new Span("Saved " + formatSavedDate(savedJob.getSavedAt()));
            savedDateSpan.addClassName("applicant-saved-date");
            footer.add(savedDateSpan);
        } else {
            footer.add(new Span());
        }

        // Actions: Remove, View Details, Apply Now / Applied
        Div actionsGroup = new Div();
        actionsGroup.addClassName("applicant-saved-card-actions");

        Button removeBtn = new Button("Remove");
        removeBtn.setIcon(new Html("<iconify-icon icon=\"lucide:trash-2\" class=\"applicant-saved-remove-icon\"></iconify-icon>"));
        removeBtn.addClassName("applicant-saved-remove-btn");
        removeBtn.setTooltipText("Remove from saved jobs");
        removeBtn.setAriaLabel("Remove from saved jobs");

        removeBtn.addClickListener(event -> {
            try {
                savedJobService.unsaveJob(currentUser, job);
                Notification.show("Lowongan dihapus dari Saved Jobs");
                allSavedJobs.remove(savedJob);
                applyFilter();
                if (onSavedCountChanged != null) {
                    onSavedCountChanged.accept(allSavedJobs.size());
                }
            } catch (Exception ex) {
                Notification.show("Gagal menghapus lowongan dari Saved Jobs. Silakan coba lagi.");
            }
        });
        actionsGroup.add(removeBtn);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.addClassName("applicant-saved-detail-btn");
        if (isActive && job != null && job.getId() != null) {
            viewDetailsBtn.addClickListener(event -> UI.getCurrent().navigate(JobDetailView.class, job.getId()));
        } else {
            viewDetailsBtn.setEnabled(false);
            viewDetailsBtn.setTooltipText("Lowongan sudah ditutup");
        }
        actionsGroup.add(viewDetailsBtn);

        if (isActive && job != null && job.getId() != null) {
            boolean sudahMelamar = jobApplicationService.hasApplied(currentUser, job);
            Button applyBtn = new Button();
            if (sudahMelamar) {
                applyBtn.setText("Applied");
                applyBtn.setEnabled(false);
                applyBtn.addClassName("applicant-saved-applied-btn");
            } else {
                applyBtn.setText("Apply Now");
                applyBtn.addClassName("applicant-saved-apply-btn");
                applyBtn.addClickListener(event -> UI.getCurrent().navigate(ApplyJobView.class, job.getId()));
            }
            actionsGroup.add(applyBtn);
        } else {
            Button closedBtn = new Button("Apply Now");
            closedBtn.setEnabled(false);
            closedBtn.addClassName("applicant-saved-apply-btn");
            closedBtn.setTooltipText("Lowongan sudah ditutup");
            actionsGroup.add(closedBtn);
        }

        footer.add(actionsGroup);

        card.add(body, footer);
        return card;
    }

    private Div createGlobalEmptyState() {
        Div emptyCard = new Div();
        emptyCard.addClassName("applicant-saved-empty-state");

        Html icon = new Html("<iconify-icon icon=\"lucide:bookmark\" class=\"applicant-saved-empty-icon\"></iconify-icon>");
        H3 title = new H3("No Saved Jobs Yet");
        title.addClassName("applicant-saved-empty-title");

        Paragraph desc = new Paragraph("Save jobs you're interested in so you can find them here later.");
        desc.addClassName("applicant-saved-empty-desc");

        Button browseBtn = new Button("Browse Jobs", event -> UI.getCurrent().navigate(PelamarView.class));
        browseBtn.addClassName("applicant-saved-browse-btn");

        emptyCard.add(icon, title, desc, browseBtn);
        return emptyCard;
    }

    private Div createSearchEmptyState() {
        Div emptyCard = new Div();
        emptyCard.addClassName("applicant-saved-search-empty");

        H3 title = new H3("No saved jobs match your search.");
        title.addClassName("applicant-saved-search-empty-title");

        Paragraph desc = new Paragraph("Try adjusting your search terms.");
        desc.addClassName("applicant-saved-search-empty-desc");

        emptyCard.add(title, desc);
        return emptyCard;
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

    private String formatSavedDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
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
