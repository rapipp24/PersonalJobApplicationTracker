package com.example.view;

import com.example.entity.JobApplication;
import com.example.entity.ApplicationStatus;
import com.example.service.JobApplicationService;
import com.example.entity.ApplicationTipe;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.example.view.component.JobApplicationForm;
import com.example.view.component.ApplicationDashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;

@Route("")
public class MainView extends VerticalLayout {

        private TextField search = new TextField("Cari Lamaran");

        private ComboBox<ApplicationStatus> filterStatus =
                new ComboBox<>("Filter Status");

        private Grid<JobApplication> tableLamaran =
                new Grid<>(JobApplication.class, false); 

        private final JobApplicationService service;

    public MainView(JobApplicationService service) {

        this.service = service;

        H1 judul = new H1("Job Application Tracker");

        JobApplicationForm form = new JobApplicationForm();
        ApplicationDashboard dashboard = new ApplicationDashboard();
        dashboard.updateData(service.findAll());

        JobApplication jobApplicationBaru = new JobApplication();
        jobApplicationBaru.setStatus(ApplicationStatus.APPLIED);
        jobApplicationBaru.setApplicationDate(LocalDate.now());

        form.setJobApplication(jobApplicationBaru);

        setWidthFull();
        setMaxWidth("1200px");
        getStyle().set("margin", "0 auto");

        //konfigurasi search
        search.setPlaceholder("Cari perusahaan, posisi, atau catatan...");
        search.setWidth("400px");
        search.setValueChangeMode(ValueChangeMode.EAGER);

        // filter
        filterStatus.setItems(ApplicationStatus.values());
        filterStatus.setClearButtonVisible(true);
        filterStatus.setWidth("220px");

        HorizontalLayout filterLayout =
                new HorizontalLayout(search, filterStatus);
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.getStyle().set("flex-wrap", "wrap");
        
        //grid
        tableLamaran.addColumn(JobApplication::getCompanyName)
                .setHeader("Perusahaan").setSortable(true);

        tableLamaran.addColumn(JobApplication::getPosition)
                .setHeader("Posisi").setSortable(true);

        tableLamaran.addComponentColumn(jobApplication -> {

        Span badgeStatus = new Span(jobApplication.getStatus() != null
                ? jobApplication.getStatus().name()
                : "-"
        );
        if (jobApplication.getStatus() == null) {
                return badgeStatus;
         }
            switch (jobApplication.getStatus()) {

                case OFFERED -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#DCFCE7")
                        .set("color", "#166534");
                }

                case REJECTED -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#FEE2E2")
                        .set("color", "#991B1B");
                }

                case INTERVIEW -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#DBEAFE")
                        .set("color", "#1E40AF");
                }

                case TECHNICAL_TEST -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#FEF3C7")
                        .set("color", "#92400E");
                }

                case SCREENING -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#F3E8FF")
                        .set("color", "#6B21A8");
                }

                case APPLIED -> {
                    badgeStatus.getStyle()
                        .set("background-color", "#E5E7EB")
                        .set("color", "#374151");
                }
            }

            badgeStatus.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-weight", "600");

            return badgeStatus;

        }).setHeader("Status");

        tableLamaran.addColumn(JobApplication::gettipeKerja)
                .setHeader("Tipe Kerja")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getApplicationDate)
                .setHeader("Tanggal Melamar")
                .setSortable(true);

        tableLamaran.addColumn(jobApplication -> {

        Long salary = jobApplication.getExpectedSalary();

        if (salary == null) {
                return "-";
        }

        return String.format("Rp %,d", salary)
                .replace(',', '.');
        })
        .setHeader("Ekspektasi Gaji")
        .setComparator(
                Comparator.comparing(
                        JobApplication::getExpectedSalary,
                        Comparator.nullsLast(Long::compareTo)
                )
        )
        .setSortable(true);

        
        tableLamaran.addColumn(JobApplication::getNotes)
                .setHeader("Catatan / Feedback");
        
        tableLamaran.setWidthFull();
        tableLamaran.setHeight("420px");

        tableLamaran.setItems(service.findAll()); //batas


tableLamaran.addItemClickListener(event -> {
        JobApplication jobApplication = event.getItem();
        
        form.setJobApplication(jobApplication);
});


 form.setSaveListener(jobApplication -> {
        service.simpanLamaran(jobApplication);

        dashboard.updateData(service.findAll());
        refreshGrid();
        
        form.setJobApplication(new JobApplication());

        Notification.show("Lamaran berhasil disimpan");
        });



form.setCancelListener(() -> {
        form.setJobApplication(new JobApplication());
        });



form.setDeleteListener(jobApplication -> {
        service.hapusLamaran(jobApplication);
       
        dashboard.updateData(service.findAll());
        refreshGrid();

        form.setJobApplication(new JobApplication());

        Notification.show("Lamaran berhasil dihapus");
        });



        tableLamaran.addComponentColumn(jobApplication -> {
                Button editButton = new Button("Edit");
                editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                Button deleteButton = new Button("Hapus");

                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        deleteButton.addClickListener(event -> {
                ConfirmDialog dialog = new ConfirmDialog();

                dialog.setHeader("Hapus Lamaran");
                dialog.setText("Anda yakin ingin menghapus lamaran " + jobApplication.getCompanyName() + " - " + jobApplication.getPosition() + "?");

                dialog.setCancelable(true);
                dialog.setCancelText("Batal");

                dialog.setConfirmText("Hapus");
                dialog.setConfirmButtonTheme("error primary");


        dialog.addConfirmListener(confirmevent -> {
        service.hapusLamaran(jobApplication);

        dashboard.updateData(service.findAll());
        refreshGrid();

        form.setJobApplication(new JobApplication());

        Notification.show("Lamaran berhasil dihapus");
        });

        dialog.open();
        });

        editButton.addClickListener(event -> {
                form.setJobApplication(jobApplication);
        });

        return new HorizontalLayout(
                editButton,
                deleteButton
        );
        }).setHeader("Aksi");


        search.addValueChangeListener(event -> refreshGrid());

        filterStatus.addValueChangeListener(event -> refreshGrid());

        Button exportButton = new Button("Export CSV");
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        StreamResource csvResource = new StreamResource( "job-applications.csv", () -> {
            List<JobApplication> dataExport = service.findAll();

            StringBuilder csv = new StringBuilder();

            csv.append(
                    "Perusahaan,Posisi,Status,Tanggal Melamar,Ekspektasi Gaji,Catatan\n"
            );

            for (JobApplication jobApplication : dataExport) {

                csv.append(jobApplication.getCompanyName())
                        .append(",")
                        .append(jobApplication.getPosition())
                        .append(",")
                        .append(jobApplication.getStatus())
                        .append(",")
                        .append(jobApplication.getApplicationDate())
                        .append(",")
                        .append(
                                jobApplication.getExpectedSalary() != null
                                        ? jobApplication.getExpectedSalary()
                                        : ""
                        )
                        .append(",")
                        .append(
                                jobApplication.getNotes() != null
                                        ? jobApplication.getNotes()
                                        : ""
                        )
                        .append("\n");
            }

            return new ByteArrayInputStream(
                    csv.toString().getBytes(StandardCharsets.UTF_8)
            );
        }
                );

                Anchor exportLink = new Anchor(csvResource, "");

                exportLink.getElement()
                        .setAttribute("download", true);

                exportLink.add(exportButton);

        H2 judulDaftar = new H2("Daftar Lamaran");
        H2 judulForm = new H2("Tambah Lamaran");
                
        add(
                judul,
                dashboard,

                judulForm,

                form,

                judulDaftar,
                filterLayout,
                exportLink,
                tableLamaran
        );
    }

    private void refreshGrid() {
        String keyword = search.getValue();

        ApplicationStatus statusDipilih = filterStatus.getValue();

        tableLamaran.setItems(
                service.findAll(keyword, statusDipilih)
        );
    }
}