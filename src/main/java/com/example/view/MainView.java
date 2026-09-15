package com.example;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
@Route ("")
public class MainView extends H1 {
    public MainView() {

        H1 judul = new H1("Job Application Tracker");
        Textfield tombol = new tombol ("Tambah Lamaran");
        Button button = new Button("Tambah ");
        
        add(judul, tombol, button);

    }
    }
