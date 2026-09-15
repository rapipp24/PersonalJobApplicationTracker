package com.example.view;

import com.example.entity.ApplicationStatus;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {

        H1 judul = new H1("Job Application Tracker");

        TextField search = new TextField("Cari Lamaran");
        search.setPlaceholder("Cari perusahaan atau posisi...");

        ComboBox<ApplicationStatus> filterStatus =
                new ComboBox<>("Filter Status");
        filterStatus.setItems(ApplicationStatus.values());

        HorizontalLayout filterLayout =
                new HorizontalLayout(search, filterStatus);

        TextField companyName =
                new TextField("Nama Perusahaan");

        TextField position =
                new TextField("Posisi / Jabatan");

        DatePicker applicationDate =
                new DatePicker("Tanggal Melamar");
        applicationDate.setValue(LocalDate.now());

        ComboBox<ApplicationStatus> status =
                new ComboBox<>("Status");
        status.setItems(ApplicationStatus.values());
        status.setValue(ApplicationStatus.APPLIED);

        NumberField expectedSalary =
                new NumberField("Ekspektasi Gaji");

        TextArea notes =
                new TextArea("Catatan / Feedback");

        Button saveButton =
                new Button("Simpan Lamaran");

        add(
                judul,
                filterLayout,
                companyName,
                position,
                applicationDate,
                status,
                expectedSalary,
                notes,
                saveButton
        );
    }
}