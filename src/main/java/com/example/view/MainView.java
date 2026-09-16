package com.example.view;

import com.example.entity.JobApplication;
import com.example.entity.ApplicationStatus;
import com.example.service.JobApplicationService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.Grid;

import java.time.LocalDate;

@Route("")
public class MainView extends VerticalLayout {

    public MainView(JobApplicationService service) {

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
        companyName.setRequiredIndicatorVisible(true);

        TextField position =
                new TextField("Posisi / Jabatan");
        position.setRequiredIndicatorVisible(true);

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

               Grid<JobApplication> tableLamaran = new Grid<>(JobApplication.class, false);
       
        tableLamaran.addColumn(JobApplication::getCompanyName)
                .setHeader("Perusahaan")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getPosition)
                .setHeader("Posisi")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getStatus)
                .setHeader("Status")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getApplicationDate)
                .setHeader("Tanggal Melamar")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getExpectedSalary)
                .setHeader("Ekspektasi Gaji")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getNotes)
                .setHeader("Catatan / Feedback");


        tableLamaran.setItems(service.findAll());

        Button saveButton =
                new Button("Simpan Lamaran");

        saveButton.addClickListener(event -> {
        
        boolean namaPerusahaanKosong = companyName.isEmpty();
        boolean posisiKosong = position.isEmpty();

        companyName.setInvalid(namaPerusahaanKosong);
        position.setInvalid(posisiKosong);

        companyName.setErrorMessage("Nama perusahaan harus diisi");
        position.setErrorMessage("Posisi harus diisi");

        if(namaPerusahaanKosong || posisiKosong) {
                Notification.show("Lengkapi data terlebih dahulu");
            return;
        }

            JobApplication jobApplication = new JobApplication();

            jobApplication.setCompanyName(companyName.getValue());
            jobApplication.setPosition(position.getValue());
            jobApplication.setApplicationDate(applicationDate.getValue());
            jobApplication.setStatus(status.getValue());
            jobApplication.setNotes(notes.getValue());


            if(expectedSalary.getValue() != null) {
                jobApplication.setExpectedSalary(expectedSalary.getValue().longValue());
            }

            service.simpanLamaran(jobApplication);
            tableLamaran.setItems(service.findAll());
            Notification.show("Lamaran berhasil disimpan");

            companyName.clear();
            position.clear();
            expectedSalary.clear();
            notes.clear();

            companyName.setInvalid(false);
            position.setInvalid(false);

            applicationDate.setValue(LocalDate.now());
            status.setValue(ApplicationStatus.APPLIED);
            
        });

        add(
                judul,
                filterLayout,
                companyName,
                position,
                applicationDate,
                status,
                expectedSalary,
                notes,
                saveButton,
                tableLamaran
        );
    }
}