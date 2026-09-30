package com.example.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();

        H1 logo = new H1("Job Application Tracker");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        String userEmail = authenticationContext.getPrincipalName().orElse("Unknown");
        String userRoles = String.join(", ", authenticationContext.getGrantedRoles());

        Span userInfo = new Span(userEmail + " (" + userRoles + ")");
        userInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button logoutButton = new Button("Logout", event -> authenticationContext.logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(
                drawerToggle,
                logo,
                userInfo,
                logoutButton
        );

        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink homeLink = new RouterLink("Home", MainView.class);
        VerticalLayout navLayout = new VerticalLayout(homeLink);

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPelamar = authenticationContext.hasRole("PELAMAR");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");

        if (isPelamar || isAdmin) {
            RouterLink pelamarLink = new RouterLink("Area Pelamar", PelamarView.class);
            navLayout.add(pelamarLink);
        }

        if (isPemberiLamaran || isAdmin) {
            RouterLink pemberiLamaranLink = new RouterLink("Area Pemberi Lamaran", PemberiLamaranView.class);
            navLayout.add(pemberiLamaranLink);
        }

        if (isAdmin) {
            RouterLink adminLink = new RouterLink("Area Admin", AdminView.class);
            navLayout.add(adminLink);
        }

        addToDrawer(navLayout);
    }
}
