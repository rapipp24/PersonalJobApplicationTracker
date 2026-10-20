package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.EmployerSettingsService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "pemberi-lamaran/settings", layout = MainLayout.class)
@PageTitle("Settings & Company Profile | Job Tracker")
@RolesAllowed("PEMBERI_LAMARAN")
public class EmployerSettingsView extends VerticalLayout {

    private final EmployerSettingsService employerSettingsService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    // Section 1: Company Profile fields
    private final TextField companyNameField = new TextField("Company Name");
    private final TextField industryField = new TextField("Primary Industry");
    private final TextField locationField = new TextField("Company Location");
    private final TextField websiteField = new TextField("Official Website (Optional)");
    private final TextField officialContactField = new TextField("Official Company Email / Contact");
    private final TextArea descriptionField = new TextArea("Company Description");
    private final Span verificationStatusBadge = new Span();
    private final Button saveCompanyButton = new Button("Save Company Profile");
    private final Paragraph noCompanyNotice = new Paragraph("Akun Anda belum terhubung dengan perusahaan.");

    // Section 2: Account Settings fields
    private final TextField fullNameField = new TextField("Full Name");
    private final EmailField emailField = new EmailField("Email Address");
    private final Button saveAccountButton = new Button("Save Account");

    // Section 2 Subsection: Change Password fields
    private final PasswordField currentPasswordField = new PasswordField("Current Password");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm New Password");
    private final Button changePasswordButton = new Button("Change Password");

    public EmployerSettingsView(
            EmployerSettingsService employerSettingsService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.employerSettingsService = employerSettingsService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        setSpacing(false);
        setPadding(true);
        setWidthFull();
        setMaxWidth("1100px");
        getStyle()
                .set("margin-left", "0")
                .set("margin-right", "auto")
                .set("box-sizing", "border-box");

        createHeader();
        createCompanyProfileSection();
        add(createDivider());
        createAccountSection();

        loadUserData();
    }

    private void createHeader() {
        Div header = new Div();
        header.setWidthFull();
        header.getStyle().set("margin-bottom", "24px");

        H1 title = new H1("Settings & Company Profile");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "4px");

        Paragraph subtitle = new Paragraph("Kelola profil perusahaan dan pengaturan akun Anda.");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0").set("margin-bottom", "0");

        header.add(title, subtitle);
        add(header);
    }

    private void createCompanyProfileSection() {
        Div section = createSectionContainer();

        H2 sectionTitle = new H2("Company Profile");
        sectionTitle.getStyle().set("margin-top", "0").set("font-size", "1.25rem");

        Paragraph sectionDesc = new Paragraph("Informasi profil perusahaan Anda yang ditampilkan pada lowongan kerja.");
        sectionDesc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875rem").set("margin-top", "0").set("margin-bottom", "16px");

        // Verification Status row
        HorizontalLayout statusLayout = new HorizontalLayout();
        statusLayout.setAlignItems(Alignment.CENTER);
        statusLayout.getStyle().set("margin-bottom", "16px");

        Span statusLabel = new Span("Verification Status: ");
        statusLabel.getStyle().set("font-weight", "600").set("font-size", "0.875rem");
        statusLayout.add(statusLabel, verificationStatusBadge);

        // Form Fields
        companyNameField.setWidthFull();
        companyNameField.setMaxLength(255);
        companyNameField.setRequired(true);

        industryField.setWidthFull();
        industryField.setMaxLength(100);
        industryField.setRequired(true);

        locationField.setWidthFull();
        locationField.setMaxLength(255);
        locationField.setRequired(true);

        websiteField.setWidthFull();
        websiteField.setMaxLength(255);
        websiteField.setPlaceholder("https://example.com");

        officialContactField.setWidthFull();
        officialContactField.setMaxLength(255);
        officialContactField.setRequired(true);

        descriptionField.setWidthFull();
        descriptionField.setMaxLength(2000);
        descriptionField.setMinHeight("100px");
        descriptionField.setRequired(true);

        FormLayout companyForm = new FormLayout();
        companyForm.add(companyNameField, industryField, locationField, websiteField, officialContactField, descriptionField);
        companyForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        companyForm.setColspan(officialContactField, 2);
        companyForm.setColspan(descriptionField, 2);

        saveCompanyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveCompanyButton.getStyle().set("margin-top", "16px");
        saveCompanyButton.addClickListener(event -> handleSaveCompanyProfile());

        noCompanyNotice.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("margin-top", "8px");
        noCompanyNotice.setVisible(false);

        section.add(sectionTitle, sectionDesc, statusLayout, noCompanyNotice, companyForm, saveCompanyButton);
        add(section);
    }

    private Hr createDivider() {
        Hr divider = new Hr();
        divider.setWidthFull();
        divider.getStyle()
                .set("border", "none")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                .set("margin", "28px 0");
        return divider;
    }

    private void createAccountSection() {
        Div section = createSectionContainer();

        H2 sectionTitle = new H2("Account Settings");
        sectionTitle.getStyle().set("margin-top", "0").set("font-size", "1.25rem");

        Paragraph sectionDesc = new Paragraph("Perbarui nama tampilan akun atau ubah password login Anda.");
        sectionDesc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875rem").set("margin-top", "0").set("margin-bottom", "16px");

        // Part A: General Account Info
        fullNameField.setWidthFull();
        fullNameField.setRequired(true);

        emailField.setWidthFull();
        emailField.setReadOnly(true);
        emailField.setHelperText("Email tidak dapat diubah karena merupakan identitas login.");

        FormLayout accountForm = new FormLayout();
        accountForm.add(fullNameField, emailField);
        accountForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        saveAccountButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveAccountButton.getStyle().set("margin-top", "16px");
        saveAccountButton.addClickListener(event -> handleSaveAccount());

        // Part B: Change Password
        H3 passwordHeading = new H3("Change Password");
        passwordHeading.getStyle().set("margin-top", "32px").set("font-size", "1.1rem");

        Paragraph passwordDesc = new Paragraph("Masukkan password saat ini untuk mengatur password baru.");
        passwordDesc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875rem").set("margin-top", "0").set("margin-bottom", "16px");

        currentPasswordField.setWidthFull();
        newPasswordField.setWidthFull();
        confirmPasswordField.setWidthFull();

        FormLayout passwordForm = new FormLayout();
        passwordForm.add(currentPasswordField, newPasswordField, confirmPasswordField);
        passwordForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );

        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordButton.getStyle().set("margin-top", "16px");
        changePasswordButton.addClickListener(event -> handleChangePassword());

        section.add(sectionTitle, sectionDesc, accountForm, saveAccountButton, passwordHeading, passwordDesc, passwordForm, changePasswordButton);
        add(section);
    }

    private Div createSectionContainer() {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle().set("box-sizing", "border-box");
        return section;
    }

    private void loadUserData() {
        User currentUser = getCurrentUser();

        // 1. Account Settings load
        fullNameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        // 2. Company Profile load
        Company company = currentUser.getCompany();
        if (company == null) {
            noCompanyNotice.setVisible(true);
            companyNameField.setVisible(false);
            industryField.setVisible(false);
            locationField.setVisible(false);
            websiteField.setVisible(false);
            officialContactField.setVisible(false);
            descriptionField.setVisible(false);
            saveCompanyButton.setVisible(false);
            verificationStatusBadge.setText("NO COMPANY");
            applyBadgeStyle(verificationStatusBadge, "#F1F5F9", "#475569");
            return;
        }

        noCompanyNotice.setVisible(false);
        companyNameField.setVisible(true);
        industryField.setVisible(true);
        locationField.setVisible(true);
        websiteField.setVisible(true);
        officialContactField.setVisible(true);
        descriptionField.setVisible(true);

        companyNameField.setValue(company.getName() != null ? company.getName() : "");
        industryField.setValue(company.getIndustry() != null ? company.getIndustry() : "");
        locationField.setValue(company.getLocation() != null ? company.getLocation() : "");
        websiteField.setValue(company.getWebsite() != null ? company.getWebsite() : "");
        officialContactField.setValue(company.getOfficialContact() != null ? company.getOfficialContact() : "");
        descriptionField.setValue(company.getDescription() != null ? company.getDescription() : "");

        // Set verification status badge
        CompanyVerificationStatus status = company.getVerificationStatus();
        if (status == CompanyVerificationStatus.VERIFIED) {
            verificationStatusBadge.setText("VERIFIED");
            applyBadgeStyle(verificationStatusBadge, "#DEF7EC", "#03543F");
        } else if (status == CompanyVerificationStatus.REJECTED) {
            verificationStatusBadge.setText("REJECTED");
            applyBadgeStyle(verificationStatusBadge, "#FEE2E2", "#991B1B");
        } else {
            verificationStatusBadge.setText("PENDING");
            applyBadgeStyle(verificationStatusBadge, "#FEF3C7", "#92400E");
        }

        // Set editable vs read-only based on permission
        boolean isCompanyAdmin = currentUser.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN;
        companyNameField.setReadOnly(!isCompanyAdmin);
        industryField.setReadOnly(!isCompanyAdmin);
        locationField.setReadOnly(!isCompanyAdmin);
        websiteField.setReadOnly(!isCompanyAdmin);
        officialContactField.setReadOnly(!isCompanyAdmin);
        descriptionField.setReadOnly(!isCompanyAdmin);

        saveCompanyButton.setVisible(isCompanyAdmin);
    }

    private void applyBadgeStyle(Span badge, String bgColor, String textColor) {
        badge.getStyle()
                .set("background-color", bgColor)
                .set("color", textColor)
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-weight", "600")
                .set("font-size", "12px");
    }

    private void handleSaveCompanyProfile() {
        try {
            User currentUser = getCurrentUser();
            employerSettingsService.updateCompanyProfile(
                    currentUser,
                    companyNameField.getValue(),
                    industryField.getValue(),
                    locationField.getValue(),
                    websiteField.getValue(),
                    officialContactField.getValue(),
                    descriptionField.getValue()
            );
            Notification.show("Profil perusahaan berhasil disimpan", 3000, Notification.Position.BOTTOM_START);
            loadUserData();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
        }
    }

    private void handleSaveAccount() {
        try {
            User currentUser = getCurrentUser();
            employerSettingsService.updateFullName(currentUser, fullNameField.getValue());
            updateHeaderDisplayName(fullNameField.getValue());
            Notification.show("Nama akun berhasil diperbarui", 3000, Notification.Position.BOTTOM_START);
            loadUserData();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
        }
    }

    private void updateHeaderDisplayName(String newName) {
        MainLayout layout = findAncestor(MainLayout.class);
        if (layout != null) {
            layout.updateCurrentUserDisplayName(newName);
        } else if (UI.getCurrent() != null) {
            UI.getCurrent().getChildren()
                    .filter(MainLayout.class::isInstance)
                    .map(MainLayout.class::cast)
                    .findFirst()
                    .ifPresent(l -> l.updateCurrentUserDisplayName(newName));
        }
    }

    private void handleChangePassword() {
        try {
            User currentUser = getCurrentUser();
            employerSettingsService.changePassword(
                    currentUser,
                    currentPasswordField.getValue(),
                    newPasswordField.getValue(),
                    confirmPasswordField.getValue()
            );
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            Notification.show("Password berhasil diubah", 3000, Notification.Position.BOTTOM_START);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
        }
    }

    private User getCurrentUser() {
        String email = authenticationContext.getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }
}
