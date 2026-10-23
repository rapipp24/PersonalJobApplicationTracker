package com.example.view;

import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
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

    private RouterLink adminDashboardLink;
    private RouterLink adminVerificationLink;
    private RouterLink adminCompaniesLink;
    private boolean isAdminUser = false;

    private RouterLink employerDashboardLink;
    private RouterLink employerJobPostingsLink;
    private RouterLink employerMemberRequestsLink;
    private boolean isEmployer = false;

    private RouterLink applicantDashboardLink;
    private RouterLink applicantBrowseJobsLink;
    private RouterLink applicantApplicationsLink;
    private boolean isApplicant = false;

    private Span accountNameSpan;

    public MainLayout(
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        boolean isPemberiLamaran = authenticationContext.hasRole("PEMBERI_LAMARAN");
        boolean isPelamar = authenticationContext.hasRole("PELAMAR");

        if (isAdmin) {
            this.isAdminUser = true;
            addClassName("employer-app-layout");
            addClassName("admin-app-layout");
            setPrimarySection(Section.DRAWER);
            createAdminHeader();
            createAdminDrawer();
        } else if (isPemberiLamaran) {
            this.isEmployer = true;
            addClassName("employer-app-layout");
            setPrimarySection(Section.DRAWER);
            createEmployerHeader();
            createEmployerDrawer();
        } else if (isPelamar) {
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

    private void createAdminHeader() {
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

    private void createAdminDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setWidthFull();
        drawerLayout.addClassName("employer-sidebar-layout");

        // 1. BRAND AREA
        Div brandArea = new Div();
        brandArea.addClassName("employer-sidebar-brand");

        Div brandContainer = new Div();
        brandContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "4px")
                .set("width", "100%");

        Span brandTitle = new Span("Job Application Tracker");
        brandTitle.addClassName("employer-brand-title");

        Span brandSubtitle = new Span("PLATFORM ADMIN");
        brandSubtitle.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.06em")
                .set("color", "#64748B")
                .set("text-transform", "uppercase")
                .set("line-height", "1.2");

        brandContainer.add(brandTitle, brandSubtitle);
        brandArea.add(brandContainer);

        // 2. SIDEBAR CONTENT
        Div sidebarContent = new Div();
        sidebarContent.addClassName("employer-sidebar-content");

        // SECTION 1: PLATFORM MANAGEMENT
        Span platformMgmtHeader = createAdminNavSectionHeader("PLATFORM MANAGEMENT");
        sidebarContent.add(platformMgmtHeader);

        // 1. Dashboard
        adminDashboardLink = new RouterLink();
        adminDashboardLink.setRoute(DashboardView.class);
        adminDashboardLink.addClassName("employer-nav-item");
        Html dashIcon = new Html("<iconify-icon icon=\"lucide:layout-dashboard\" class=\"employer-nav-lucide-icon\"></iconify-icon>");
        Span dashLabel = new Span("Dashboard");
        dashLabel.addClassName("employer-nav-label");
        adminDashboardLink.add(dashIcon, dashLabel);
        sidebarContent.add(adminDashboardLink);

        // 2. Company Verification
        adminVerificationLink = new RouterLink();
        adminVerificationLink.setRoute(AdminView.class);
        adminVerificationLink.addClassName("employer-nav-item");
        Html verifyIcon = new Html("<iconify-icon icon=\"lucide:shield-check\" class=\"employer-nav-lucide-icon\"></iconify-icon>");
        Span verifyLabel = new Span("Company Verification");
        verifyLabel.addClassName("employer-nav-label");
        adminVerificationLink.add(verifyIcon, verifyLabel);
        sidebarContent.add(adminVerificationLink);

        // 3. Companies
        adminCompaniesLink = new RouterLink();
        adminCompaniesLink.setRoute(AdminCompaniesView.class);
        adminCompaniesLink.addClassName("employer-nav-item");
        Html companiesIcon = new Html("<iconify-icon icon=\"lucide:building-2\" class=\"employer-nav-lucide-icon\"></iconify-icon>");
        Span companiesLabel = new Span("Companies");
        companiesLabel.addClassName("employer-nav-label");
        adminCompaniesLink.add(companiesIcon, companiesLabel);
        sidebarContent.add(adminCompaniesLink);

        // 4. Users (Future Phase 3 - Visual / Non-navigable)
        Div usersItem = createDisabledAdminNavItem("lucide:users", "Users");
        sidebarContent.add(usersItem);

        // SECTION 2: PLATFORM MONITORING
        Span platformMonHeader = createAdminNavSectionHeader("PLATFORM MONITORING");
        sidebarContent.add(platformMonHeader);

        // 5. Job Postings (Future Phase 5 - Visual / Non-navigable)
        Div jobPostingsItem = createDisabledAdminNavItem("lucide:briefcase", "Job Postings");
        sidebarContent.add(jobPostingsItem);

        // 6. Applications (Future Phase 5 - Visual / Non-navigable)
        Div applicationsItem = createDisabledAdminNavItem("lucide:file-text", "Applications");
        sidebarContent.add(applicationsItem);

        drawerLayout.add(brandArea, sidebarContent);
        addToDrawer(drawerLayout);
    }

    private Span createAdminNavSectionHeader(String text) {
        Span header = new Span(text);
        header.getStyle()
                .set("font-size", "0.68rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.05em")
                .set("color", "#64748B")
                .set("text-transform", "uppercase")
                .set("padding", "14px 14px 6px 14px")
                .set("display", "block");
        return header;
    }

    private Div createDisabledAdminNavItem(String iconName, String labelText) {
        Div item = new Div();
        item.addClassName("employer-nav-item");
        item.getStyle()
                .set("color", "#94A3B8")
                .set("cursor", "default")
                .set("opacity", "0.75")
                .set("pointer-events", "none");

        Html icon = new Html("<iconify-icon icon=\"" + iconName + "\" style=\"font-size: 1.15rem; color: #94A3B8; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0;\"></iconify-icon>");
        Span label = new Span(labelText);
        label.addClassName("employer-nav-label");
        label.getStyle().set("color", "#94A3B8");

        item.add(icon, label);
        return item;
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
        sidebarContent.add(employerDashboardLink);

        if (isCurrentEmployerActive()) {
            employerJobPostingsLink = new RouterLink();
            employerJobPostingsLink.setRoute(PemberiLamaranView.class);
            employerJobPostingsLink.addClassName("employer-nav-item");

            Html jobsIcon = new Html("<iconify-icon icon=\"lucide:file-text\" class=\"employer-nav-lucide-icon\"></iconify-icon>");

            Span jobsLabel = new Span("Job Postings");
            jobsLabel.addClassName("employer-nav-label");

            employerJobPostingsLink.add(jobsIcon, jobsLabel);

            sidebarContent.add(employerJobPostingsLink);
        }

        if (isCurrentCompanyAdmin()) {
            employerMemberRequestsLink = new RouterLink();
            employerMemberRequestsLink.setRoute(CompanyMemberRequestsView.class);
            employerMemberRequestsLink.addClassName("employer-nav-item");

            Html membersIcon = new Html("<iconify-icon icon=\"lucide:users\" class=\"employer-nav-lucide-icon\"></iconify-icon>");

            Span membersLabel = new Span("Member Requests");
            membersLabel.addClassName("employer-nav-label");

            employerMemberRequestsLink.add(membersIcon, membersLabel);

            sidebarContent.add(employerMemberRequestsLink);
        }

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
        if (isAdminUser) {
            updateAdminNavActiveState(path);
        } else if (isEmployer) {
            updateEmployerNavActiveState(path);
        } else if (isApplicant) {
            updateApplicantNavActiveState(path);
        }
    }

    private void updateAdminNavActiveState(String path) {
        if (adminDashboardLink == null || adminVerificationLink == null || adminCompaniesLink == null) {
            return;
        }

        adminDashboardLink.removeClassName("employer-nav-item-active");
        adminVerificationLink.removeClassName("employer-nav-item-active");
        adminCompaniesLink.removeClassName("employer-nav-item-active");

        if (path == null || path.isBlank()) {
            adminDashboardLink.addClassName("employer-nav-item-active");
        } else if (path.startsWith("admin/companies")) {
            adminCompaniesLink.addClassName("employer-nav-item-active");
        } else if (path.equals("admin") || path.equals("admin/")) {
            adminVerificationLink.addClassName("employer-nav-item-active");
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
        } else if (path.startsWith("pelamar") && !path.startsWith("pelamar/profile")) {
            applicantBrowseJobsLink.addClassName("applicant-nav-item-active");
        } else if (path.startsWith("applications")) {
            applicantApplicationsLink.addClassName("applicant-nav-item-active");
        }
    }

    private void updateEmployerNavActiveState(String path) {
        if (employerDashboardLink != null) {
            employerDashboardLink.removeClassName("employer-nav-item-active");
            if (path == null || path.isBlank()) {
                employerDashboardLink.addClassName("employer-nav-item-active");
            }
        }

        if (employerJobPostingsLink != null) {
            employerJobPostingsLink.removeClassName("employer-nav-item-active");
            if (path != null && path.startsWith("pemberi-lamaran")
                    && !path.startsWith("pemberi-lamaran/member-requests")
                    && !path.startsWith("pemberi-lamaran/settings")) {
                employerJobPostingsLink.addClassName("employer-nav-item-active");
            }
        }

        if (employerMemberRequestsLink != null) {
            employerMemberRequestsLink.removeClassName("employer-nav-item-active");
            if (path != null && path.startsWith("pemberi-lamaran/member-requests")) {
                employerMemberRequestsLink.addClassName("employer-nav-item-active");
            }
        }
    }

    private boolean isCurrentEmployerActive() {
        String userEmail = authenticationContext.getPrincipalName().orElse(null);
        if (userEmail == null) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return user.getCompany() != null
                && user.getCompany().getVerificationStatus() == CompanyVerificationStatus.VERIFIED
                && user.getMembershipStatus() == CompanyMembershipStatus.ACTIVE;
    }

    private boolean isCurrentCompanyAdmin() {
        String userEmail = authenticationContext.getPrincipalName().orElse(null);
        if (userEmail == null) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return user.getCompany() != null
                && user.getCompany().getVerificationStatus() == CompanyVerificationStatus.VERIFIED
                && user.getMembershipStatus() == CompanyMembershipStatus.ACTIVE
                && user.getCompanyPermission() == CompanyPermission.COMPANY_ADMIN;
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

        if (isAdmin || (isPemberiLamaran && isCurrentEmployerActive())) {
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

        accountNameSpan = new Span(userName);
        accountNameSpan.addClassName("account-user-name");

        Html dropdownIcon = new Html("<iconify-icon icon=\"lucide:chevron-down\" class=\"account-dropdown-icon\"></iconify-icon>");

        HorizontalLayout accountContent = new HorizontalLayout(userIcon, accountNameSpan, dropdownIcon);
        accountContent.setAlignItems(Alignment.CENTER);
        accountContent.addClassName("account-menu-content");

        MenuItem accountItem = accountMenu.addItem(accountContent);
        if (currentUser.isPresent() && currentUser.get().getRole() == Role.PELAMAR) {
            accountItem.getSubMenu().addItem("Profile & Settings", event -> UI.getCurrent().navigate(ApplicantProfileView.class));
        } else if (currentUser.isPresent() && currentUser.get().getRole() == Role.PEMBERI_LAMARAN) {
            accountItem.getSubMenu().addItem("Profile & Settings", event -> UI.getCurrent().navigate(EmployerSettingsView.class));
        }
        accountItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());

        return accountMenu;
    }

    public void updateCurrentUserDisplayName(String newName) {
        if (accountNameSpan != null && newName != null && !newName.isBlank()) {
            accountNameSpan.setText(newName.trim());
        }
    }
}
