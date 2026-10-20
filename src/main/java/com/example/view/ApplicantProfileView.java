package com.example.view;

import com.example.entity.ApplicantProfile;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.ApplicantProfileService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "pelamar/profile", layout = MainLayout.class)
@PageTitle("Profile & Settings | Job Tracker")
@RolesAllowed("PELAMAR")
public class ApplicantProfileView extends VerticalLayout {

    private final ApplicantProfileService applicantProfileService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    // Section 1: Profile Information
    private final TextField phoneField = new TextField("Phone Number");
    private final TextField linkedInField = new TextField("LinkedIn URL");
    private final TextField portfolioField = new TextField("Portfolio URL");
    private final Button saveProfileButton = new Button("Save Profile");

    // Section 2: Account Settings
    private final TextField fullNameField = new TextField("Full Name");
    private final EmailField emailField = new EmailField("Email Address");
    private final Button saveAccountButton = new Button("Save Account");

    // Section 2 Part B: Change Password
    private final PasswordField currentPasswordField = new PasswordField("Current Password");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm New Password");
    private final Button changePasswordButton = new Button("Change Password");

    public ApplicantProfileView(
            ApplicantProfileService applicantProfileService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.applicantProfileService = applicantProfileService;
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
        createProfileSection();
        add(createDivider());
        createAccountSection();

        loadUserData();
    }

    private void createHeader() {
        Div header = new Div();
        header.setWidthFull();
        header.getStyle().set("margin-bottom", "24px");

        H1 title = new H1("Profile & Settings");
        title.getStyle().set("margin-top", "0").set("margin-bottom", "4px");

        Paragraph subtitle = new Paragraph("Manage your contact information, profile links, and account credentials.");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0").set("margin-bottom", "0");

        header.add(title, subtitle);
        add(header);
    }

    private void createProfileSection() {
        Div section = createSectionContainer();

        H2 sectionTitle = new H2("Profile Information");
        sectionTitle.getStyle().set("margin-top", "0").set("font-size", "1.25rem");

        Paragraph sectionDesc = new Paragraph("Provide your contact details and professional links. All fields are optional.");
        sectionDesc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875rem").set("margin-top", "0").set("margin-bottom", "16px");

        phoneField.setPlaceholder("+62 8xx xxxx xxxx");
        phoneField.setMaxLength(30);
        phoneField.setWidthFull();

        linkedInField.setPlaceholder("https://linkedin.com/in/username");
        linkedInField.setMaxLength(255);
        linkedInField.setWidthFull();

        portfolioField.setPlaceholder("https://yourportfolio.com");
        portfolioField.setMaxLength(255);
        portfolioField.setWidthFull();

        FormLayout formLayout = new FormLayout();
        formLayout.add(phoneField, linkedInField, portfolioField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        formLayout.setColspan(portfolioField, 2);

        saveProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfileButton.getStyle().set("margin-top", "16px");
        saveProfileButton.addClickListener(event -> handleSaveProfile());

        section.add(sectionTitle, sectionDesc, formLayout, saveProfileButton);
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

        Paragraph sectionDesc = new Paragraph("Update your account display name or change your login password.");
        sectionDesc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875rem").set("margin-top", "0").set("margin-bottom", "16px");

        // Part A: General Account Info
        fullNameField.setWidthFull();
        fullNameField.setRequired(true);

        emailField.setWidthFull();
        emailField.setReadOnly(true);
        emailField.setHelperText("Email cannot be changed as it is your login identifier.");

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

        Paragraph passwordDesc = new Paragraph("Enter your current password to set a new password.");
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
        ApplicantProfile profile = applicantProfileService.getOrCreateProfile(currentUser);

        phoneField.setValue(profile.getPhone() != null ? profile.getPhone() : "");
        linkedInField.setValue(profile.getLinkedIn() != null ? profile.getLinkedIn() : "");
        portfolioField.setValue(profile.getPortfolio() != null ? profile.getPortfolio() : "");

        fullNameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
    }

    private void handleSaveProfile() {
        try {
            User currentUser = getCurrentUser();
            applicantProfileService.updateProfile(
                    currentUser,
                    phoneField.getValue(),
                    linkedInField.getValue(),
                    portfolioField.getValue()
            );
            Notification.show("Profil berhasil disimpan", 3000, Notification.Position.BOTTOM_START);
            loadUserData();
        } catch (IllegalArgumentException | SecurityException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
        }
    }

    private void handleSaveAccount() {
        try {
            User currentUser = getCurrentUser();
            applicantProfileService.updateFullName(currentUser, fullNameField.getValue());
            updateHeaderDisplayName(fullNameField.getValue());
            Notification.show("Nama akun berhasil diperbarui", 3000, Notification.Position.BOTTOM_START);
            loadUserData();
        } catch (IllegalArgumentException | SecurityException ex) {
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
            applicantProfileService.changePassword(
                    currentUser,
                    currentPasswordField.getValue(),
                    newPasswordField.getValue(),
                    confirmPasswordField.getValue()
            );
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            Notification.show("Password berhasil diubah", 3000, Notification.Position.BOTTOM_START);
        } catch (IllegalArgumentException | SecurityException ex) {
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
