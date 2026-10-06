package com.example.view;

import com.example.entity.JobApplication;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.JobApplicationService;
import com.example.view.component.ApplicationDashboard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Job Application Tracker")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final JobApplicationService service;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public DashboardView(
            JobApplicationService service,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {
        this.service = service;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        addClassName("view-container");
        setWidthFull();

        createDashboard();
    }

    private User getCurrentUser() {
        String email = authenticationContext
                .getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User belum login"));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));
    }

    private void createDashboard() {
        H2 title = new H2("Dashboard Ringkasan");

        ApplicationDashboard dashboard = new ApplicationDashboard();

        boolean isAdmin = authenticationContext.hasRole("ADMIN");
        List<JobApplication> data;

        if (isAdmin) {
            data = service.findAll();
        } else {
            User currentUser = getCurrentUser();
            data = service.findByApplicant(currentUser);
        }

        dashboard.updateData(data);

        add(title, dashboard);
    }
}
