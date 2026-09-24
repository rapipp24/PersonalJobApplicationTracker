package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.ApplicationTipe;
import com.example.entity.JobApplication;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;

import java.util.function.Consumer;
import java.time.LocalDate;
import java.lang.Runnable;

public class JobApplicationForm extends FormLayout {

    private TextField companyName = new TextField("Perusahaan");
    private TextField position = new TextField("Posisi");
    private DatePicker applicationDate = new DatePicker("Tanggal Melamar");
    private ComboBox<ApplicationStatus> status = new ComboBox<>("Status");
    private ComboBox<ApplicationTipe> tipeKerja = new ComboBox<>("Tipe Kerja");
    private NumberField expectedSalary = new NumberField("Ekspektasi Gaji");
    private TextArea notes = new TextArea("Catatan");

    private BeanValidationBinder<JobApplication> binder = new BeanValidationBinder<>(JobApplication.class);

    private Button saveButton = new Button("Simpan");
    private Button cancelButton = new Button("Batal");
    private Button deleteButton = new Button("Hapus");

    private Consumer<JobApplication> saveListener;
    private Consumer<JobApplication> deleteListener;
    private Runnable cancelListener;

    public JobApplicationForm() {
        status.setItems(ApplicationStatus.values());
        status.setValue(ApplicationStatus.APPLIED);

        tipeKerja.setItems(ApplicationTipe.values());
        
        binder.forField(expectedSalary)
            .withConverter(
                value -> value == null ? null : value.longValue(),
                value -> value == null ? null : value.doubleValue()
            )
            .bind(
                JobApplication::getExpectedSalary,
                JobApplication::setExpectedSalary
            );

        binder.bindInstanceFields(this);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveButton.addClickListener(event -> {

        JobApplication jobApplication = binder.getBean();

        if (jobApplication != null && binder.validate().isOk()) {
            if (saveListener != null) {
                saveListener.accept(jobApplication);
            }
        }
    });

        cancelButton.addClickListener(event -> {
            if (cancelListener != null) {
                cancelListener.run();
            }
        });

        deleteButton.addClickListener(event -> {
            JobApplication jobApplication = binder.getBean();

            if (jobApplication != null && deleteListener != null) {
                deleteListener.accept(jobApplication);
            }
        });

        add(
            companyName,
            position,
            applicationDate,
            status,
            tipeKerja,
            expectedSalary,
            notes,
            saveButton,
            cancelButton,
            deleteButton
        );
    }

    public void setJobApplication(JobApplication jobApplication) {

        if (jobApplication.getStatus() == null) {
            jobApplication.setStatus(ApplicationStatus.APPLIED);
        }

        if (jobApplication.getApplicationDate() == null) {
            jobApplication.setApplicationDate(LocalDate.now());
        }
    
        binder.setBean(jobApplication);
    }

    public void setSaveListener(Consumer<JobApplication> saveListener) {
        this.saveListener = saveListener;
    }

    public void setCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }

    public void setDeleteListener(Consumer<JobApplication> deleteListener) {
        this.deleteListener = deleteListener;
    }
}

