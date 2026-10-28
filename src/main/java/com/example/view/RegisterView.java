package com.example.view;

import com.example.dto.CompanyRegistrationType;
import com.example.dto.RegistrationFormData;
import com.example.entity.Company;
import com.example.entity.Role;
import com.example.service.RegistrationService;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Route("register")
@PageTitle("Register | Job Application Tracker")
@AnonymousAllowed
@StyleSheet("context://styles.css")
public class RegisterView extends VerticalLayout {

    private final RegistrationService registrationService;
    private final BeanValidationBinder<RegistrationFormData> binder;

    // State management for Two-Step Employer Registration
    private int currentStep = 1;
    private Div step1Container;
    private Div step2Container;
    private Div applicantBenefitContainer;
    private Div employerBenefitContainer;
    private Div employerFooter;
    private Div verificationProcessBox;
    private Div illustrationCard;
    private Image defaultIllustrationImg;
    private Image companyIllustrationImg;
    private Div registerCard;
    private Span stepBadge;
    private H2 step2Title;
    private Paragraph step2Subtitle;
    private Div joinLoginDivider;
    private RouterLink joinLoginLink;

    // Step 1: Account Details Fields
    private final TextField fullName = new TextField("Full Name");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");
    private final RadioButtonGroup<Role> accountType = new RadioButtonGroup<>("Account Type");
    private final Button registerButton = new Button("Register");

    // Step 2: Employer Company Registration Option
    private final RadioButtonGroup<CompanyRegistrationType> companyRegistrationType =
            new RadioButtonGroup<>("Registration Option");

    private static final List<String> INDUSTRY_OPTIONS = List.of(
            "Teknologi Informasi",
            "Keuangan",
            "Kesehatan",
            "Pendidikan",
            "Manufaktur",
            "Konstruksi",
            "Properti",
            "Ritel",
            "E-Commerce",
            "Transportasi & Logistik",
            "Perhotelan & Pariwisata",
            "Telekomunikasi",
            "Media & Kreatif",
            "Energi & Pertambangan",
            "Pertanian",
            "Otomotif",
            "Jasa Profesional",
            "Makanan & Minuman",
            "Pemerintahan / Sektor Publik",
            "Lainnya"
    );

    // Step 2: Register New Company Fields (Grid 4 baris)
    private final TextField companyName = new TextField("Company Name");
    private final Select<String> industry = new Select<>();
    private final TextField companyLocation = new TextField("Company Location");
    private final TextField website = new TextField("Official Website (Optional)");
    private final TextField officialContact = new TextField("Official Company Email / Contact");
    private final TextArea description = new TextArea("Company Description");
    private final Div newCompanyContainer = new Div();

    // Step 2: Join Existing Company Container & Search
    private final ComboBox<Company> companySearch = new ComboBox<>("Search Company");
    private final Div existingCompanyContainer = new Div();

    // Step 2 Action Button
    private final Button step2SubmitButton = new Button("Register & Submit Company");

    public RegisterView(RegistrationService registrationService) {
        this.registrationService = registrationService;
        this.binder = new BeanValidationBinder<>(RegistrationFormData.class);

        loadStyles();

        addClassNames("auth-view", "register-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        configureBinder();
        createRegisterForm();
    }

    private void loadStyles() {
        try (InputStream is = getClass().getResourceAsStream("/META-INF/resources/styles.css")) {
            if (is != null) {
                String css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                HtmlComponent styleTag = new HtmlComponent("style");
                styleTag.getElement().setAttribute("id", "register-injected-styles");
                styleTag.getElement().setText(css);
                getElement().appendChild(styleTag.getElement());
            }
        } catch (Exception ignored) {
        }
    }

    private void configureBinder() {
        binder.forField(fullName)
                .withConverter(
                        value -> value == null ? "" : value.trim(),
                        value -> value
                )
                .asRequired("Full Name is required.")
                .bind("fullName");

        binder.forField(email)
                .withConverter(
                        value -> value == null ? "" : value.trim(),
                        value -> value
                )
                .asRequired("Email is required.")
                .bind("email");

        binder.forField(password)
                .asRequired("Password is required.")
                .bind("password");

        binder.forField(confirmPassword)
                .asRequired("Password confirmation is required.")
                .withValidator(
                        confirmValue -> confirmValue != null && confirmValue.equals(password.getValue()),
                        "Password confirmation does not match."
                )
                .bind("confirmPassword");

        password.addValueChangeListener(event -> {
            if (!confirmPassword.isEmpty()) {
                binder.validate();
            }
        });

        binder.forField(accountType)
                .asRequired("Please select an account type.")
                .bind("accountType");
    }

    private void createRegisterForm() {
        registerCard = new Div();
        registerCard.addClassName("register-card");
        registerCard.addClassName("register-card-applicant");

        // 1. Panel Kiri (Register Form - Step 1 & Step 2)
        Div leftPanel = buildLeftPanel();

        // 2. Panel Kanan (Branding & Benefit & Ilustrasi)
        Div rightPanel = buildRightPanel();

        registerCard.add(leftPanel, rightPanel);
        add(registerCard);
    }

    private Div buildLeftPanel() {
        Div leftPanel = new Div();
        leftPanel.addClassName("register-left-panel");

        // =====================================================================
        // STEP 1 CONTAINER (Account Details)
        // =====================================================================
        step1Container = new Div();
        step1Container.addClassName("register-step1-container");

        // Brand Header (Lucide briefcase icon + Title)
        Div brandHeader = new Div();
        brandHeader.addClassName("register-brand-header");

        Div brandLogoWrapper = new Div();
        brandLogoWrapper.addClassName("register-brand-logo-wrapper");
        StreamResource logoResource = new StreamResource("briefcase-business.svg", () ->
                getClass().getResourceAsStream("/META-INF/resources/images/briefcase-business.svg")
        );
        Image brandLogo = new Image(logoResource, "Job Application Tracker Logo");
        brandLogo.addClassName("register-brand-logo");
        brandLogoWrapper.add(brandLogo);

        Span brandTitle = new Span("Job Application Tracker");
        brandTitle.addClassName("register-brand-title");
        brandHeader.add(brandLogoWrapper, brandTitle);

        // Heading & Subtitle
        H2 formTitle = new H2("Create Account");
        formTitle.addClassName("register-form-title");

        Paragraph formSubtitle = new Paragraph("Join our portal to track applications or hire top talent");
        formSubtitle.addClassName("register-form-subtitle");

        // Basic Account Input Fields
        fullName.setWidthFull();
        fullName.setPlaceholder("Enter your full name");

        email.setWidthFull();
        email.setPlaceholder("Enter your email address");
        email.setErrorMessage("Please enter a valid email address.");

        password.setWidthFull();
        password.setPlaceholder("Create a password");

        confirmPassword.setWidthFull();
        confirmPassword.setPlaceholder("Repeat your password");

        accountType.setItems(Role.PELAMAR, Role.PEMBERI_LAMARAN);
        accountType.setItemLabelGenerator(role -> {
            if (role == Role.PELAMAR) {
                return "Applicant";
            }
            if (role == Role.PEMBERI_LAMARAN) {
                return "Employer";
            }
            return role.name();
        });
        accountType.setValue(Role.PELAMAR);

        // Submit Button (Step 1)
        registerButton.setWidthFull();
        registerButton.addClassName("register-submit-btn");
        registerButton.setText("Register");

        // Divider
        Div divider = new Div();
        divider.addClassName("register-divider");
        Span dividerText = new Span("Already have an account?");
        divider.add(dividerText);

        // Outlined Login Button
        RouterLink loginLink = new RouterLink("Login", LoginView.class);
        loginLink.addClassName("register-login-btn");

        step1Container.add(
                brandHeader,
                formTitle,
                formSubtitle,
                fullName,
                email,
                password,
                confirmPassword,
                accountType,
                registerButton,
                divider,
                loginLink
        );

        // =====================================================================
        // STEP 2 CONTAINER (Company Registration - Two-Step Employer)
        // =====================================================================
        step2Container = new Div();
        step2Container.addClassName("register-step2-container");
        step2Container.setVisible(false);

        // Header: Back button & Step Badge
        Div step2Header = new Div();
        step2Header.addClassName("register-step2-header");

        Button backBtn = new Button("Back to Account Details", VaadinIcon.ARROW_LEFT.create());
        backBtn.addClassName("register-back-btn");
        backBtn.addClickListener(event -> switchToStep(1));

        stepBadge = new Span("Step 2 of 2: Company Profile");
        stepBadge.addClassName("register-step-badge");

        step2Header.add(backBtn, stepBadge);

        // Heading & Subtitle
        step2Title = new H2("Company Registration");
        step2Title.addClassName("register-step2-title");

        step2Subtitle = new Paragraph(
                "Set up your organization profile to start posting jobs and reviewing applicants."
        );
        step2Subtitle.addClassName("register-step2-subtitle");

        // Registration Option RadioGroup
        companyRegistrationType.setLabel("Registration Option");
        companyRegistrationType.setItems(
                CompanyRegistrationType.REGISTER_NEW_COMPANY,
                CompanyRegistrationType.JOIN_EXISTING_COMPANY
        );
        companyRegistrationType.setItemLabelGenerator(type -> {
            if (type == CompanyRegistrationType.REGISTER_NEW_COMPANY) {
                return "Register New Company";
            }
            if (type == CompanyRegistrationType.JOIN_EXISTING_COMPANY) {
                return "Join Existing Company";
            }
            return type.name();
        });
        companyRegistrationType.setValue(CompanyRegistrationType.REGISTER_NEW_COMPANY);
        companyRegistrationType.setWidthFull();

        // ---------------------------------------------------------------------
        // New Company Container & Fields (Grid Layout)
        // ---------------------------------------------------------------------
        newCompanyContainer.addClassName("register-new-company-container");

        // Baris 1: Company Name & Industry
        companyName.setLabel("Company Name");
        companyName.setRequiredIndicatorVisible(true);
        companyName.setPlaceholder("Enter company name");
        companyName.setHelperText("Enter the official company name. Example: PT Muara Buana");

        industry.setLabel("Industry");
        industry.setRequiredIndicatorVisible(true);
        industry.setPlaceholder("Pilih Industri");
        industry.setItems(INDUSTRY_OPTIONS);
        industry.setWidthFull();
        industry.setHelperText("Select the company's primary industry.");

        Div row1 = new Div();
        row1.addClassName("register-form-row");
        row1.add(companyName, industry);

        // Baris 2: Official Website (Optional) & Official Company Email / Contact
        website.setLabel("Official Website (Optional)");
        website.setRequiredIndicatorVisible(false);
        website.setWidthFull();
        website.setPlaceholder("https://...");
        website.setHelperText("Example: https://company.com");

        officialContact.setLabel("Official Company Email / Contact");
        officialContact.setRequiredIndicatorVisible(true);
        officialContact.setWidthFull();
        officialContact.setPlaceholder("hr@company.com or contact@...");
        officialContact.setHelperText("Example: hr@company.com or contact@company.com");

        Div row2 = new Div();
        row2.addClassName("register-form-row");
        row2.add(website, officialContact);

        // Baris 3: Company Location (Full Width)
        companyLocation.setLabel("Company Location");
        companyLocation.setRequiredIndicatorVisible(true);
        companyLocation.setWidthFull();
        companyLocation.setPlaceholder("City, Country");
        companyLocation.setHelperText("Enter the main location. Example: Bandar Lampung");

        // Baris 4: Company Description (Full Width)
        description.setLabel("Company Description");
        description.setRequiredIndicatorVisible(false);
        description.setWidthFull();
        description.setMinHeight("104px");
        description.setPlaceholder("Briefly describe your company culture, core products, and scale...");
        description.setHelperText("Briefly describe the company's main business activities.");

        newCompanyContainer.add(
                row1,
                row2,
                companyLocation,
                description
        );

        // ---------------------------------------------------------------------
        // Existing Company Container & Search ComboBox
        // ---------------------------------------------------------------------
        existingCompanyContainer.addClassName("register-existing-company-container");
        existingCompanyContainer.setVisible(false);

        companySearch.setLabel("Search Company");
        companySearch.setRequiredIndicatorVisible(true);
        companySearch.setWidthFull();
        companySearch.setPlaceholder("Search verified company by name...");
        companySearch.setItemLabelGenerator(company -> {
            if (company == null) {
                return "";
            }
            String name = company.getName() != null ? company.getName() : "";
            String ind = (company.getIndustry() != null && !company.getIndustry().trim().isEmpty())
                    ? company.getIndustry().trim()
                    : "-";
            String loc = (company.getLocation() != null && !company.getLocation().trim().isEmpty())
                    ? company.getLocation().trim()
                    : "-";
            return name + " — " + ind + " (" + loc + ")";
        });
        companySearch.setItems(
                query -> {
                    String filter = query.getFilter().orElse("");
                    List<Company> list = registrationService.findVerifiedCompanies(filter);
                    return list.stream()
                            .skip(query.getOffset())
                            .limit(query.getLimit());
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    return registrationService.findVerifiedCompanies(filter).size();
                }
        );
        Div joinInfoBox = new Div();
        joinInfoBox.addClassName("register-join-info-box");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
        infoIcon.addClassName("register-join-info-icon");
        Paragraph infoText = new Paragraph(
                "Only verified companies appear in this list. Your request to join will be reviewed before access is granted."
        );
        infoText.addClassName("register-join-info-text");
        joinInfoBox.add(infoIcon, infoText);

        existingCompanyContainer.add(companySearch, joinInfoBox);

        // Step 2 Submit Button
        step2SubmitButton.setWidthFull();
        step2SubmitButton.addClassName("register-submit-btn");
        step2SubmitButton.setText("Register & Submit Company");

        // Step 2 Join Company Login Divider & Outlined Button
        joinLoginDivider = new Div();
        joinLoginDivider.addClassName("register-divider");
        joinLoginDivider.add(new Span("Already have an account?"));
        joinLoginDivider.setVisible(false);

        joinLoginLink = new RouterLink("Login", LoginView.class);
        joinLoginLink.addClassName("register-login-btn");
        joinLoginLink.setVisible(false);

        step2Container.add(
                step2Header,
                step2Title,
                step2Subtitle,
                companyRegistrationType,
                newCompanyContainer,
                existingCompanyContainer,
                step2SubmitButton,
                joinLoginDivider,
                joinLoginLink
        );

        // =====================================================================
        // EVENT LISTENERS
        // =====================================================================
        accountType.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            if (selectedRole == Role.PELAMAR) {
                registerButton.setText("Register");
                clearCompanyInputsAndErrors();
                if (registerCard != null) {
                    registerCard.addClassName("register-card-applicant");
                }
            } else {
                registerButton.setText("Continue");
                if (registerCard != null) {
                    registerCard.removeClassName("register-card-applicant");
                }
            }
        });

        companyRegistrationType.addValueChangeListener(event -> {
            updateCompanyRegistrationVisibility();
        });

        registerButton.addClickListener(event -> {
            if (accountType.getValue() == Role.PELAMAR) {
                handleApplicantRegistration();
            } else {
                handleEmployerStep1Continue();
            }
        });

        step2SubmitButton.addClickListener(event -> {
            handleEmployerFinalSubmit();
        });

        leftPanel.add(step1Container, step2Container);
        return leftPanel;
    }

    private Div buildRightPanel() {
        Div rightPanel = new Div();
        rightPanel.addClassName("register-right-panel");

        Div badge = new Div();
        badge.addClassName("register-badge");
        Icon badgeIcon = VaadinIcon.BRIEFCASE.create();
        badgeIcon.addClassName("register-badge-icon");
        badge.add(badgeIcon, new Span("ENTERPRISE RECRUITMENT"));

        H1 rightHeading = new H1("Simplify job tracking with our portal");
        rightHeading.addClassName("register-right-heading");

        Paragraph rightDesc = new Paragraph(
                "Manage your job applications, track recruitment stages, and connect with verified employers in one place."
        );
        rightDesc.addClassName("register-right-desc");

        // 1. Applicant Benefit Container (Step 1)
        applicantBenefitContainer = new Div();
        applicantBenefitContainer.addClassName("register-benefit-container");

        String[] applicantBenefits = new String[]{
                "Real-time application status tracking from applied to offered",
                "Direct connection with verified companies and recruiters",
                "One-click CV submission and saved job bookmarks"
        };

        for (String benefitText : applicantBenefits) {
            Div item = new Div();
            item.addClassName("register-benefit-item");
            Icon check = VaadinIcon.CHECK.create();
            check.addClassName("register-benefit-check");
            Span text = new Span(benefitText);
            text.addClassName("register-benefit-text");
            item.add(check, text);
            applicantBenefitContainer.add(item);
        }

        // 2. Employer Benefit Container (Step 2)
        employerBenefitContainer = new Div();
        employerBenefitContainer.addClassName("register-benefit-container");
        employerBenefitContainer.setVisible(false);

        String[] employerBenefits = new String[]{
                "Publish job openings and manage candidate pipelines easily",
                "Verified organization badge to attract top-tier talent",
                "Centralized team access for recruiters and hiring managers"
        };

        for (String benefitText : employerBenefits) {
            Div item = new Div();
            item.addClassName("register-benefit-item");
            Icon check = VaadinIcon.CHECK.create();
            check.addClassName("register-benefit-check");
            Span text = new Span(benefitText);
            text.addClassName("register-benefit-text");
            item.add(check, text);
            employerBenefitContainer.add(item);
        }

        // 3. Illustration Card with 20px rounded image
        illustrationCard = new Div();
        illustrationCard.addClassName("register-illustration-card");

        StreamResource defaultIllustResource = new StreamResource("logo_register.png", () ->
                getClass().getResourceAsStream("/META-INF/resources/images/logo_register.png")
        );
        defaultIllustrationImg = new Image(defaultIllustResource, "Job Application Tracker Registration");
        defaultIllustrationImg.addClassName("register-illustration-img");

        StreamResource companyIllustResource = new StreamResource("company.jfif", () ->
                getClass().getResourceAsStream("/META-INF/resources/images/company.jfif")
        );
        companyIllustResource.setContentType("image/jpeg");
        companyIllustrationImg = new Image(companyIllustResource, "Enterprise Company Registration");
        companyIllustrationImg.addClassName("register-illustration-img");
        companyIllustrationImg.addClassName("register-company-illustration-img");
        companyIllustrationImg.setVisible(false);

        illustrationCard.add(defaultIllustrationImg, companyIllustrationImg);

        // 4. Employer Footer (Step 2)
        employerFooter = new Div();
        employerFooter.addClassName("register-right-footer");
        employerFooter.setVisible(false);

        Div footerLeft = new Div();
        footerLeft.addClassName("register-footer-item");
        Icon shieldIcon = VaadinIcon.SHIELD.create();
        shieldIcon.addClassName("register-footer-icon");
        Span verifiedText = new Span("Verified Employer Network");
        footerLeft.add(shieldIcon, verifiedText);

        Div footerRight = new Div();
        footerRight.addClassName("register-footer-item");
        Span slaText = new Span("1–3 Days Review SLA");
        footerRight.add(slaText);

        // 5. Verification & Approval Process Info Box (Step 2 - New Company)
        verificationProcessBox = new Div();
        verificationProcessBox.addClassName("register-verification-box");
        verificationProcessBox.setVisible(false);

        Div verifyHeader = new Div();
        verifyHeader.addClassName("register-verify-header");
        Icon verifyIcon = VaadinIcon.CHECK_CIRCLE.create();
        verifyIcon.addClassName("register-verify-icon");
        Span verifyTitle = new Span("Verification & Approval Process");
        verifyTitle.addClassName("register-verify-title");
        verifyHeader.add(verifyIcon, verifyTitle);

        Paragraph verifyDesc = new Paragraph(
                "New company profiles are reviewed by the platform administrator before verification. You can check the verification status after registration."
        );
        verifyDesc.addClassName("register-verify-desc");

        verificationProcessBox.add(verifyHeader, verifyDesc);

        rightPanel.add(
                badge,
                rightHeading,
                rightDesc,
                applicantBenefitContainer,
                employerBenefitContainer,
                illustrationCard,
                verificationProcessBox,
                employerFooter
        );
        return rightPanel;
    }

    private void switchToStep(int step) {
        this.currentStep = step;
        if (step == 1) {
            step1Container.setVisible(true);
            step2Container.setVisible(false);
            applicantBenefitContainer.setVisible(true);
            employerBenefitContainer.setVisible(false);
            if (defaultIllustrationImg != null) {
                defaultIllustrationImg.setVisible(true);
            }
            if (companyIllustrationImg != null) {
                companyIllustrationImg.setVisible(false);
            }
            if (verificationProcessBox != null) {
                verificationProcessBox.setVisible(false);
            }
            employerFooter.setVisible(false);
            illustrationCard.removeClassName("step-2-illust");
            if (registerCard != null) {
                registerCard.removeClassName("step-2-active");
                registerCard.removeClassName("step-2-new-company");
                registerCard.removeClassName("step-2-join-company");
                if (accountType.getValue() == Role.PELAMAR) {
                    registerCard.addClassName("register-card-applicant");
                } else {
                    registerCard.removeClassName("register-card-applicant");
                }
            }
            if (joinLoginDivider != null) {
                joinLoginDivider.setVisible(false);
            }
            if (joinLoginLink != null) {
                joinLoginLink.setVisible(false);
            }
            if (accountType.getValue() == Role.PEMBERI_LAMARAN) {
                registerButton.setText("Continue");
            } else {
                registerButton.setText("Register");
            }
        } else {
            step1Container.setVisible(false);
            step2Container.setVisible(true);
            applicantBenefitContainer.setVisible(false);
            illustrationCard.addClassName("step-2-illust");
            if (registerCard != null) {
                registerCard.removeClassName("register-card-applicant");
                registerCard.addClassName("step-2-active");
            }
            updateCompanyRegistrationVisibility();
        }
    }

    private void handleEmployerStep1Continue() {
        registerButton.setEnabled(false);

        // Validate Step 1 Account Details
        boolean isValid = binder.validate().isOk();
        if (!isValid) {
            registerButton.setEnabled(true);
            return;
        }

        String emailValue = email.getValue() != null ? email.getValue().trim() : "";
        if (registrationService.emailExists(emailValue)) {
            registerButton.setEnabled(true);
            email.setErrorMessage("Email is already registered.");
            email.setInvalid(true);
            Notification.show("Email is already registered.");
            return;
        }

        // Valid: Switch to Step 2 without writing to database
        registerButton.setEnabled(true);
        switchToStep(2);
    }

    private void handleApplicantRegistration() {
        registerButton.setEnabled(false);
        RegistrationFormData formData = new RegistrationFormData();
        boolean isValid = binder.writeBeanIfValid(formData);

        if (!isValid) {
            registerButton.setEnabled(true);
            return;
        }

        String emailValue = formData.getEmail().trim();
        if (registrationService.emailExists(emailValue)) {
            registerButton.setEnabled(true);
            email.setErrorMessage("Email is already registered.");
            email.setInvalid(true);
            Notification.show("Email is already registered.");
            return;
        }

        try {
            String nameValue = formData.getFullName().trim();
            registrationService.register(
                    nameValue,
                    emailValue,
                    formData.getPassword(),
                    formData.getAccountType(),
                    null
            );

            Notification.show("Registration successful. Please log in.");
            UI.getCurrent().navigate(LoginView.class);
        } catch (Exception ex) {
            registerButton.setEnabled(true);
            Notification.show(ex.getMessage());
        }
    }

    private void handleEmployerFinalSubmit() {
        step2SubmitButton.setEnabled(false);

        RegistrationFormData formData = new RegistrationFormData();
        boolean isStep1Valid = binder.writeBeanIfValid(formData);
        if (!isStep1Valid) {
            step2SubmitButton.setEnabled(true);
            switchToStep(1);
            Notification.show("Silakan periksa kembali data akun Anda.");
            return;
        }

        String emailValue = formData.getEmail().trim();
        if (registrationService.emailExists(emailValue)) {
            step2SubmitButton.setEnabled(true);
            switchToStep(1);
            email.setErrorMessage("Email is already registered.");
            email.setInvalid(true);
            Notification.show("Email is already registered.");
            return;
        }

        CompanyRegistrationType regType = companyRegistrationType.getValue();
        if (regType == null) {
            regType = CompanyRegistrationType.REGISTER_NEW_COMPANY;
        }
        formData.setCompanyRegistrationType(regType);

        if (regType == CompanyRegistrationType.REGISTER_NEW_COMPANY) {
            boolean companyFormValid = true;

            String nameVal = companyName.getValue() != null ? companyName.getValue().trim() : "";
            if (nameVal.isEmpty()) {
                companyName.setErrorMessage("Company Name harus diisi.");
                companyName.setInvalid(true);
                companyFormValid = false;
            } else {
                companyName.setInvalid(false);
                companyName.setErrorMessage(null);
            }

            String industryVal = industry.getValue() != null ? industry.getValue().trim() : "";
            if (industryVal.isEmpty()) {
                industry.setErrorMessage("Industry harus dipilih.");
                industry.setInvalid(true);
                companyFormValid = false;
            } else {
                industry.setInvalid(false);
                industry.setErrorMessage(null);
            }

            String locationVal = companyLocation.getValue() != null ? companyLocation.getValue().trim() : "";
            if (locationVal.isEmpty()) {
                companyLocation.setErrorMessage("Company Location harus diisi.");
                companyLocation.setInvalid(true);
                companyFormValid = false;
            } else {
                companyLocation.setInvalid(false);
                companyLocation.setErrorMessage(null);
            }

            String contactVal = officialContact.getValue() != null ? officialContact.getValue().trim() : "";
            if (contactVal.isEmpty()) {
                officialContact.setErrorMessage("Official Company Email / Contact harus diisi.");
                officialContact.setInvalid(true);
                companyFormValid = false;
            } else {
                officialContact.setInvalid(false);
                officialContact.setErrorMessage(null);
            }

            String descVal = description.getValue() != null ? description.getValue().trim() : "";
            if (descVal.isEmpty()) {
                description.setErrorMessage("Company Description harus diisi.");
                description.setInvalid(true);
                companyFormValid = false;
            } else {
                description.setInvalid(false);
                description.setErrorMessage(null);
            }

            if (!companyFormValid) {
                step2SubmitButton.setEnabled(true);
                return;
            }

            formData.setCompanyName(nameVal);
            formData.setIndustry(industryVal);
            formData.setLocation(locationVal);
            formData.setWebsite(website.getValue() != null ? website.getValue().trim() : "");
            formData.setOfficialContact(contactVal);
            formData.setDescription(descVal);

            try {
                registrationService.registerEmployerWithNewCompany(formData);
                showCompanyRegistrationSuccessDialog();
            } catch (IllegalStateException ex) {
                step2SubmitButton.setEnabled(true);
                String msg = ex.getMessage();
                Notification.show(msg);
                if (msg != null && msg.contains("Join Existing Company")) {
                    companyRegistrationType.setValue(CompanyRegistrationType.JOIN_EXISTING_COMPANY);
                    updateCompanyRegistrationVisibility();
                }
            } catch (Exception ex) {
                step2SubmitButton.setEnabled(true);
                Notification.show(ex.getMessage());
            }
        } else if (regType == CompanyRegistrationType.JOIN_EXISTING_COMPANY) {
            Company selectedCompany = companySearch.getValue();
            if (selectedCompany == null || selectedCompany.getId() == null) {
                step2SubmitButton.setEnabled(true);
                companySearch.setErrorMessage("Silakan pilih perusahaan terlebih dahulu.");
                companySearch.setInvalid(true);
                return;
            } else {
                companySearch.setInvalid(false);
                companySearch.setErrorMessage(null);
            }

            formData.setSelectedCompanyId(selectedCompany.getId());
            formData.setCompanyName(selectedCompany.getName());

            try {
                registrationService.registerEmployerWithExistingCompany(formData);
                showJoinCompanySuccessDialog(selectedCompany.getName());
            } catch (Exception ex) {
                step2SubmitButton.setEnabled(true);
                Notification.show(ex.getMessage());
            }
        }
    }

    private void updateCompanyRegistrationVisibility() {
        CompanyRegistrationType regType = companyRegistrationType.getValue();
        if (regType == null) {
            regType = CompanyRegistrationType.REGISTER_NEW_COMPANY;
            companyRegistrationType.setValue(regType);
        }

        if (regType == CompanyRegistrationType.JOIN_EXISTING_COMPANY) {
            newCompanyContainer.setVisible(false);
            existingCompanyContainer.setVisible(true);
            clearNewCompanyErrors();

            // Dynamic Header Badge for Join Existing Company
            if (stepBadge != null) {
                stepBadge.removeAll();
                Icon checkIcon = VaadinIcon.CHECK_SQUARE_O.create();
                checkIcon.addClassName("register-step-badge-icon");
                stepBadge.add(checkIcon, new Span("Step 2 of 2: Organization Binding"));
                stepBadge.addClassName("register-step-badge-join");
            }

            // Dynamic Heading and Subtitle
            if (step2Title != null) {
                step2Title.setText("Join Existing Company");
            }
            if (step2Subtitle != null) {
                step2Subtitle.setText("Connect your account with an already registered organization on our platform.");
            }

            // Radio button label
            companyRegistrationType.setLabel("COMPANY REGISTRATION OPTION");
            companyRegistrationType.addClassName("register-radio-join");

            // Submit button
            step2SubmitButton.setText("Register & Join Company");
            step2SubmitButton.addClassName("register-submit-join");

            // Divider and Login Link
            if (joinLoginDivider != null) {
                joinLoginDivider.setVisible(true);
            }
            if (joinLoginLink != null) {
                joinLoginLink.setVisible(true);
            }

            // Modifier classes on card for isolated compact styling
            if (registerCard != null) {
                registerCard.addClassName("step-2-join-company");
                registerCard.removeClassName("step-2-new-company");
            }

            // Right panel composition: Badge, Heading, Desc, company.jfif
            // Strictly NO benefit list, NO verification box, NO footer SLA!
            if (applicantBenefitContainer != null) {
                applicantBenefitContainer.setVisible(false);
            }
            if (employerBenefitContainer != null) {
                employerBenefitContainer.setVisible(false);
            }
            if (verificationProcessBox != null) {
                verificationProcessBox.setVisible(false);
            }
            if (employerFooter != null) {
                employerFooter.setVisible(false);
            }
            if (defaultIllustrationImg != null) {
                defaultIllustrationImg.setVisible(false);
            }
            if (companyIllustrationImg != null) {
                companyIllustrationImg.setVisible(true);
            }
        } else {
            newCompanyContainer.setVisible(true);
            existingCompanyContainer.setVisible(false);
            companySearch.setInvalid(false);
            companySearch.setErrorMessage(null);

            // Dynamic Header Badge for Register New Company
            if (stepBadge != null) {
                stepBadge.removeAll();
                stepBadge.setText("Step 2 of 2: Company Profile");
                stepBadge.removeClassName("register-step-badge-join");
            }

            // Dynamic Heading and Subtitle
            if (step2Title != null) {
                step2Title.setText("Company Registration");
            }
            if (step2Subtitle != null) {
                step2Subtitle.setText("Set up your organization profile to start posting jobs and reviewing applicants.");
            }

            // Radio button label
            companyRegistrationType.setLabel("Registration Option");
            companyRegistrationType.removeClassName("register-radio-join");

            // Submit button
            step2SubmitButton.setText("Register & Submit Company");
            step2SubmitButton.removeClassName("register-submit-join");

            // Divider and Login Link hidden in New Company mode
            if (joinLoginDivider != null) {
                joinLoginDivider.setVisible(false);
            }
            if (joinLoginLink != null) {
                joinLoginLink.setVisible(false);
            }

            // Modifier classes on card
            if (registerCard != null) {
                registerCard.addClassName("step-2-new-company");
                registerCard.removeClassName("step-2-join-company");
            }

            // Right panel composition for New Company
            if (applicantBenefitContainer != null) {
                applicantBenefitContainer.setVisible(false);
            }
            if (employerBenefitContainer != null) {
                employerBenefitContainer.setVisible(true);
            }
            if (verificationProcessBox != null) {
                verificationProcessBox.setVisible(true);
            }
            if (employerFooter != null) {
                employerFooter.setVisible(false);
            }
            if (defaultIllustrationImg != null) {
                defaultIllustrationImg.setVisible(false);
            }
            if (companyIllustrationImg != null) {
                companyIllustrationImg.setVisible(true);
            }
        }
    }

    private void clearNewCompanyErrors() {
        companyName.setInvalid(false);
        companyName.setErrorMessage(null);
        industry.setInvalid(false);
        industry.setErrorMessage(null);
        companyLocation.setInvalid(false);
        companyLocation.setErrorMessage(null);
        website.setInvalid(false);
        website.setErrorMessage(null);
        officialContact.setInvalid(false);
        officialContact.setErrorMessage(null);
        description.setInvalid(false);
        description.setErrorMessage(null);
    }

    private void clearCompanyInputsAndErrors() {
        companyName.clear();
        industry.clear();
        companyLocation.clear();
        website.clear();
        officialContact.clear();
        description.clear();
        clearNewCompanyErrors();

        companySearch.clear();
        companySearch.setInvalid(false);
        companySearch.setErrorMessage(null);

        companyRegistrationType.setValue(CompanyRegistrationType.REGISTER_NEW_COMPANY);
    }

    private void showCompanyRegistrationSuccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.setWidth("440px");
        dialog.setMaxWidth("92vw");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle()
                .set("text-align", "center")
                .set("padding", "32px 24px 28px 24px")
                .set("box-sizing", "border-box");

        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "52px")
                .set("height", "52px")
                .set("border-radius", "50%")
                .set("background-color", "#DCFCE7")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 16px auto")
                .set("border", "3px solid #FFFFFF")
                .set("box-shadow", "0 0 0 4px rgba(220, 252, 231, 0.6), 0 4px 10px rgba(0, 0, 0, 0.06)");

        Icon checkIcon = VaadinIcon.CHECK.create();
        checkIcon.setSize("24px");
        checkIcon.setColor("#15803D");
        iconContainer.add(checkIcon);

        H2 title = new H2("Company Registration Submitted");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#0F172A")
                .set("line-height", "1.3");

        Paragraph subtitle = new Paragraph("Your company has been successfully registered and is currently under verification.");
        subtitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("font-size", "14px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        Div reviewTimeBox = new Div();
        reviewTimeBox.setWidthFull();
        reviewTimeBox.getStyle()
                .set("background-color", "#F8FAFC")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("padding", "12px 16px")
                .set("margin", "0 0 16px 0")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "8px")
                .set("box-sizing", "border-box");

        Icon hourglassIcon = VaadinIcon.HOURGLASS.create();
        hourglassIcon.setSize("16px");
        hourglassIcon.setColor("#1E293B");

        Span reviewTimeText = new Span("Estimated Review Time: 1–3 Business Days");
        reviewTimeText.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#1E293B");

        reviewTimeBox.add(hourglassIcon, reviewTimeText);

        Paragraph supportingInfo = new Paragraph("Please wait for approval from the application administrator. No further action is required at this time.");
        supportingInfo.getStyle()
                .set("margin", "0 0 24px 0")
                .set("font-size", "13.5px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        Button gotItButton = new Button("Got It");
        gotItButton.setWidthFull();
        gotItButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        gotItButton.getStyle()
                .set("height", "44px")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("background-color", "#1E293B")
                .set("color", "#FFFFFF")
                .set("cursor", "pointer");

        gotItButton.addClickListener(event -> {
            dialog.close();
            resetButtons();
            UI.getCurrent().navigate(LoginView.class);
        });

        layout.add(
                iconContainer,
                title,
                subtitle,
                reviewTimeBox,
                supportingInfo,
                gotItButton
        );

        dialog.add(layout);
        dialog.open();
    }

    private void showJoinCompanySuccessDialog(String companyName) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.setWidth("440px");
        dialog.setMaxWidth("92vw");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle()
                .set("text-align", "center")
                .set("padding", "32px 24px 28px 24px")
                .set("box-sizing", "border-box");

        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "52px")
                .set("height", "52px")
                .set("border-radius", "50%")
                .set("background-color", "#DCFCE7")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 16px auto")
                .set("border", "3px solid #FFFFFF")
                .set("box-shadow", "0 0 0 4px rgba(220, 252, 231, 0.6), 0 4px 10px rgba(0, 0, 0, 0.06)");

        Icon checkIcon = VaadinIcon.CHECK.create();
        checkIcon.setSize("24px");
        checkIcon.setColor("#15803D");
        iconContainer.add(checkIcon);

        H2 title = new H2("Permintaan Bergabung Terkirim");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#0F172A")
                .set("line-height", "1.3");

        String compText = (companyName != null && !companyName.trim().isEmpty()) ? companyName : "perusahaan";
        Paragraph subtitle = new Paragraph("Permintaan Anda untuk bergabung dengan " + compText + " berhasil dikirim.");
        subtitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "14px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        Paragraph supportingInfo = new Paragraph("Status keanggotaan Anda saat ini adalah PENDING. Silakan tunggu persetujuan dari administrator perusahaan sebelum mengakses area Employer.");
        supportingInfo.getStyle()
                .set("margin", "0 0 24px 0")
                .set("font-size", "13.5px")
                .set("color", "#475569")
                .set("line-height", "1.5");

        Button gotItButton = new Button("Got It");
        gotItButton.setWidthFull();
        gotItButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        gotItButton.getStyle()
                .set("height", "44px")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("background-color", "#1E293B")
                .set("color", "#FFFFFF")
                .set("cursor", "pointer");

        gotItButton.addClickListener(event -> {
            dialog.close();
            resetButtons();
            UI.getCurrent().navigate(LoginView.class);
        });

        layout.add(
                iconContainer,
                title,
                subtitle,
                supportingInfo,
                gotItButton
        );

        dialog.add(layout);
        dialog.open();
    }

    private void resetButtons() {
        registerButton.setEnabled(true);
        if (accountType.getValue() == Role.PEMBERI_LAMARAN) {
            registerButton.setText("Continue");
        } else {
            registerButton.setText("Register");
        }
        step2SubmitButton.setEnabled(true);
    }
}
