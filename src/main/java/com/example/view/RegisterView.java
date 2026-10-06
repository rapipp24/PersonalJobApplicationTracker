package com.example.view;

import com.example.dto.RegistrationFormData;
import com.example.entity.Role;
import com.example.service.RegistrationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
    private final TextField companyName = new TextField("Nama Perusahaan");

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

        // Binding Email dengan Bean Validation + trimming
        binder.forField(email)
                .withConverter(
                        value -> value == null ? "" : value.trim(),
                        value -> value
                )
                .asRequired("Email is required.")
                .bind("email");

        // Binding Password dengan Bean Validation (minimal 8 karakter)
        binder.forField(password)
                .asRequired("Password is required.")
                .bind("password");

        // Binding Confirm Password dengan custom cross-field validator
        binder.forField(confirmPassword)
                .asRequired("Password confirmation is required.")
                .withValidator(
                        confirmValue -> confirmValue != null && confirmValue.equals(password.getValue()),
                        "Password confirmation does not match."
                )
                .bind("confirmPassword");

        // Re-validasi confirm password ketika password diubah
        password.addValueChangeListener(event -> {
            if (!confirmPassword.isEmpty()) {
                binder.validate();
            }
        });

        // Binding Account Type dengan Bean Validation (wajib dipilih)
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

        companyName.setWidthFull();
        companyName.setVisible(false);

        // Tampilkan field Nama Perusahaan hanya saat memilih PEMBERI_LAMARAN
        accountType.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            if (selectedRole == Role.PEMBERI_LAMARAN) {
                companyName.setVisible(true);
            } else {
                companyName.setVisible(false);
                companyName.clear();
            }
        });

        Button registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClassName("auth-submit-btn");

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
                return;
            }

            // Validasi tambahan: PEMBERI_LAMARAN wajib mengisi nama perusahaan
            if (formData.getAccountType() == Role.PEMBERI_LAMARAN) {
                String namaPerusahaan = companyName.getValue();
                if (namaPerusahaan == null || namaPerusahaan.trim().isEmpty()) {
                    companyName.setErrorMessage("Nama perusahaan harus diisi.");
                    companyName.setInvalid(true);
                    return;
                }
                formData.setCompanyName(namaPerusahaan.trim());
            }

            String emailValue = formData.getEmail().trim();
            if (registrationService.emailExists(emailValue)) {
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
                        formData.getCompanyName()
                );

                Notification.show("Registration successful. Please log in.");
                UI.getCurrent().navigate(LoginView.class);
            } catch (Exception ex) {
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
                companyName,
                registerButton
        );

        add(title, form, loginLayout);
    }
}
