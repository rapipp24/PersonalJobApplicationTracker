package com.example.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Job Application Tracker")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassNames("auth-view", "login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Login");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Login");
        i18n.getErrorMessage().setTitle("Login Gagal");
        i18n.getErrorMessage().setMessage("Email atau password yang Anda masukkan salah.");
        loginForm.setI18n(i18n);
        loginForm.setForgotPasswordButtonVisible(false);

        H1 title = new H1("Job Application Tracker");
        title.addClassName("auth-title");

        HorizontalLayout registerLayout = new HorizontalLayout(
                new Span("Don't have an account?"),
                new RouterLink("Create account", RegisterView.class)
        );
        registerLayout.setAlignItems(Alignment.CENTER);
        registerLayout.addClassName("register-link-layout");

        add(title, loginForm, registerLayout);
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
