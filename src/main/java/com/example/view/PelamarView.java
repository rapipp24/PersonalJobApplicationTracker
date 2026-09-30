package com.example.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("pelamar")
@PageTitle("Area Pelamar | Job Tracker")
@RolesAllowed({"PELAMAR", "ADMIN"})
public class PelamarView extends VerticalLayout {

    public PelamarView() {
        H1 title = new H1("Area Pelamar");
        Paragraph description = new Paragraph("Halaman ini hanya dapat diakses oleh Pelamar dan Admin.");

        Button backButton = new Button("Kembali ke Halaman Utama", event -> {
            UI.getCurrent().navigate("");
        });

        add(title, description, backButton);
    }
}
