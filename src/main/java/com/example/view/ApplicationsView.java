package com.example.view;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.service.JobApplicationService;
import com.example.view.component.JobApplicationForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Route(value = "applications", layout = MainLayout.class)
@PageTitle("Applications | Job Application Tracker")
@PermitAll
public class ApplicationsView extends VerticalLayout {

    private final JobApplicationService service;

    private final TextField search = new TextField("Cari Lamaran");
    private final ComboBox<ApplicationStatus> filterStatus = new ComboBox<>("Filter Status");
    private final Grid<JobApplication> tableLamaran = new Grid<>(JobApplication.class, false);
    private final JobApplicationForm form = new JobApplicationForm();

    public ApplicationsView(JobApplicationService service) {
        this.service = service;

        addClassName("view-container");
        setWidthFull();

        createFormSection();
        createTableSection();

        refreshGrid();
        resetForm();
    }

    private void createFormSection() {
        H2 formTitle = new H2("Tambah Lamaran");

        form.setSaveListener(jobApplication -> {
            service.simpanLamaran(jobApplication);
            refreshGrid();
            resetForm();
            Notification.show("Lamaran berhasil disimpan");
        });

        form.setCancelListener(this::resetForm);

        form.setDeleteListener(jobApplication -> {
            service.hapusLamaran(jobApplication);
            refreshGrid();
            resetForm();
            Notification.show("Lamaran berhasil dihapus");
        });

        add(formTitle, form);
    }

    private void createTableSection() {
        H2 tableTitle = new H2("Daftar Lamaran");

        HorizontalLayout filterLayout = createFilterLayout();
        Anchor exportLink = createExportButton();
        configureGrid();

        add(tableTitle, filterLayout, exportLink, tableLamaran);
    }

    private HorizontalLayout createFilterLayout() {
        search.setPlaceholder("Cari perusahaan, posisi, atau catatan...");
        search.setWidth("400px");
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.addValueChangeListener(event -> refreshGrid());

        filterStatus.setItems(ApplicationStatus.values());
        filterStatus.setClearButtonVisible(true);
        filterStatus.setWidth("220px");
        filterStatus.addValueChangeListener(event -> refreshGrid());

        HorizontalLayout filterLayout = new HorizontalLayout(search, filterStatus);
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.addClassName("filter-layout");

        return filterLayout;
    }

    private Anchor createExportButton() {
        Button exportButton = new Button("Export CSV");
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        StreamResource csvResource = new StreamResource("job-applications.csv", () -> {
            List<JobApplication> dataExport = service.findAll();
            StringBuilder csv = new StringBuilder();

            csv.append("Perusahaan,Posisi,Status,Tanggal Melamar,Ekspektasi Gaji,Catatan\n");

            for (JobApplication jobApplication : dataExport) {
                csv.append(jobApplication.getCompanyName() != null ? jobApplication.getCompanyName() : "")
                        .append(",")
                        .append(jobApplication.getPosition() != null ? jobApplication.getPosition() : "")
                        .append(",")
                        .append(jobApplication.getStatus() != null ? jobApplication.getStatus() : "")
                        .append(",")
                        .append(jobApplication.getApplicationDate() != null ? jobApplication.getApplicationDate() : "")
                        .append(",")
                        .append(jobApplication.getExpectedSalary() != null ? jobApplication.getExpectedSalary() : "")
                        .append(",")
                        .append(jobApplication.getNotes() != null ? jobApplication.getNotes() : "")
                        .append("\n");
            }

            return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
        });

        Anchor exportLink = new Anchor(csvResource, "");
        exportLink.getElement().setAttribute("download", true);
        exportLink.add(exportButton);

        return exportLink;
    }

    private void configureGrid() {
        tableLamaran.addColumn(JobApplication::getCompanyName)
                .setHeader("Perusahaan")
                .setSortable(true);

        tableLamaran.addColumn(JobApplication::getPosition)
                .setHeader("Posisi")
                .setSortable(true);

        tableLamaran.addComponentColumn(this::createStatusBadge)
                .setHeader("Status");

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
            return String.format("Rp %,d", salary).replace(',', '.');
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

        tableLamaran.addComponentColumn(this::createActionButtons)
                .setHeader("Aksi");

        tableLamaran.setWidthFull();
        tableLamaran.setHeight("420px");

        tableLamaran.addItemClickListener(event -> {
            JobApplication jobApplication = event.getItem();
            if (jobApplication != null) {
                form.setJobApplication(jobApplication);
            }
        });
    }

    private Span createStatusBadge(JobApplication jobApplication) {
        ApplicationStatus status = jobApplication.getStatus();
        Span badgeStatus = new Span(status != null ? status.name() : "-");
        badgeStatus.addClassName("status-badge");

        if (status != null) {
            String cssClass = status.name().toLowerCase().replace('_', '-');
            badgeStatus.addClassName(cssClass);
        }

        return badgeStatus;
    }

    private HorizontalLayout createActionButtons(JobApplication jobApplication) {
        Button editButton = new Button("Edit");
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        editButton.addClickListener(event -> form.setJobApplication(jobApplication));

        Button deleteButton = new Button("Hapus");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteButton.addClickListener(event -> openDeleteConfirmDialog(jobApplication));

        return new HorizontalLayout(editButton, deleteButton);
    }

    private void openDeleteConfirmDialog(JobApplication jobApplication) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Hapus Lamaran");
        dialog.setText("Anda yakin ingin menghapus lamaran " + jobApplication.getCompanyName() + " - " + jobApplication.getPosition() + "?");
        dialog.setCancelable(true);
        dialog.setCancelText("Batal");
        dialog.setConfirmText("Hapus");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            service.hapusLamaran(jobApplication);
            refreshGrid();
            resetForm();
            Notification.show("Lamaran berhasil dihapus");
        });

        dialog.open();
    }

    private void resetForm() {
        JobApplication jobApplicationBaru = new JobApplication();
        jobApplicationBaru.setStatus(ApplicationStatus.APPLIED);
        jobApplicationBaru.setApplicationDate(LocalDate.now());
        form.setJobApplication(jobApplicationBaru);
    }

    private void refreshGrid() {
        String keyword = search.getValue();
        ApplicationStatus statusDipilih = filterStatus.getValue();
        tableLamaran.setItems(service.findAll(keyword, statusDipilih));
    }
}
