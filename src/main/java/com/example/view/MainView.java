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

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;

@Route("")
public class MainView extends VerticalLayout {

    public MainView(JobApplicationService service) {

        H1 judul = new H1("Job Application Tracker");
        setWidthFull();
        setMaxWidth("1200px");
        getStyle().set("margin", "0 auto");

        TextField search = new TextField("Cari Lamaran");
        search.setPlaceholder("Cari perusahaan, posisi, atau catatan...");
        search.setWidth("400px");
        search.setValueChangeMode(ValueChangeMode.EAGER);


        ComboBox<ApplicationStatus> filterStatus =
                new ComboBox<>("Filter Status");
        filterStatus.setItems(ApplicationStatus.values());
        filterStatus.setClearButtonVisible(true);
        filterStatus.setWidth("220px");

        HorizontalLayout filterLayout =
                new HorizontalLayout(search, filterStatus);
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.getStyle().set("flex-wrap", "wrap");

        TextField companyName =
        new TextField("Nama Perusahaan");
        companyName.setRequiredIndicatorVisible(true);
        companyName.setWidth("320px");

        TextField position =
        new TextField("Posisi / Jabatan");
        position.setRequiredIndicatorVisible(true);
        position.setWidth("320px");

        DatePicker applicationDate =
        new DatePicker("Tanggal Melamar");
        applicationDate.setValue(LocalDate.now());
        applicationDate.setWidth("220px");

        ComboBox<ApplicationStatus> status =
        new ComboBox<>("Status");
        status.setItems(ApplicationStatus.values());
        status.setValue(ApplicationStatus.APPLIED);
        status.setWidth("220px");

        ComboBox<ApplicationTipe> tipeKerja =
        new ComboBox<>("Tipe Kerja");
        tipeKerja.setItems(ApplicationTipe.values());
        tipeKerja.setWidth("220px");


        NumberField expectedSalary =
        new NumberField("Ekspektasi Gaji");
        expectedSalary.setWidth("220px");

        TextArea notes =
                new TextArea("Catatan / Feedback");
        notes.setWidthFull();
        notes.setMaxWidth("680px");

        HorizontalLayout baris1 = new HorizontalLayout(
        companyName, position
        );
        baris1.setWidthFull();
        baris1.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout baris2 = new HorizontalLayout(
                applicationDate, status, tipeKerja, expectedSalary
        );
        baris2.setWidthFull();
        baris2.getStyle().set("flex-wrap", "wrap");

        final JobApplication[] dataSedangDiedit = {null};

        Button saveButton =
                new Button("Simpan Lamaran");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Grid<JobApplication> tableLamaran =
                new Grid<>(JobApplication.class, false);


        tableLamaran.addColumn(JobApplication::getCompanyName)
                .setHeader("Perusahaan").setSortable(true);

        tableLamaran.addColumn(JobApplication::getPosition)
                .setHeader("Posisi").setSortable(true);

        tableLamaran.addComponentColumn(jobApplication -> {

            Span badgeStatus =
                    new Span(jobApplication.getStatus().name());

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

        tableLamaran.addColumn(JobApplication::getExpectedSalary)
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

        tableLamaran.setItems(service.findAll());
        
        List<JobApplication> semuaLamaran = service.findAll();
        long totalLamaran = semuaLamaran.size();


        long totalDiproses = semuaLamaran.stream()
                .filter(jobApplication -> 
                jobApplication.getStatus() == ApplicationStatus.APPLIED
                || jobApplication.getStatus() == ApplicationStatus.SCREENING
                || jobApplication.getStatus() == ApplicationStatus.TECHNICAL_TEST
                || jobApplication.getStatus() == ApplicationStatus.INTERVIEW)
                .count();

        long totalDiterima = semuaLamaran.stream()
                .filter(jobApplication -> jobApplication.getStatus() == ApplicationStatus.OFFERED)
                .count();

        long totalDitolak = semuaLamaran.stream()
                .filter(jobApplication -> jobApplication.getStatus() == ApplicationStatus.REJECTED)
                .count();


        Span labelTotalLamaran = new Span("Total Lamaran");
        H2 angkaTotalLamaran = new H2(String.valueOf(totalLamaran));
        VerticalLayout cardTotalLamaran = new VerticalLayout(labelTotalLamaran, angkaTotalLamaran);

        Span labelDiproses = new Span("Diproses");
        H2 angkaDiproses = new H2(String.valueOf(totalDiproses));
        VerticalLayout cardDiproses = new VerticalLayout(labelDiproses, angkaDiproses);

        Span labelDiterima = new Span("Diterima");
        H2 angkaDiterima = new H2(String.valueOf(totalDiterima));
        VerticalLayout cardDiterima = new VerticalLayout(labelDiterima, angkaDiterima);

        Span labelDitolak = new Span("Ditolak");
        H2 angkaDitolak = new H2(String.valueOf(totalDitolak));
        VerticalLayout cardDitolak = new VerticalLayout(labelDitolak, angkaDitolak);


        HorizontalLayout dashboard = new HorizontalLayout(
                cardTotalLamaran,
                cardDiproses,
                cardDiterima,
                cardDitolak
        );

        cardTotalLamaran.getStyle()
                .set("padding", "16px 24px")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("font-weight", "600");

        cardDiproses.getStyle()
                .set("padding", "16px 24px")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("background-color", "#FEF3C7");

        cardDiterima.getStyle()
                .set("padding", "16px 24px")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("background-color", "#DCFCE7");

        cardDitolak.getStyle()
                .set("padding", "16px 24px")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("background-color", "#FEE2E2");

        dashboard.setWidthFull();
        dashboard.setSpacing(true);


        dashboard.getStyle().set("flex-wrap", "wrap");

        cardTotalLamaran.setWidth("220px");
        cardDiproses.setWidth("220px");
        cardDiterima.setWidth("220px");
        cardDitolak.setWidth("220px");

        cardTotalLamaran.setSpacing(false);
        cardDiproses.setSpacing(false);
        cardDiterima.setSpacing(false);
        cardDitolak.setSpacing(false);

        angkaTotalLamaran.getStyle().set("margin", "4px 0 0 0");
        angkaDiproses.getStyle().set("margin", "4px 0 0 0");
        angkaDiterima.getStyle().set("margin", "4px 0 0 0");
        angkaDitolak.getStyle().set("margin", "4px 0 0 0");

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

        List<JobApplication> dataTerbaru = service.findAll();
        angkaTotalLamaran.setText(String.valueOf(dataTerbaru.size()));
        long diprosesTerbaru = dataTerbaru.stream()
        .filter(dataLamaran ->
                dataLamaran.getStatus() == ApplicationStatus.APPLIED
                ||
                dataLamaran.getStatus() == ApplicationStatus.SCREENING
                ||
                dataLamaran.getStatus() == ApplicationStatus.TECHNICAL_TEST
                ||
                dataLamaran.getStatus() == ApplicationStatus.INTERVIEW
        )
        .count();

                angkaDiproses.setText(
                        String.valueOf(diprosesTerbaru)
                );

                long diterimaTerbaru = dataTerbaru.stream()
                        .filter(dataLamaran ->
                                dataLamaran.getStatus() == ApplicationStatus.OFFERED
                        )
                        .count();

                angkaDiterima.setText(
                        String.valueOf(diterimaTerbaru)
                );

                long ditolakTerbaru = dataTerbaru.stream()
                        .filter(dataLamaran ->
                                dataLamaran.getStatus() == ApplicationStatus.REJECTED
                        )
                        .count();

                angkaDitolak.setText(
                        String.valueOf(ditolakTerbaru)
                );

        if(dataSedangDiedit[0] == jobApplication) {

        dataSedangDiedit[0] = null;

        companyName.clear();    
        position.clear();
        expectedSalary.clear();
        notes.clear();

        applicationDate.setValue(LocalDate.now());
        status.setValue(ApplicationStatus.APPLIED);
        tipeKerja.clear();

        companyName.setInvalid(false);
        position.setInvalid(false);

        saveButton.setText("Simpan Lamaran");
    }

        tableLamaran.setItems(service.findAll());

        Notification.show("Lamaran berhasil dihapus");
        });
                dialog.open();
        });

        editButton.addClickListener(event -> {

                dataSedangDiedit[0] = jobApplication ;

                companyName.setValue(jobApplication.getCompanyName());
                position.setValue(jobApplication.getPosition());
                applicationDate.setValue(jobApplication.getApplicationDate());
                status.setValue(jobApplication.getStatus());

                if(jobApplication.getExpectedSalary() != null) {
                        expectedSalary.setValue(jobApplication.getExpectedSalary().doubleValue());
                } else {
                        expectedSalary.clear();
                }

                if(jobApplication.getNotes() != null) {
                        notes.setValue(jobApplication.getNotes());
                } else {
                        notes.clear();
                }

                saveButton.setText("Update Lamaran");
        });

        return new HorizontalLayout(
                editButton,
                deleteButton
        );
        }).setHeader("Aksi");

        search.addValueChangeListener(event -> {

            String keyword = search.getValue().toLowerCase();

            List<JobApplication> hasilFilter =
                    service.findAll()
                            .stream()
                            .filter(jobApplication -> {

                                boolean cocokSearch =
                                        jobApplication.getCompanyName()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        jobApplication.getPosition()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        (
                                            jobApplication.getNotes() != null
                                            &&
                                            jobApplication.getNotes()
                                                .toLowerCase()
                                                .contains(keyword)
                                        );

                                boolean cocokStatus =
                                        filterStatus.getValue() == null
                                        ||
                                        jobApplication.getStatus()
                                                == filterStatus.getValue();

                                return cocokSearch && cocokStatus;
                            })
                            .toList();

            tableLamaran.setItems(hasilFilter);
        });

        filterStatus.addValueChangeListener(event -> {

            String keyword = search.getValue().toLowerCase();

            List<JobApplication> hasilFilter =
                    service.findAll()
                            .stream()
                            .filter(jobApplication -> {

                                boolean cocokSearch =
                                        jobApplication.getCompanyName()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        jobApplication.getPosition()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        (
                                            jobApplication.getNotes() != null
                                            &&
                                            jobApplication.getNotes()
                                                .toLowerCase()
                                                .contains(keyword)
                                        );

                                boolean cocokStatus =
                                        filterStatus.getValue() == null
                                        ||
                                        jobApplication.getStatus()
                                                == filterStatus.getValue();

                                return cocokSearch && cocokStatus;
                            })
                            .toList();

            tableLamaran.setItems(hasilFilter);
        });
        
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

            JobApplication jobApplication ;

            if(dataSedangDiedit[0] != null) {
                    jobApplication = dataSedangDiedit[0];
            } else {
                jobApplication = new JobApplication();
            }
                jobApplication.setCompanyName(companyName.getValue());
                jobApplication.setPosition(position.getValue());
                jobApplication.setApplicationDate(applicationDate.getValue());
                jobApplication.setStatus(status.getValue());
                jobApplication.settipeKerja(tipeKerja.getValue());
                jobApplication.setNotes(notes.getValue());

            if(expectedSalary.getValue() != null) {
                jobApplication.setExpectedSalary(
                        expectedSalary.getValue().longValue()
                );
            }

            service.simpanLamaran(jobApplication);

            List<JobApplication> dataTerbaru = service.findAll();

            angkaTotalLamaran.setText(
                String.valueOf(dataTerbaru.size())
            );

            long diprosesTerbaru = dataTerbaru.stream()
        .filter(dataLamaran ->
                dataLamaran.getStatus() == ApplicationStatus.APPLIED
                ||
                dataLamaran.getStatus() == ApplicationStatus.SCREENING
                ||
                dataLamaran.getStatus() == ApplicationStatus.TECHNICAL_TEST
                ||
                dataLamaran.getStatus() == ApplicationStatus.INTERVIEW
        )
        .count();

                angkaDiproses.setText(
                        String.valueOf(diprosesTerbaru)
                );

        long diterimaTerbaru = dataTerbaru.stream()
        .filter(dataLamaran ->
                dataLamaran.getStatus() == ApplicationStatus.OFFERED
        )
        .count();

                angkaDiterima.setText(
                        String.valueOf(diterimaTerbaru)
                );

                long ditolakTerbaru = dataTerbaru.stream()
                        .filter(dataLamaran ->
                                dataLamaran.getStatus() == ApplicationStatus.REJECTED
                        )
                        .count();

                angkaDitolak.setText(
                        String.valueOf(ditolakTerbaru)
                );
            String keyword = search.getValue().toLowerCase();

            List<JobApplication> hasilFilter =
                    service.findAll()
                            .stream()
                            .filter(dataLamaran -> {

                                boolean cocokSearch =
                                        dataLamaran.getCompanyName()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        dataLamaran.getPosition()
                                                .toLowerCase()
                                                .contains(keyword)
                                        ||
                                        (
                                            dataLamaran.getNotes() != null
                                            &&
                                            dataLamaran.getNotes()
                                                .toLowerCase()
                                                .contains(keyword)
                                        );

                                boolean cocokStatus =
                                        filterStatus.getValue() == null
                                        ||
                                        dataLamaran.getStatus()
                                                == filterStatus.getValue();

                                return cocokSearch && cocokStatus;
                            })
                            .toList();

            tableLamaran.setItems(hasilFilter);

            Notification.show("Lamaran berhasil disimpan");

            dataSedangDiedit[0] = null;
            saveButton.setText("Simpan");

            companyName.clear();
            position.clear();
            expectedSalary.clear();
            notes.clear();

            companyName.setInvalid(false);
            position.setInvalid(false);

            applicationDate.setValue(LocalDate.now());
            status.setValue(ApplicationStatus.APPLIED);
        });

        H2 judulDaftar = new H2("Daftar Lamaran");
        H2 judulForm = new H2("Tambah Lamaran");
                
        add(
                judul,
                dashboard,

                judulForm,
                baris1,
                baris2,
                notes,
                saveButton,

                judulDaftar,
                filterLayout,
                exportLink,
                tableLamaran
        );
    }
}