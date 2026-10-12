package com.example.view;

import com.example.entity.ApplicationTipe;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.example.service.SavedJobService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "pelamar", layout = MainLayout.class)
@PageTitle("Applicant Area | Job Tracker")
@RolesAllowed({"PELAMAR", "ADMIN"})
public class PelamarView extends VerticalLayout {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final SavedJobService savedJobService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    // Search and filter fields
    private final TextField searchField = new TextField();
    private final TextField locationField = new TextField();
    private final Select<String> workTypeSelect = new Select<>();
    private final Select<String> jobTypeSelect = new Select<>();
    private final Select<String> sortBySelect = new Select<>();
    private final Span jobCountSpan = new Span();

    // Containers
    private final Div cardsContainer = new Div();
    private final Div paginationContainer = new Div();

    // Data and pagination state
    private List<JobPosting> allActiveJobs = new ArrayList<>();
    private List<JobPosting> filteredJobs = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 6;

    public PelamarView(
            JobPostingService jobPostingService,
            JobApplicationService jobApplicationService,
            SavedJobService savedJobService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.savedJobService = savedJobService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        addClassName("applicant-browse-jobs");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        createHeaderSection();
        createSearchAndFilterSection();
        createCardsSection();

        loadDataAndApplyFilter();
    }

    private void createHeaderSection() {
        Div headerLayout = new Div();
        headerLayout.addClassName("applicant-browse-header");

        H2 title = new H2("Browse Jobs");
        title.addClassName("applicant-browse-title");

        Paragraph subtitle = new Paragraph("Explore open positions and apply directly to matching opportunities.");
        subtitle.addClassName("applicant-browse-subtitle");

        headerLayout.add(title, subtitle);
        add(headerLayout);
    }

    private void createSearchAndFilterSection() {
        Div filterCard = new Div();
        filterCard.addClassName("applicant-browse-filter-card");

        // Top Row: Keyword Search, Location Search, Search Button
        Div searchRow = new Div();
        searchRow.addClassName("applicant-browse-search-row");

        searchField.setPlaceholder("Search position, skill, or keyword...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("applicant-browse-search-input");
        searchField.setClearButtonVisible(true);
        searchField.addKeyPressListener(Key.ENTER, event -> applyFilter());

        locationField.setPlaceholder("Search location...");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.addClassName("applicant-browse-location-input");
        locationField.setClearButtonVisible(true);
        locationField.addKeyPressListener(Key.ENTER, event -> applyFilter());

        Button searchButton = new Button("Search");
        searchButton.addClassName("applicant-browse-search-btn");
        searchButton.addClickListener(event -> applyFilter());

        searchRow.add(searchField, locationField, searchButton);

        // Bottom Row: Working Type, Job Type, Result Count
        Div filterRow = new Div();
        filterRow.addClassName("applicant-browse-filter-row");

        Div filterGroup = new Div();
        filterGroup.addClassName("applicant-browse-filter-group");

        // Working Type Filter (Label + Dropdown)
        HorizontalLayout workingTypeFilter = new HorizontalLayout();
        workingTypeFilter.addClassName("applicant-filter-item");
        workingTypeFilter.setAlignItems(Alignment.CENTER);
        workingTypeFilter.setSpacing(true);

        Span workingTypeLabel = new Span("Working Type");
        workingTypeLabel.addClassName("applicant-filter-label");

        workTypeSelect.setItems("All", "Onsite", "Remote", "Hybrid");
        workTypeSelect.setValue("All");
        workTypeSelect.addClassName("applicant-browse-filter-select");
        workTypeSelect.addClassName("applicant-work-type-select");
        workTypeSelect.addValueChangeListener(event -> applyFilter());

        workingTypeFilter.add(workingTypeLabel, workTypeSelect);

        // Job Type Filter (Label + Dropdown)
        HorizontalLayout jobTypeFilter = new HorizontalLayout();
        jobTypeFilter.addClassName("applicant-filter-item");
        jobTypeFilter.setAlignItems(Alignment.CENTER);
        jobTypeFilter.setSpacing(true);

        Span jobTypeLabel = new Span("Job Type");
        jobTypeLabel.addClassName("applicant-filter-label");

        jobTypeSelect.setItems("All", "Full-time", "Part-time", "Contract", "Internship", "Freelance");
        jobTypeSelect.setValue("All");
        jobTypeSelect.addClassName("applicant-browse-filter-select");
        jobTypeSelect.addClassName("applicant-job-type-select");
        jobTypeSelect.addValueChangeListener(event -> applyFilter());

        jobTypeFilter.add(jobTypeLabel, jobTypeSelect);

        // Sort by Filter (Label + Dropdown)
        HorizontalLayout sortByFilter = new HorizontalLayout();
        sortByFilter.addClassName("applicant-filter-item");
        sortByFilter.setAlignItems(Alignment.CENTER);
        sortByFilter.setSpacing(true);

        Span sortByLabel = new Span("Sort by");
        sortByLabel.addClassName("applicant-filter-label");

        sortBySelect.setItems("Newest", "Oldest", "Salary: High to Low", "Salary: Low to High");
        sortBySelect.setValue("Newest");
        sortBySelect.addClassName("applicant-browse-filter-select");
        sortBySelect.addClassName("applicant-sort-by-select");
        sortBySelect.addValueChangeListener(event -> applyFilter());

        sortByFilter.add(sortByLabel, sortBySelect);

        filterGroup.add(workingTypeFilter, jobTypeFilter, sortByFilter);

        jobCountSpan.addClassName("applicant-browse-job-count");

        filterRow.add(filterGroup, jobCountSpan);
        filterCard.add(searchRow, filterRow);

        add(filterCard);
    }

    private void createCardsSection() {
        cardsContainer.addClassName("applicant-jobs-grid");
        paginationContainer.addClassName("applicant-job-pagination");

        add(cardsContainer, paginationContainer);
    }

    private void loadDataAndApplyFilter() {
        allActiveJobs = jobPostingService.findActiveJobPostings();
        applyFilter();
    }

    private void applyFilter() {
        currentPage = 0;
        filteredJobs = new ArrayList<>();

        String keyword = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        String lokasi = locationField.getValue() != null ? locationField.getValue().trim().toLowerCase() : "";
        String tipeKerjaDipilih = workTypeSelect.getValue();
        String jobTipeDipilih = jobTypeSelect.getValue();

        for (JobPosting job : allActiveJobs) {
            boolean cocokKeyword = true;
            if (!keyword.isEmpty()) {
                boolean matchPosition = job.getPosition() != null && job.getPosition().toLowerCase().contains(keyword);
                boolean matchCompany = job.getCompanyName() != null && job.getCompanyName().toLowerCase().contains(keyword);
                boolean matchDesc = job.getDescription() != null && job.getDescription().toLowerCase().contains(keyword);
                boolean matchReq = job.getRequirements() != null && job.getRequirements().toLowerCase().contains(keyword);
                cocokKeyword = matchPosition || matchCompany || matchDesc || matchReq;
            }

            boolean cocokLokasi = true;
            if (!lokasi.isEmpty()) {
                cocokLokasi = job.getLocation() != null && job.getLocation().toLowerCase().contains(lokasi);
            }

            boolean cocokTipeKerja = true;
            if (tipeKerjaDipilih != null && !tipeKerjaDipilih.equals("All")) {
                String friendlyWorkType = formatWorkType(job.getTipeKerja());
                cocokTipeKerja = friendlyWorkType.equalsIgnoreCase(tipeKerjaDipilih);
            }

            boolean cocokJobTipe = true;
            if (jobTipeDipilih != null && !jobTipeDipilih.equals("All")) {
                String friendlyJobType = formatJobType(job.getJobTipe());
                cocokJobTipe = friendlyJobType.equalsIgnoreCase(jobTipeDipilih);
            }

            if (cocokKeyword && cocokLokasi && cocokTipeKerja && cocokJobTipe) {
                filteredJobs.add(job);
            }
        }

        sortFilteredJobs(filteredJobs);

        jobCountSpan.setText("Showing " + filteredJobs.size() + " active job openings");
        renderCurrentPage();
    }

    private void sortFilteredJobs(List<JobPosting> jobs) {
        String selectedSort = sortBySelect.getValue();
        if (selectedSort == null || selectedSort.isEmpty()) {
            selectedSort = "Newest";
        }

        if ("Newest".equals(selectedSort)) {
            jobs.sort((a, b) -> {
                LocalDate dateA = a.getPostedDate();
                LocalDate dateB = b.getPostedDate();
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateB.compareTo(dateA);
            });
        } else if ("Oldest".equals(selectedSort)) {
            jobs.sort((a, b) -> {
                LocalDate dateA = a.getPostedDate();
                LocalDate dateB = b.getPostedDate();
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateA.compareTo(dateB);
            });
        } else if ("Salary: High to Low".equals(selectedSort)) {
            jobs.sort((a, b) -> {
                Long salA = a.getSalaryMax() != null ? a.getSalaryMax() : a.getSalaryMin();
                Long salB = b.getSalaryMax() != null ? b.getSalaryMax() : b.getSalaryMin();
                if (salA == null && salB == null) return 0;
                if (salA == null) return 1;
                if (salB == null) return -1;
                return salB.compareTo(salA);
            });
        } else if ("Salary: Low to High".equals(selectedSort)) {
            jobs.sort((a, b) -> {
                Long salA = a.getSalaryMin() != null ? a.getSalaryMin() : a.getSalaryMax();
                Long salB = b.getSalaryMin() != null ? b.getSalaryMin() : b.getSalaryMax();
                if (salA == null && salB == null) return 0;
                if (salA == null) return 1;
                if (salB == null) return -1;
                return salA.compareTo(salB);
            });
        }
    }

    private void renderCurrentPage() {
        cardsContainer.removeAll();
        paginationContainer.removeAll();

        if (filteredJobs.isEmpty()) {
            Div emptyState = new Div();
            emptyState.addClassName("applicant-job-empty");

            Paragraph title = new Paragraph("No job openings found.");
            title.addClassName("applicant-job-empty-title");

            Paragraph desc = new Paragraph("Try adjusting your search or filters.");
            desc.addClassName("applicant-job-empty-desc");

            emptyState.add(title, desc);
            cardsContainer.add(emptyState);
            return;
        }

        int totalItems = filteredJobs.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            JobPosting job = filteredJobs.get(i);
            cardsContainer.add(createJobCard(job));
        }

        renderPagination(startIndex, endIndex, totalItems, totalPages);
    }

    private Div createJobCard(JobPosting job) {
        Div card = new Div();
        card.addClassName("applicant-job-card");

        // 1. Header (Avatar + Company Info + Active Badge)
        Div headerRow = new Div();
        headerRow.addClassName("applicant-job-card-header");

        Div companyBlock = new Div();
        companyBlock.addClassName("applicant-job-company-block");

        String companyName = job.getCompanyName() != null && !job.getCompanyName().isBlank()
                ? job.getCompanyName()
                : "Company";
        Span avatar = new Span(getCompanyInitials(companyName));
        avatar.addClassName("applicant-job-company-avatar");

        Div companyInfo = new Div();
        companyInfo.addClassName("applicant-job-company-info");

        Span nameSpan = new Span(companyName);
        nameSpan.addClassName("applicant-job-company-name");

        String locationText = job.getLocation() != null && !job.getLocation().isBlank()
                ? job.getLocation()
                : "Indonesia";
        String dateText = formatPostedDate(job.getPostedDate());
        Span metaSpan = new Span(locationText + " · " + dateText);
        metaSpan.addClassName("applicant-job-company-meta");

        companyInfo.add(nameSpan, metaSpan);
        companyBlock.add(avatar, companyInfo);

        headerRow.add(companyBlock);

        boolean isPelamar = authenticationContext.hasRole("PELAMAR");
        User currentUser = null;
        if (isPelamar) {
            try {
                currentUser = getCurrentUser();
            } catch (Exception ignored) {
            }
        }

        if (isPelamar && currentUser != null) {
            boolean isSaved = savedJobService.isJobSaved(currentUser, job);
            Button bookmarkBtn = new Button();
            bookmarkBtn.addClassName("applicant-job-save-btn");
            updateBookmarkButton(bookmarkBtn, isSaved);

            final User applicantUser = currentUser;
            bookmarkBtn.addClickListener(event -> {
                try {
                    boolean nowSaved = savedJobService.toggleSave(applicantUser, job);
                    updateBookmarkButton(bookmarkBtn, nowSaved);
                    Notification.show(nowSaved ? "Lowongan berhasil disimpan" : "Lowongan dihapus dari Saved Jobs");
                } catch (Exception ex) {
                    boolean actualSaved = savedJobService.isJobSaved(applicantUser, job);
                    updateBookmarkButton(bookmarkBtn, actualSaved);
                    Notification.show("Gagal memperbarui status simpan lowongan. Silakan coba lagi.");
                }
            });

            headerRow.add(bookmarkBtn);
        }

        // 2. Position Title
        H3 positionTitle = new H3(job.getPosition() != null ? job.getPosition() : "-");
        positionTitle.addClassName("applicant-job-position");

        // 3. Tags Row (Salary, Work Type, Job Type)
        Div tagsRow = new Div();
        tagsRow.addClassName("applicant-job-tags");

        String salaryText = formatSalaryBadge(job.getSalaryMin(), job.getSalaryMax());
        if (salaryText != null) {
            Span salaryBadge = new Span(salaryText);
            salaryBadge.addClassName("applicant-job-salary");
            tagsRow.add(salaryBadge);
        }

        Span workTypeBadge = new Span(formatWorkType(job.getTipeKerja()));
        workTypeBadge.addClassName("applicant-job-work-type");

        Span jobTypeBadge = new Span(formatJobType(job.getJobTipe()));
        jobTypeBadge.addClassName("applicant-job-type");

        tagsRow.add(workTypeBadge, jobTypeBadge);

        // 4. Description Preview
        Paragraph descPreview = new Paragraph();
        descPreview.addClassName("applicant-job-description");
        if (job.getDescription() != null && !job.getDescription().isBlank()) {
            descPreview.setText(job.getDescription());
        } else {
            descPreview.setText("No description provided.");
        }

        // 5. Footer (View Details on left, Apply on right)
        Div footerRow = new Div();
        footerRow.addClassName("applicant-job-footer");

        Button viewDetailsBtn = new Button("View Details", event -> UI.getCurrent().navigate(JobDetailView.class, job.getId()));
        viewDetailsBtn.addClassName("applicant-job-detail-button");
        footerRow.add(viewDetailsBtn);

        if (isPelamar && currentUser != null) {
            boolean sudahMelamar = jobApplicationService.hasApplied(currentUser, job);

            Button applyButton = new Button();
            if (sudahMelamar) {
                applyButton.setText("Applied");
                applyButton.setEnabled(false);
                applyButton.addClassName("applicant-job-applied-button");
            } else {
                applyButton.setText("Apply Now");
                applyButton.addClassName("applicant-job-apply-button");
                applyButton.addClickListener(event -> UI.getCurrent().navigate(ApplyJobView.class, job.getId()));
            }
            footerRow.add(applyButton);
        }

        card.add(headerRow, positionTitle, tagsRow, descPreview, footerRow);
        return card;
    }

    private void updateBookmarkButton(Button btn, boolean isSaved) {
        if (isSaved) {
            btn.setIcon(new Html("<iconify-icon icon=\"lucide:bookmark\" class=\"applicant-job-save-icon saved\"></iconify-icon>"));
            btn.addClassName("saved");
            btn.setTooltipText("Remove from saved jobs");
            btn.setAriaLabel("Remove from saved jobs");
        } else {
            btn.setIcon(new Html("<iconify-icon icon=\"lucide:bookmark\" class=\"applicant-job-save-icon\"></iconify-icon>"));
            btn.removeClassName("saved");
            btn.setTooltipText("Save job");
            btn.setAriaLabel("Save job");
        }
    }


    private void renderPagination(int startIndex, int endIndex, int totalItems, int totalPages) {
        paginationContainer.removeAll();

        Span infoText = new Span("Showing " + (startIndex + 1) + " to " + endIndex + " of " + totalItems + " active postings");
        infoText.addClassName("applicant-job-pagination-info");

        HorizontalLayout controls = new HorizontalLayout();
        controls.addClassName("applicant-job-pagination-controls");
        controls.setSpacing(true);
        controls.setAlignItems(Alignment.CENTER);

        Button prevBtn = new Button("‹", event -> {
            if (currentPage > 0) {
                currentPage--;
                renderCurrentPage();
            }
        });
        prevBtn.addClassName("applicant-page-btn");
        prevBtn.setEnabled(currentPage > 0);
        controls.add(prevBtn);

        for (int p = 0; p < totalPages; p++) {
            final int pageIndex = p;
            Button pageBtn = new Button(String.valueOf(p + 1), event -> {
                currentPage = pageIndex;
                renderCurrentPage();
            });
            pageBtn.addClassName("applicant-page-btn");
            if (p == currentPage) {
                pageBtn.addClassName("applicant-page-btn-active");
            }
            controls.add(pageBtn);
        }

        Button nextBtn = new Button("›", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                renderCurrentPage();
            }
        });
        nextBtn.addClassName("applicant-page-btn");
        nextBtn.setEnabled(currentPage < totalPages - 1);
        controls.add(nextBtn);

        paginationContainer.add(infoText, controls);
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
        long days = ChronoUnit.DAYS.between(date, LocalDate.now());
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "1 day ago";
        } else if (days > 1 && days <= 30) {
            return days + " days ago";
        }
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
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

    private User getCurrentUser() {
        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }
}
