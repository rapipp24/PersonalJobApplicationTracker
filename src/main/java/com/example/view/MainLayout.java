package com.example.view;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

@StyleSheet("context://styles.css")
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public MainLayout(
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();

        H1 logo = new H1("Job Application Tracker");
        logo.addClassName("app-logo");

        String userEmail = authenticationContext.getPrincipalName().orElse("Unknown");
        String userName = userEmail;

        Optional<User> currentUser = userRepository.findByEmail(userEmail);
        if (currentUser.isPresent()) {
            userName = currentUser.get().getName();
        }

        MenuBar accountMenu = new MenuBar();
        accountMenu.addClassName("account-menu");
        accountMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        Html userIcon = new Html("<iconify-icon icon=\"lucide:user-round\" class=\"user-account-icon\"></iconify-icon>");

        Span nameSpan = new Span(userName);
        nameSpan.addClassName("account-user-name");

        Html dropdownIcon = new Html("<iconify-icon icon=\"lucide:chevron-down\" class=\"account-dropdown-icon\"></iconify-icon>");

        HorizontalLayout accountContent = new HorizontalLayout(userIcon, nameSpan, dropdownIcon);
        accountContent.setAlignItems(Alignment.CENTER);
        accountContent.addClassName("account-menu-content");

        MenuItem accountItem = accountMenu.addItem(accountContent);
        accountItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());

        HorizontalLayout header = new HorizontalLayout(
                drawerToggle,
                logo,
                accountMenu
        );

        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        VerticalLayout navLayout = new VerticalLayout(dashboardLink);

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPelamar = authenticationContext.hasRole("PELAMAR");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");

        if (isPelamar || isAdmin) {
            RouterLink applicationsLink = new RouterLink("Applications", ApplicationsView.class);
            RouterLink applicantLink = new RouterLink("Applicant Area", PelamarView.class);
            navLayout.add(applicationsLink, applicantLink);
        }

        if (isPemberiLamaran || isAdmin) {
            RouterLink employerLink = new RouterLink("Employer Area", PemberiLamaranView.class);
            navLayout.add(employerLink);
        }

        if (isAdmin) {
            RouterLink adminLink = new RouterLink("Admin Area", AdminView.class);
            navLayout.add(adminLink);
        }

        addToDrawer(navLayout);
    }
}
