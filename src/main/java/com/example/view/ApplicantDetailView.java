package com.example.view;

import com.example.entity.ApplicationStatus;
import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.JobApplication;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "pemberi-lamaran/pelamar/detail", layout = MainLayout.class)
@PageTitle("Applicant Detail | Job Application Tracker")
@RolesAllowed("PEMBERI_LAMARAN")
public class ApplicantDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final Logger log = LoggerFactory.getLogger(ApplicantDetailView.class);

    private final JobApplicationService jobApplicationService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private JobApplication currentApplication;
    private User currentUser;
    private Span identityStatusBadge;

    public ApplicantDetailView(
            JobApplicationService jobApplicationService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.jobApplicationService = jobApplicationService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        addClassName("view-container");
        setWidthFull();
        setSpacing(false);
        setPadding(true);
        getStyle()
                .set("box-sizing", "border-box")
                .set("padding-top", "16px");
    }

    @Override
    public void setParameter(BeforeEvent event, Long jobApplicationId) {
        User user;
        try {
            user = getCurrentUser();
        } catch (Exception ex) {
            event.forwardTo(LoginView.class);
            return;
        }

        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }

        Company company = user.getCompany();
        if (company == null
                || company.getVerificationStatus() != CompanyVerificationStatus.VERIFIED
                || user.getMembershipStatus() != CompanyMembershipStatus.ACTIVE) {
            event.forwardTo(DashboardView.class);
            return;
        }

        if (jobApplicationId == null) {
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        try {
            this.currentApplication = jobApplicationService.findApplicationForEmployer(jobApplicationId, user);
            this.currentUser = user;
        } catch (SecurityException se) {
            log.warn("Akses ditolak saat membuka detail lamaran #{}: {}", jobApplicationId, se.getMessage());
            event.forwardTo(PemberiLamaranView.class);
            return;
        } catch (Exception ex) {
            log.warn("Lamaran #{} tidak dapat dimuat: {}", jobApplicationId, ex.getMessage());
            event.forwardTo(PemberiLamaranView.class);
            return;
        }

        removeAll();
        buildView();
    }

    private void buildView() {
        if (currentApplication == null || currentUser == null) {
            return;
        }

        createHeader();
        createTwoColumnContent();
    }

    private void createHeader() {
        // Back navigation deterministic ke daftar pelamar lowongan yang bersangkutan — local navigation link
        HorizontalLayout backLink = new HorizontalLayout();
        backLink.setAlignItems(Alignment.CENTER);
        backLink.setSpacing(false);
        backLink.getStyle()
                .set("cursor", "pointer")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("color", "#475569")
                .set("font-weight", "500")
                .set("font-size", "0.875rem")
                .set("margin-bottom", "14px")
                .set("width", "fit-content")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("transition", "color 0.15s ease");

        Html backIcon = new Html("<iconify-icon icon=\"lucide:square-chevron-left\" style=\"font-size: 20px; line-height: 1; display: inline-flex; align-items: center; flex-shrink: 0;\"></iconify-icon>");
        Span backText = new Span("Kembali ke Daftar Pelamar");
        backText.getStyle().set("line-height", "1.2");

        backLink.add(backIcon, backText);

        backLink.getElement().addEventListener("mouseover", e -> backLink.getStyle().set("color", "#0f172a"));
        backLink.getElement().addEventListener("mouseout", e -> backLink.getStyle().set("color", "#475569"));

        backLink.addClickListener(event -> {
            if (currentApplication != null && currentApplication.getJobPosting() != null) {
                UI.getCurrent().navigate(JobApplicantsView.class, currentApplication.getJobPosting().getId());
            } else {
                UI.getCurrent().navigate(PemberiLamaranView.class);
            }
        });

        H1 title = new H1("Applicant Detail");
        title.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "4px")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("line-height", "1.25");

        Paragraph subtitle = new Paragraph("Informasi pelamar dan detail lamaran");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "0")
                .set("margin-bottom", "22px")
                .set("font-size", "0.875rem");

        add(backLink, title, subtitle);
    }

    private void createTwoColumnContent() {
        Div layoutContainer = new Div();
        layoutContainer.setWidthFull();
        layoutContainer.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("align-items", "flex-start");

        // Kolom Kiri (~65%)
        Div leftColumn = new Div();
        leftColumn.getStyle()
                .set("flex", "1 1 560px")
                .set("min-width", "0")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");

        leftColumn.add(createApplicantIdentityCard());
        leftColumn.add(createInformationCard());

        // Kolom Kanan (~35%)
        Div rightColumn = new Div();
        rightColumn.getStyle()
                .set("flex", "0 0 320px")
                .set("min-width", "280px")
                .set("max-width", "100%")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");

        rightColumn.add(createResumeCard());
        rightColumn.add(createUpdateStatusCard());

        layoutContainer.add(leftColumn, rightColumn);
        add(layoutContainer);
    }

    /**
     * Card 1 (Kiri Atas): Applicant Identity Card
     * Menampilkan avatar inisial, nama, posisi, dan SATU-SATUNYA status badge di halaman ini.
     */
    private Div createApplicantIdentityCard() {
        Div card = createWhiteCard();
        card.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "14px 20px");

        HorizontalLayout leftGroup = new HorizontalLayout();
        leftGroup.setAlignItems(Alignment.CENTER);
        leftGroup.setSpacing(true);

        String applicantName = getApplicantName();
        String initials = getApplicantInitials(applicantName);

        // Avatar container
        Div avatar = new Div();
        avatar.setText(initials);
        avatar.getStyle()
                .set("width", "44px")
                .set("height", "44px")
                .set("border-radius", "8px")
                .set("background-color", "#f1f5f9")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-weight", "700")
                .set("font-size", "1rem")
                .set("color", "#1e293b");

        // Nama dan Posisi
        Div namePositionBox = new Div();
        namePositionBox.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "2px");

        Span nameSpan = new Span(applicantName);
        nameSpan.getStyle()
                .set("font-size", "1.125rem")
                .set("font-weight", "700")
                .set("color", "#0f172a");

        String position = getApplicationPosition();
        Span positionSpan = new Span(position);
        positionSpan.getStyle()
                .set("font-size", "0.8125rem")
                .set("color", "#64748b");

        namePositionBox.add(nameSpan, positionSpan);
        leftGroup.add(avatar, namePositionBox);

        // Status Badge tunggal
        identityStatusBadge = createApplicationStatusBadge(currentApplication.getStatus());

        card.add(leftGroup, identityStatusBadge);
        return card;
    }

    /**
     * Card 2 (Kiri Bawah): Information Card
     * Terdiri dari 3 section:
     * 1. Applicant Information (Full Name, Email)
     * 2. Contact Information (Phone, LinkedIn, Portfolio dari JobApplication snapshot)
     * 3. Application Information (Position Applied, Application Date) - TANPA status ganda
     */
    private Div createInformationCard() {
        Div card = createWhiteCard();

        // Section 1: Applicant Information
        H3 sec1Title = new H3("Applicant Information");
        sec1Title.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin-top", "0")
                .set("margin-bottom", "14px");

        Div sec1Grid = new Div();
        sec1Grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(2, minmax(0, 1fr))")
                .set("gap", "20px");

        sec1Grid.add(createFieldBlock("Full Name", getApplicantName()));
        sec1Grid.add(createFieldBlock("Email Address", getApplicantEmail()));

        // Section 2: Contact Information
        Div sec2Header = new Div();
        sec2Header.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("margin-bottom", "14px");

        H3 sec2Title = new H3("Contact Information");
        sec2Title.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin", "0");

        Span sec2Subtitle = new Span("Submitted with this application");
        sec2Subtitle.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#94a3b8")
                .set("font-weight", "400");

        sec2Header.add(sec2Title, sec2Subtitle);

        Div contactList = new Div();
        contactList.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "10px");

        String phoneVal = currentApplication.getPhone() != null && !currentApplication.getPhone().isBlank()
                ? currentApplication.getPhone()
                : "-";
        String linkedInVal = currentApplication.getLinkedIn() != null && !currentApplication.getLinkedIn().isBlank()
                ? currentApplication.getLinkedIn()
                : "-";
        String portfolioVal = currentApplication.getPortfolio() != null && !currentApplication.getPortfolio().isBlank()
                ? currentApplication.getPortfolio()
                : "-";

        contactList.add(createContactRow("lucide:phone", "Phone", phoneVal));
        contactList.add(createContactRow("lucide:linkedin", "LinkedIn", linkedInVal));
        contactList.add(createContactRow("lucide:globe", "Portfolio", portfolioVal));

        // Section 3: Application Information
        H3 sec3Title = new H3("Application Information");
        sec3Title.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin-top", "0")
                .set("margin-bottom", "14px");

        Div sec3Grid = new Div();
        sec3Grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(2, minmax(0, 1fr))")
                .set("gap", "20px");

        sec3Grid.add(createFieldBlock("Position Applied", getApplicationPosition()));
        sec3Grid.add(createFieldBlock("Application Date", formatReadableDate(currentApplication.getApplicationDate())));

        card.add(
                sec1Title,
                sec1Grid,
                createDivider(),
                sec2Header,
                contactList,
                createDivider(),
                sec3Title,
                sec3Grid
        );

        return card;
    }

    /**
     * Card 3 (Kanan Atas): Resume / CV Card
     * Menampilkan dokumen icon bernuansa dark blue/light blue container (sesuai referensi icon gambar),
     * nama file asli, ukuran file, dan tombol Download CV (atau notice jika tidak tersedia).
     */
    private Div createResumeCard() {
        Div card = createWhiteCard();
        card.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px");

        H3 title = new H3("Resume / CV");
        title.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin", "0");

        card.add(title);

        boolean hasCv = currentApplication.getResumeStorageKey() != null
                && !currentApplication.getResumeStorageKey().trim().isEmpty();

        if (hasCv) {
            // File row
            Div fileRow = new Div();
            fileRow.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("gap", "10px")
                    .set("padding", "10px 12px")
                    .set("border-radius", "8px")
                    .set("background-color", "#f8fafc")
                    .set("border", "1px solid #f1f5f9");

            // Blue document icon container (referensi gambar icon CV)
            Div iconBox = new Div();
            iconBox.getStyle()
                    .set("width", "38px")
                    .set("height", "38px")
                    .set("border-radius", "8px")
                    .set("background-color", "#eff6ff")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("flex-shrink", "0");

            Html docIcon = new Html("<iconify-icon icon=\"lucide:file-text\" style=\"color: #1E2858; font-size: 20px;\"></iconify-icon>");
            iconBox.add(docIcon);

            // Filename & size
            Div fileInfo = new Div();
            fileInfo.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("min-width", "0")
                    .set("gap", "2px");

            String originalName = currentApplication.getResumeOriginalFilename() != null
                    ? currentApplication.getResumeOriginalFilename()
                    : "CV_Document.pdf";

            Span nameSpan = new Span(originalName);
            nameSpan.getStyle()
                    .set("font-size", "0.85rem")
                    .set("font-weight", "600")
                    .set("color", "#0f172a")
                    .set("word-break", "break-word");

            Span sizeSpan = new Span(formatFileSize(currentApplication.getResumeFileSize()));
            sizeSpan.getStyle()
                    .set("font-size", "0.725rem")
                    .set("color", "#64748b");

            fileInfo.add(nameSpan, sizeSpan);
            fileRow.add(iconBox, fileInfo);

            // Download Button with StreamResource (Authorized backend streaming)
            StreamResource resumeResource = new StreamResource(originalName, () -> {
                try {
                    return jobApplicationService.loadResumeStreamForEmployer(currentApplication.getId(), currentUser);
                } catch (Exception ex) {
                    log.error("Gagal mendownload file CV: {}", ex.getMessage());
                    Notification.show("File CV tidak ditemukan pada sistem penyimpanan.", 4000, Notification.Position.MIDDLE);
                    return InputStream.nullInputStream();
                }
            });

            if (currentApplication.getResumeContentType() != null) {
                resumeResource.setContentType(currentApplication.getResumeContentType());
            }

            Anchor downloadAnchor = new Anchor(resumeResource, "");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.setWidthFull();

            Button downloadButton = new Button("Download CV");
            downloadButton.setIcon(new Html("<iconify-icon icon=\"lucide:download\" style=\"margin-right: 6px; font-size: 16px;\"></iconify-icon>"));
            downloadButton.setWidthFull();
            downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            downloadButton.getStyle()
                    .set("background-color", "#1E2858")
                    .set("color", "#ffffff")
                    .set("border-radius", "7px")
                    .set("font-weight", "600")
                    .set("font-size", "0.875rem")
                    .set("height", "36px")
                    .set("cursor", "pointer");

            downloadButton.getElement().addEventListener("mouseover", e -> downloadButton.getStyle().set("background-color", "#162044"));
            downloadButton.getElement().addEventListener("mouseout", e -> downloadButton.getStyle().set("background-color", "#1E2858"));

            downloadAnchor.add(downloadButton);

            card.add(fileRow, downloadAnchor);
        } else {
            // Empty state untuk lamaran historis tanpa file CV
            Paragraph emptyNotice = new Paragraph("CV tidak tersedia");
            emptyNotice.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic")
                    .set("font-size", "0.85rem")
                    .set("margin", "0");
            card.add(emptyNotice);
        }

        return card;
    }

    /**
     * Card 4 (Kanan Bawah): Update Application Status Card
     * Hanya berisi form select status dan tombol update status (TANPA kotak Current Status duplikat).
     */
    private Div createUpdateStatusCard() {
        Div card = createWhiteCard();
        card.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px");

        H3 title = new H3("Update Application Status");
        title.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin", "0");

        ComboBox<ApplicationStatus> statusComboBox = new ComboBox<>("Select New Status");
        statusComboBox.setItems(ApplicationStatus.values());
        statusComboBox.setItemLabelGenerator(this::formatStatusLabel);
        statusComboBox.setValue(currentApplication.getStatus());
        statusComboBox.setWidthFull();

        Button updateButton = new Button("Update Status");
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setWidthFull();
        updateButton.getStyle()
                .set("background-color", "#1E2858")
                .set("color", "#ffffff")
                .set("border-radius", "7px")
                .set("font-weight", "600")
                .set("font-size", "0.875rem")
                .set("height", "36px")
                .set("cursor", "pointer");

        updateButton.getElement().addEventListener("mouseover", e -> updateButton.getStyle().set("background-color", "#162044"));
        updateButton.getElement().addEventListener("mouseout", e -> updateButton.getStyle().set("background-color", "#1E2858"));

        updateButton.addClickListener(event -> {
            ApplicationStatus selectedStatus = statusComboBox.getValue();
            if (selectedStatus == null) {
                Notification.show("Silakan pilih status baru terlebih dahulu", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                this.currentApplication = jobApplicationService.updateStatus(currentApplication, currentUser, selectedStatus);
                updateIdentityStatusBadge(currentApplication.getStatus());
                Notification.show("Status lamaran berhasil diperbarui");
            } catch (SecurityException se) {
                Notification.show("Akses ditolak: Anda tidak memiliki hak untuk mengubah status lamaran ini", 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Gagal memperbarui status: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        card.add(title, statusComboBox, updateButton);
        return card;
    }

    private void updateIdentityStatusBadge(ApplicationStatus status) {
        if (identityStatusBadge == null) {
            return;
        }
        identityStatusBadge.setText(status != null ? status.name() : "-");
        identityStatusBadge.setClassName("status-badge");
        if (status != null) {
            String cssClass = status.name().toLowerCase().replace('_', '-');
            identityStatusBadge.addClassName(cssClass);
        }
    }

    private Div createWhiteCard() {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "10px")
                .set("padding", "16px 20px")
                .set("box-sizing", "border-box");
        return card;
    }

    private Div createDivider() {
        Div divider = new Div();
        divider.setWidthFull();
        divider.getStyle()
                .set("border-top", "1px solid #e2e8f0")
                .set("margin", "20px 0");
        return divider;
    }

    private Div createFieldBlock(String label, String value) {
        Div block = new Div();
        block.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "4px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "500")
                .set("color", "#64748b")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.03em");

        Span valueSpan = new Span(value != null && !value.isBlank() ? value : "-");
        valueSpan.getStyle()
                .set("font-size", "0.925rem")
                .set("font-weight", "600")
                .set("color", "#0f172a")
                .set("word-break", "break-word");

        block.add(labelSpan, valueSpan);
        return block;
    }

    private Div createContactRow(String iconName, String label, String value) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        Div labelContainer = new Div();
        labelContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("width", "95px")
                .set("flex-shrink", "0");

        Html icon = new Html("<iconify-icon icon=\"" + iconName + "\" style=\"color: #64748b; font-size: 16px; margin-right: 8px; flex-shrink: 0;\"></iconify-icon>");
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.8125rem")
                .set("color", "#64748b")
                .set("font-weight", "500");

        labelContainer.add(icon, labelSpan);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", "#0f172a")
                .set("word-break", "break-all");

        row.add(labelContainer, valueSpan);
        return row;
    }

    private Span createApplicationStatusBadge(ApplicationStatus status) {
        Span badge = new Span(status != null ? status.name() : "-");
        badge.addClassName("status-badge");
        if (status != null) {
            String cssClass = status.name().toLowerCase().replace('_', '-');
            badge.addClassName(cssClass);
        }
        return badge;
    }

    private String getApplicantName() {
        if (currentApplication != null
                && currentApplication.getApplicant() != null
                && currentApplication.getApplicant().getName() != null) {
            return currentApplication.getApplicant().getName();
        }
        return "Pelamar";
    }

    private String getApplicantEmail() {
        if (currentApplication != null
                && currentApplication.getApplicant() != null
                && currentApplication.getApplicant().getEmail() != null) {
            return currentApplication.getApplicant().getEmail();
        }
        return "-";
    }

    private String getApplicationPosition() {
        if (currentApplication != null && currentApplication.getPosition() != null && !currentApplication.getPosition().isBlank()) {
            return currentApplication.getPosition();
        }
        if (currentApplication != null && currentApplication.getJobPosting() != null && currentApplication.getJobPosting().getPosition() != null) {
            return currentApplication.getJobPosting().getPosition();
        }
        return "-";
    }

    private String getApplicantInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "PL";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String single = parts[0].toUpperCase(Locale.ENGLISH);
            return single.length() >= 2 ? single.substring(0, 2) : single;
        }
        String first = parts[0].substring(0, 1).toUpperCase(Locale.ENGLISH);
        String second = parts[1].substring(0, 1).toUpperCase(Locale.ENGLISH);
        return first + second;
    }

    private String formatReadableDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.of("id", "ID")));
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else {
            return String.format(Locale.ENGLISH, "%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    private String formatStatusLabel(ApplicationStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case APPLIED -> "Applied";
            case SCREENING -> "Screening";
            case TECHNICAL_TEST -> "Technical Test";
            case INTERVIEW -> "Interview";
            case OFFERED -> "Offered";
            case REJECTED -> "Rejected";
        };
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
