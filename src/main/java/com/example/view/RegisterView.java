package com.example.view;

import com.example.dto.CompanyRegistrationType;
import com.example.dto.RegistrationFormData;
import com.example.entity.Company;
import com.example.entity.Role;
import com.example.service.RegistrationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.IOException;
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

    private final TextField fullName = new TextField("Full Name");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");
    private final RadioButtonGroup<Role> accountType = new RadioButtonGroup<>("Account Type");

    // Task 2: Pilihan Registrasi Employer
    private final RadioButtonGroup<CompanyRegistrationType> companyRegistrationType =
            new RadioButtonGroup<>("Company Registration Option");

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

    // Task 2: Field Register New Company
    private final TextField companyName = new TextField("Company Name");
    private final Select<String> industry = new Select<>();
    private final TextField companyLocation = new TextField("Company Location");
    private final TextField website = new TextField("Official Website (Optional)");
    private final TextField officialContact = new TextField("Official Company Email / Contact");
    private final TextArea description = new TextArea("Company Description");
    private final VerticalLayout newCompanyContainer = new VerticalLayout();

    // Task 2: Pencarian Existing Company
    private final ComboBox<Company> companySearch = new ComboBox<>("Search Company");
    private final VerticalLayout existingCompanyContainer = new VerticalLayout();

    private final Button registerButton = new Button("Register");

    public RegisterView(RegistrationService registrationService) {
        this.registrationService = registrationService;
        this.binder = new BeanValidationBinder<>(RegistrationFormData.class);

        loadStyles();

        addClassNames("auth-view", "register-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        configureBinder();
        createRegisterForm();
    }

    private void loadStyles() {
        try (InputStream is = getClass().getResourceAsStream("/META-INF/resources/styles.css")) {
            if (is != null) {
                String css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                getElement().executeJs(
                        "if (!document.getElementById('app-styles')) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = 'app-styles';" +
                        "  style.textContent = $0;" +
                        "  document.head.appendChild(style);" +
                        "}",
                        css
                );
            }
        } catch (IOException ignored) {
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
        H1 title = new H1("Job Application Tracker");
        title.addClassName("auth-title");

        VerticalLayout form = new VerticalLayout();
        form.addClassNames("auth-form", "register-form");
        form.setPadding(false);
        form.setSpacing(true);
        form.setAlignItems(Alignment.STRETCH);

        H2 formTitle = new H2("Create Account");
        formTitle.addClassName("auth-form-title");

        fullName.setWidthFull();

        email.setWidthFull();
        email.setErrorMessage("Please enter a valid email address.");

        password.setWidthFull();

        confirmPassword.setWidthFull();

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

        // Konfigurasi opsi tipe registrasi Company
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
        companyRegistrationType.setVisible(false);

        // Konfigurasi container dan field New Company
        newCompanyContainer.setWidthFull();
        newCompanyContainer.setPadding(false);
        newCompanyContainer.setSpacing(true);
        newCompanyContainer.setVisible(false);

        companyName.setWidthFull();
        companyName.setHelperText("Enter the official company name. Example: PT Muara Buana");

        industry.setLabel("Industry *");
        industry.setPlaceholder("Pilih Industri");
        industry.setItems(INDUSTRY_OPTIONS);
        industry.setWidthFull();
        industry.setHelperText("Select the company's primary industry.");

        companyLocation.setWidthFull();
        companyLocation.setHelperText("Enter the main location of the company. Example: Bandar Lampung");

        website.setWidthFull();
        website.setHelperText("Enter the official company website if available. Example: https://company.com");

        officialContact.setWidthFull();
        officialContact.setHelperText("Use an official company email if available. Example: hr@company.com or contact@company.com");

        description.setWidthFull();
        description.setMinHeight("80px");
        description.setHelperText("Briefly describe the company's main business activities.");

        newCompanyContainer.add(
                companyName,
                industry,
                companyLocation,
                website,
                officialContact,
                description
        );

        // Konfigurasi container dan pencarian Existing Company
        existingCompanyContainer.setWidthFull();
        existingCompanyContainer.setPadding(false);
        existingCompanyContainer.setSpacing(true);
        existingCompanyContainer.setVisible(false);

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

        existingCompanyContainer.add(companySearch);

        // Listener perubahan Account Type
        accountType.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            if (selectedRole != Role.PEMBERI_LAMARAN) {
                clearCompanyInputsAndErrors();
            }
            updateCompanyRegistrationVisibility();
        });

        // Listener perubahan Opsi Registrasi Company
        companyRegistrationType.addValueChangeListener(event -> {
            updateCompanyRegistrationVisibility();
        });

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClassName("auth-submit-btn");
        registerButton.setDisableOnClick(true);

        HorizontalLayout loginLayout = new HorizontalLayout(
                new Span("Already have an account?"),
                new RouterLink("Login", LoginView.class)
        );
        loginLayout.setAlignItems(Alignment.CENTER);
        loginLayout.addClassName("register-link-layout");

        registerButton.addClickListener(event -> {
            RegistrationFormData formData = new RegistrationFormData();
            boolean isValid = binder.writeBeanIfValid(formData);

            if (!isValid) {
                resetRegisterButton();
                return;
            }

            if (formData.getAccountType() == Role.PEMBERI_LAMARAN) {
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
                        resetRegisterButton();
                        return;
                    }

                    String emailValue = formData.getEmail().trim();
                    if (registrationService.emailExists(emailValue)) {
                        resetRegisterButton();
                        Notification.show("Email is already registered.");
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
                        resetRegisterButton();
                        String msg = ex.getMessage();
                        Notification.show(msg);
                        if (msg != null && msg.contains("Join Existing Company")) {
                            companyRegistrationType.setValue(CompanyRegistrationType.JOIN_EXISTING_COMPANY);
                            updateCompanyRegistrationVisibility();
                        }
                    } catch (Exception ex) {
                        resetRegisterButton();
                        Notification.show(ex.getMessage());
                    }
                    return;
                } else if (regType == CompanyRegistrationType.JOIN_EXISTING_COMPANY) {
                    Company selectedCompany = companySearch.getValue();
                    if (selectedCompany == null || selectedCompany.getId() == null) {
                        resetRegisterButton();
                        companySearch.setErrorMessage("Silakan pilih perusahaan terlebih dahulu.");
                        companySearch.setInvalid(true);
                        return;
                    } else {
                        companySearch.setInvalid(false);
                        companySearch.setErrorMessage(null);
                    }

                    String emailValue = formData.getEmail().trim();
                    if (registrationService.emailExists(emailValue)) {
                        resetRegisterButton();
                        Notification.show("Email is already registered.");
                        return;
                    }

                    formData.setSelectedCompanyId(selectedCompany.getId());
                    formData.setCompanyName(selectedCompany.getName());

                    resetRegisterButton();
                    Notification.show("Perusahaan telah dipilih. Proses pengajuan bergabung dengan perusahaan belum tersedia.");
                    return;
                }
            }

            // Flow pendaftaran PELAMAR (Applicant) tetap normal
            String emailValue = formData.getEmail().trim();
            if (registrationService.emailExists(emailValue)) {
                resetRegisterButton();
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
                resetRegisterButton();
                Notification.show(ex.getMessage());
            }
        });

        form.add(
                formTitle,
                fullName,
                email,
                password,
                confirmPassword,
                accountType,
                companyRegistrationType,
                newCompanyContainer,
                existingCompanyContainer,
                registerButton
        );

        add(title, form, loginLayout);
    }

    private void updateCompanyRegistrationVisibility() {
        Role role = accountType.getValue();
        boolean isEmployer = (role == Role.PEMBERI_LAMARAN);
        companyRegistrationType.setVisible(isEmployer);

        if (!isEmployer) {
            newCompanyContainer.setVisible(false);
            existingCompanyContainer.setVisible(false);
            return;
        }

        CompanyRegistrationType regType = companyRegistrationType.getValue();
        if (regType == null) {
            regType = CompanyRegistrationType.REGISTER_NEW_COMPANY;
            companyRegistrationType.setValue(regType);
        }

        if (regType == CompanyRegistrationType.JOIN_EXISTING_COMPANY) {
            newCompanyContainer.setVisible(false);
            existingCompanyContainer.setVisible(true);
            clearNewCompanyErrors();
        } else {
            newCompanyContainer.setVisible(true);
            existingCompanyContainer.setVisible(false);
            companySearch.setInvalid(false);
            companySearch.setErrorMessage(null);
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

        // Icon Container (Circular light green badge with Success Checkmark icon)
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

        // Title
        H2 title = new H2("Company Registration Submitted");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", "#0F172A")
                .set("line-height", "1.3");

        // Subtitle
        Paragraph subtitle = new Paragraph("Your company has been successfully registered and is currently under verification.");
        subtitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("font-size", "14px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        // Estimated Review Time Box
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

        // Supporting Information
        Paragraph supportingInfo = new Paragraph("Please wait for approval from the application administrator. No further action is required at this time.");
        supportingInfo.getStyle()
                .set("margin", "0 0 24px 0")
                .set("font-size", "13.5px")
                .set("color", "#64748B")
                .set("line-height", "1.5");

        // Primary Button (Got It)
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
            resetRegisterButton();
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

    private void resetRegisterButton() {
        registerButton.setEnabled(true);
        registerButton.setText("Register");
    }
}
