package com.example.view;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Route("login")
@PageTitle("Login | Job Application Tracker")
@AnonymousAllowed
@StyleSheet("context://styles.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassNames("auth-view", "login-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Injeksi stylesheet secara langsung untuk memastikan tampilan ter-render sempurna
        // bahkan ketika server security mencegat permintaan stylesheet statis publik
        try (InputStream is = getClass().getResourceAsStream("/META-INF/resources/styles.css")) {
            if (is != null) {
                String css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                HtmlComponent styleTag = new HtmlComponent("style");
                styleTag.getElement().setAttribute("id", "login-injected-styles");
                styleTag.getElement().setText(css);
                getElement().appendChild(styleTag.getElement());
            }
        } catch (Exception ignored) {
        }

        Div loginCard = new Div();
        loginCard.addClassName("login-card");

        // 1. Panel Kiri (Branding & Ilustrasi)
        Div leftPanel = new Div();
        leftPanel.addClassName("login-left-panel");

        Div badge = new Div();
        badge.addClassName("login-badge");
        Icon badgeIcon = VaadinIcon.BRIEFCASE.create();
        badgeIcon.addClassName("login-badge-icon");
        badge.add(badgeIcon, new Span("ENTERPRISE RECRUITMENT"));

        H1 leftHeading = new H1("Simplify job tracking with our portal");
        leftHeading.addClassName("login-left-heading");

        Paragraph leftDesc = new Paragraph(
                "Manage your job applications, track recruitment stages, and connect with verified employers in one place."
        );
        leftDesc.addClassName("login-left-desc");

        Div illustrationCard = new Div();
        illustrationCard.addClassName("login-illustration-card");
        StreamResource illustResource = new StreamResource("logo.png", () ->
                getClass().getResourceAsStream("/META-INF/resources/images/logo.png")
        );
        Image illustrationImg = new Image(illustResource, "Job Application Tracker Logo");
        illustrationImg.addClassName("login-illustration-img");
        illustrationCard.add(illustrationImg);

        leftPanel.add(badge, leftHeading, leftDesc, illustrationCard);

        // 2. Panel Kanan (Login Form)
        Div rightPanel = new Div();
        rightPanel.addClassName("login-right-panel");

        Div brandHeader = new Div();
        brandHeader.addClassName("login-brand-header");

        Div brandLogoWrapper = new Div();
        brandLogoWrapper.addClassName("login-brand-logo-wrapper");
        StreamResource logoResource = new StreamResource("briefcase-business.svg", () ->
                getClass().getResourceAsStream("/META-INF/resources/images/briefcase-business.svg")
        );
        Image brandLogo = new Image(logoResource, "Job Application Tracker Logo");
        brandLogo.addClassName("login-brand-logo");
        brandLogoWrapper.add(brandLogo);

        Span brandTitle = new Span("Job Application Tracker");
        brandTitle.addClassName("login-brand-title");
        brandHeader.add(brandLogoWrapper, brandTitle);

        H2 welcomeTitle = new H2("Welcome Back");
        welcomeTitle.addClassName("login-form-title");

        Paragraph welcomeSubtitle = new Paragraph("Please login to your account");
        welcomeSubtitle.addClassName("login-form-subtitle");

        // Konfigurasi Vaadin LoginForm
        loginForm.setAction("login");
        loginForm.addClassName("login-vaadin-form");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle(""); // Hilangkan judul internal "Login" agar tidak dobel
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Login");
        i18n.getErrorMessage().setTitle("Login Gagal");
        i18n.getErrorMessage().setMessage("Email atau password yang Anda masukkan salah.");
        loginForm.setI18n(i18n);
        loginForm.setForgotPasswordButtonVisible(false);

        Div divider = new Div();
        divider.addClassName("login-divider");
        Span dividerText = new Span("Don't have an account?");
        divider.add(dividerText);

        RouterLink registerLink = new RouterLink("Register", RegisterView.class);
        registerLink.addClassName("login-register-btn");

        rightPanel.add(brandHeader, welcomeTitle, welcomeSubtitle, loginForm, divider, registerLink);

        loginCard.add(leftPanel, rightPanel);
        add(loginCard);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}

