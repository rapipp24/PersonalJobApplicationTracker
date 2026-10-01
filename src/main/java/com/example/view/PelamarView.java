package com.example.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "pelamar", layout = MainLayout.class)
@PageTitle("Applicant Workspace | Job Tracker")
@RolesAllowed({"PELAMAR", "ADMIN"})
public class PelamarView extends VerticalLayout {

    public PelamarView() {
        H1 title = new H1("Applicant Workspace");
        Paragraph description = new Paragraph("Track and manage your job application journey.");

        add(title, description);
    }
}
