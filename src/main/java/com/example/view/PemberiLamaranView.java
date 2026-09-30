package com.example.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "pemberi-lamaran", layout = MainLayout.class)
@PageTitle("Area Pemberi Lamaran | Job Tracker")
@RolesAllowed({"PEMBERI_LAMARAN", "ADMIN"})
public class PemberiLamaranView extends VerticalLayout {

    public PemberiLamaranView() {
        H1 title = new H1("Area Pemberi Lamaran");
        Paragraph description = new Paragraph("Halaman ini hanya dapat diakses oleh Pemberi Lamaran dan Admin.");

        Button backButton = new Button("Kembali ke Halaman Utama", event -> {
            UI.getCurrent().navigate("");
        });

        add(title, description, backButton);
    }
}
