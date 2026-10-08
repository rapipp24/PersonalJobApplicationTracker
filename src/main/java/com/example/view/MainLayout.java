package com.example.view;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

@StyleSheet("context://styles.css")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private RouterLink employerDashboardLink;
    private RouterLink employerJobPostingsLink;
    private boolean isEmployer = false;

    private RouterLink applicantDashboardLink;
    private RouterLink applicantBrowseJobsLink;
    private RouterLink applicantApplicationsLink;
    private boolean isApplicant = false;

    public MainLayout(
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");
        boolean isPelamar = authenticationContext.hasRole("PELAMAR");

        if (isPemberiLamaran && !isAdmin) {
            this.isEmployer = true;
            addClassName("employer-app-layout");
            setPrimarySection(Section.DRAWER);
            createEmployerHeader();
            createEmployerDrawer();
        } else if (isPelamar && !isAdmin) {
            this.isApplicant = true;
            addClassName("applicant-app-layout");
            setPrimarySection(Section.DRAWER);
            createApplicantHeader();
            createApplicantDrawer();
        } else {
            setPrimarySection(Section.NAVBAR);
            createHeader();
            createDrawer();
        }
    }

    private void createEmployerHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClassName("employer-drawer-toggle");

        Div spacer = new Div();

        MenuBar accountMenu = createAccountMenu();

        HorizontalLayout header = new HorizontalLayout(
                drawerToggle,
                spacer,
                accountMenu
        );

        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(spacer);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        header.addClassName("employer-navbar-header");

        addToNavbar(header);
    }

    private void createEmployerDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setWidthFull();
        drawerLayout.addClassName("employer-sidebar-layout");

        // 1. BRAND AREA
        Div brandArea = new Div();
        brandArea.addClassName("employer-sidebar-brand");

        Div brandContainer = new Div();
        brandContainer.addClassName("employer-brand-container");

        Div iconBox = new Div();
        iconBox.addClassName("employer-brand-icon-box");
        Html brandIcon = new Html("<iconify-icon icon=\"lucide:briefcase-business\" class=\"employer-brand-icon\"></iconify-icon>");
        iconBox.add(brandIcon);

        Div textGroup = new Div();
        textGroup.addClassName("employer-brand-text-group");

        Span brandTitle = new Span("Job Application Tracker");
        brandTitle.addClassName("employer-brand-title");

        Span badge = new Span("EMPLOYER");
        badge.addClassName("employer-brand-badge");

        textGroup.add(brandTitle, badge);
        brandContainer.add(iconBox, textGroup);
        brandArea.add(brandContainer);

        // 2. SIDEBAR CONTENT
        Div sidebarContent = new Div();
        sidebarContent.addClassName("employer-sidebar-content");

        employerDashboardLink = new RouterLink();
        employerDashboardLink.setRoute(DashboardView.class);
        employerDashboardLink.addClassName("employer-nav-item");

        Html dashIcon = new Html("<iconify-icon icon=\"lucide:layout-dashboard\" class=\"employer-nav-lucide-icon\"></iconify-icon>");

        Span dashLabel = new Span("Dashboard");
        dashLabel.addClassName("employer-nav-label");

        employerDashboardLink.add(dashIcon, dashLabel);

        employerJobPostingsLink = new RouterLink();
        employerJobPostingsLink.setRoute(PemberiLamaranView.class);
        employerJobPostingsLink.addClassName("employer-nav-item");

        Html jobsIcon = new Html("<iconify-icon icon=\"lucide:file-text\" class=\"employer-nav-lucide-icon\"></iconify-icon>");

        Span jobsLabel = new Span("Job Postings");
        jobsLabel.addClassName("employer-nav-label");

        employerJobPostingsLink.add(jobsIcon, jobsLabel);

        sidebarContent.add(employerDashboardLink, employerJobPostingsLink);

        drawerLayout.add(brandArea, sidebarContent);
        addToDrawer(drawerLayout);
    }

    private void createApplicantHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClassName("applicant-drawer-toggle");

        Div spacer = new Div();

        MenuBar accountMenu = createAccountMenu();

        HorizontalLayout header = new HorizontalLayout(
                drawerToggle,
                spacer,
                accountMenu
        );

        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(spacer);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        header.addClassName("applicant-navbar-header");

        addToNavbar(header);
    }

    private void createApplicantDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setWidthFull();
        drawerLayout.addClassName("applicant-sidebar-layout");

        // 1. BRAND AREA
        Div brandArea = new Div();
        brandArea.addClassName("applicant-sidebar-brand");

        Div brandContainer = new Div();
        brandContainer.addClassName("applicant-brand-container");

        Div iconBox = new Div();
        iconBox.addClassName("applicant-brand-icon-box");
        Html brandIcon = new Html("<iconify-icon icon=\"lucide:briefcase-business\" class=\"applicant-brand-icon\"></iconify-icon>");
        iconBox.add(brandIcon);

        Div textGroup = new Div();
        textGroup.addClassName("applicant-brand-text-group");

        Span brandTitle = new Span("Job Application Tracker");
        brandTitle.addClassName("applicant-brand-title");

        Span badge = new Span("APPLICANT");
        badge.addClassName("applicant-brand-badge");

        textGroup.add(brandTitle, badge);
        brandContainer.add(iconBox, textGroup);
        brandArea.add(brandContainer);

        // 2. SIDEBAR CONTENT
        Div sidebarContent = new Div();
        sidebarContent.addClassName("applicant-sidebar-content");

        // Link 1: Dashboard
        applicantDashboardLink = new RouterLink();
        applicantDashboardLink.setRoute(DashboardView.class);
        applicantDashboardLink.addClassName("applicant-nav-item");
        Html dashIcon = new Html("<iconify-icon icon=\"lucide:layout-dashboard\" class=\"applicant-nav-lucide-icon\"></iconify-icon>");
        Span dashLabel = new Span("Dashboard");
        dashLabel.addClassName("applicant-nav-label");
        applicantDashboardLink.add(dashIcon, dashLabel);

        // Link 2: Browse Jobs
        applicantBrowseJobsLink = new RouterLink();
        applicantBrowseJobsLink.setRoute(PelamarView.class);
        applicantBrowseJobsLink.addClassName("applicant-nav-item");
        Html browseIcon = new Html("<iconify-icon icon=\"lucide:search\" class=\"applicant-nav-lucide-icon\"></iconify-icon>");
        Span browseLabel = new Span("Browse Jobs");
        browseLabel.addClassName("applicant-nav-label");
        applicantBrowseJobsLink.add(browseIcon, browseLabel);

        // Link 3: Applications
        applicantApplicationsLink = new RouterLink();
        applicantApplicationsLink.setRoute(ApplicationsView.class);
        applicantApplicationsLink.addClassName("applicant-nav-item");
        Html appsIcon = new Html("<iconify-icon icon=\"lucide:file-text\" class=\"applicant-nav-lucide-icon\"></iconify-icon>");
        Span appsLabel = new Span("Applications");
        appsLabel.addClassName("applicant-nav-label");
        applicantApplicationsLink.add(appsIcon, appsLabel);

        sidebarContent.add(applicantDashboardLink, applicantBrowseJobsLink, applicantApplicationsLink);

        drawerLayout.add(brandArea, sidebarContent);
        addToDrawer(drawerLayout);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();
        if (isEmployer) {
            updateEmployerNavActiveState(path);
        } else if (isApplicant) {
            updateApplicantNavActiveState(path);
        }
    }

    private void updateApplicantNavActiveState(String path) {
        if (applicantDashboardLink == null || applicantBrowseJobsLink == null || applicantApplicationsLink == null) {
            return;
        }

        applicantDashboardLink.removeClassName("applicant-nav-item-active");
        applicantBrowseJobsLink.removeClassName("applicant-nav-item-active");
        applicantApplicationsLink.removeClassName("applicant-nav-item-active");

        if (path == null || path.isBlank()) {
            applicantDashboardLink.addClassName("applicant-nav-item-active");
        } else if (path.startsWith("pelamar")) {
            applicantBrowseJobsLink.addClassName("applicant-nav-item-active");
        } else if (path.startsWith("applications")) {
            applicantApplicationsLink.addClassName("applicant-nav-item-active");
        }
    }

    private void updateEmployerNavActiveState(String path) {
        if (employerDashboardLink == null || employerJobPostingsLink == null) {
            return;
        }

        employerDashboardLink.removeClassName("employer-nav-item-active");
        employerJobPostingsLink.removeClassName("employer-nav-item-active");

        if (path == null || path.isBlank()) {
            employerDashboardLink.addClassName("employer-nav-item-active");
        } else if (path.startsWith("pemberi-lamaran")) {
            employerJobPostingsLink.addClassName("employer-nav-item-active");
        }
    }


    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();

        H1 logo = new H1("Job Application Tracker");
        logo.addClassName("app-logo");

        MenuBar accountMenu = createAccountMenu();

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

    private MenuBar createAccountMenu() {
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

        return accountMenu;
    }
}
