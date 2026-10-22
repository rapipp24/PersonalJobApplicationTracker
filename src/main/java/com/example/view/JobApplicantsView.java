package com.example.view;

import com.example.entity.ApplicationStatus;
import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.service.JobPostingService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "pemberi-lamaran/pelamar", layout = MainLayout.class)
@PageTitle("Daftar Pelamar | Job Tracker")
@RolesAllowed("PEMBERI_LAMARAN")
public class JobApplicantsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private JobPosting currentJobPosting;
    private User currentUser;

    private final Paragraph totalInfo = new Paragraph();
    private final Paragraph emptyNotice = new Paragraph("Belum ada pelamar untuk lowongan ini.");
    private Grid<JobApplication> grid;

    public JobApplicantsView(
            JobPostingService jobPostingService,
            JobApplicationService jobApplicationService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;
    }

    @Override
    public void setParameter(BeforeEvent event, Long jobPostingId) {
        User currentUser = getCurrentUser();
        Company company = currentUser.getCompany();

        // Guard: Akses pelamar hanya untuk Employer dengan Company VERIFIED dan Membership ACTIVE
        if (company == null
                || company.getVerificationStatus() != CompanyVerificationStatus.VERIFIED
                || currentUser.getMembershipStatus() != CompanyMembershipStatus.ACTIVE) {
            event.forwardTo(DashboardView.class);
            return;
        }

        if (jobPostingId == null) {
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        Optional<JobPosting> postingOptional = jobPostingService.findById(jobPostingId);
        if (postingOptional.isEmpty()) {
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        JobPosting jobPosting = postingOptional.get();

        Company postingCompany = jobPosting.getCompany();
        if (postingCompany == null && jobPosting.getEmployer() != null) {
            postingCompany = jobPosting.getEmployer().getCompany();
        }

        if (postingCompany == null || postingCompany.getId() == null || company.getId() == null) {
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        if (!postingCompany.getId().equals(company.getId())) {
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        this.currentJobPosting = jobPosting;
        this.currentUser = currentUser;

        displayApplicants();
    }

    private void displayApplicants() {
        removeAll();

        Button backButton = new Button("Kembali ke Daftar Lowongan", event -> UI.getCurrent().navigate(PemberiLamaranView.class));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        H1 title = new H1("Pelamar — " + currentJobPosting.getPosition());

        Paragraph companyInfo = new Paragraph("Perusahaan: " + currentJobPosting.getCompanyName());

        HorizontalLayout statusLayout = new HorizontalLayout();
        statusLayout.setAlignItems(Alignment.CENTER);
        Span statusLabel = new Span("Status Lowongan: ");
        Span statusBadge = createPostingStatusBadge(currentJobPosting);
        statusLayout.add(statusLabel, statusBadge);

        totalInfo.getStyle().set("font-weight", "600");

        emptyNotice.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        grid = new Grid<>(JobApplication.class, false);
        configureGrid();

        add(backButton, title, companyInfo, statusLayout, totalInfo, emptyNotice, grid);

        refreshApplicants();
    }

    private void configureGrid() {
        grid.addColumn(app -> {
            if (app.getApplicant() != null && app.getApplicant().getName() != null) {
                return app.getApplicant().getName();
            }
            return "Data Pelamar Tidak Tersedia";
        }).setHeader("Nama Pelamar");

        grid.addColumn(app -> {
            if (app.getApplicant() != null && app.getApplicant().getEmail() != null) {
                return app.getApplicant().getEmail();
            }
            return "-";
        }).setHeader("Email Pelamar");

        grid.addColumn(app -> {
            if (app.getApplicationDate() != null) {
                return app.getApplicationDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
            return "-";
        }).setHeader("Tanggal Apply");

        grid.addComponentColumn(this::createApplicationStatusBadge).setHeader("Status");

        grid.addComponentColumn(app -> {
            Button detailButton = new Button("Detail", event -> {
                UI.getCurrent().navigate(ApplicantDetailView.class, app.getId());
            });
            detailButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            Button ubahStatusButton = new Button("Ubah Status", event -> openUpdateStatusDialog(app));
            ubahStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

            HorizontalLayout actions = new HorizontalLayout(detailButton, ubahStatusButton);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Aksi");

        grid.setWidthFull();
    }

    private void openUpdateStatusDialog(JobApplication jobApplication) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ubah Status Lamaran");

        String applicantName;
        if (jobApplication.getApplicant() != null && jobApplication.getApplicant().getName() != null) {
            applicantName = jobApplication.getApplicant().getName();
        } else {
            applicantName = "Data Pelamar Tidak Tersedia";
        }

        String positionName = currentJobPosting != null ? currentJobPosting.getPosition() : jobApplication.getPosition();

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Span pelamarLabel = new Span("Pelamar: " + applicantName);
        pelamarLabel.getStyle().set("font-weight", "500");

        Span posisiLabel = new Span("Posisi: " + (positionName != null ? positionName : "-"));
        posisiLabel.getStyle().set("font-weight", "500");

        HorizontalLayout currentStatusLayout = new HorizontalLayout();
        currentStatusLayout.setAlignItems(Alignment.CENTER);
        Span statusTextLabel = new Span("Status Saat Ini: ");
        Span currentBadge = createApplicationStatusBadge(jobApplication);
        currentStatusLayout.add(statusTextLabel, currentBadge);

        ComboBox<ApplicationStatus> statusComboBox = new ComboBox<>("Status Baru");
        statusComboBox.setItems(ApplicationStatus.values());
        statusComboBox.setItemLabelGenerator(this::formatStatusLabel);
        statusComboBox.setValue(jobApplication.getStatus());
        statusComboBox.setWidthFull();

        content.add(pelamarLabel, posisiLabel, currentStatusLayout, statusComboBox);
        dialog.add(content);

        Button cancelButton = new Button("Batal", event -> dialog.close());

        Button saveButton = new Button("Simpan", event -> {
            ApplicationStatus newStatus = statusComboBox.getValue();
            if (newStatus == null) {
                Notification.show("Silakan pilih status baru terlebih dahulu", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                jobApplicationService.updateStatus(jobApplication, currentUser, newStatus);
                refreshApplicants();
                dialog.close();
                Notification.show("Status lamaran berhasil diperbarui");
            } catch (SecurityException se) {
                Notification.show("Akses ditolak: Anda tidak memiliki hak untuk mengubah status lamaran ini", 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Gagal memperbarui status: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void refreshApplicants() {
        List<JobApplication> applications = jobApplicationService.findByJobPosting(currentJobPosting, currentUser);
        totalInfo.setText("Total Pelamar: " + applications.size());

        if (applications.isEmpty()) {
            grid.setVisible(false);
            emptyNotice.setVisible(true);
        } else {
            grid.setItems(applications);
            grid.setVisible(true);
            emptyNotice.setVisible(false);
        }
    }

    private String formatStatusLabel(ApplicationStatus status) {
        if (status == null) {
            return "-";
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
    }

    private Span createPostingStatusBadge(JobPosting jobPosting) {
        Span badge = new Span();
        badge.addClassName("status-badge");

        if (jobPosting.isActive()) {
            badge.setText("ACTIVE");
            badge.addClassName("active");
        } else {
            badge.setText("INACTIVE");
            badge.addClassName("inactive");
        }

        return badge;
    }

    private Span createApplicationStatusBadge(JobApplication jobApplication) {
        ApplicationStatus status = jobApplication.getStatus();
        Span badge = new Span(status != null ? status.name() : "-");
        badge.addClassName("status-badge");

        if (status != null) {
            String cssClass = status.name().toLowerCase().replace('_', '-');
            badge.addClassName(cssClass);
        }

        return badge;
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
