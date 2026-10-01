package com.example.view;

import com.example.service.JobApplicationService;
import com.example.view.component.ApplicationDashboard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Job Application Tracker")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final JobApplicationService service;

    public DashboardView(JobApplicationService service) {
        this.service = service;

        addClassName("view-container");
        setWidthFull();

        createDashboard();
    }

    private void createDashboard() {
        H2 title = new H2("Dashboard Ringkasan");

        ApplicationDashboard dashboard = new ApplicationDashboard();
        dashboard.updateData(service.findAll());

        add(title, dashboard);
    }
}
