package com.example.view.component;

import com.example.entity.ApplicationTipe;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import java.util.function.Consumer;

public class JobPostingForm extends FormLayout {

    private TextField position = new TextField("Posisi");
    private TextField companyName = new TextField("Perusahaan");
    private TextField location = new TextField("Lokasi");
    private Select<ApplicationTipe> tipeKerja = new Select<>();
    private Select<JobTipe> jobTipe = new Select<>();
    private NumberField salaryMin = new NumberField("Gaji Minimum");
    private NumberField salaryMax = new NumberField("Gaji Maksimum");
    private TextArea description = new TextArea("Deskripsi");
    private TextArea requirements = new TextArea("Persyaratan");
    private BeanValidationBinder<JobPosting> binder = new BeanValidationBinder<>(JobPosting.class);

    private Button saveButton = new Button("Simpan");
    private Button cancelButton = new Button("Batal");
    private Button deleteButton = new Button("Hapus");

    private Consumer<JobPosting> saveListener;
    private Consumer<JobPosting> deleteListener;
    private Runnable cancelListener;


    public JobPostingForm() {
        companyName.setReadOnly(true);

        tipeKerja.setLabel("Tipe Kerja");
        tipeKerja.setItems(ApplicationTipe.values());
        tipeKerja.setEmptySelectionAllowed(true);
        tipeKerja.setEmptySelectionCaption("Pilih tipe kerja");


        jobTipe.setLabel("Jenis Pekerjaan");
        jobTipe.setItems(JobTipe.values());
        jobTipe.setEmptySelectionAllowed(true);
        jobTipe.setEmptySelectionCaption("Pilih jenis pekerjaan");

        binder.forField(salaryMin)
        .withConverter(
            value -> value == null ? null : value.longValue(),
            value -> value == null ? null : value.doubleValue()
        )
        .bind(
            JobPosting::getSalaryMin,
            JobPosting::setSalaryMin
        );

        binder.forField(salaryMax)
        .withConverter(
            value -> value == null ? null : value.longValue(),
            value -> value == null ? null : value.doubleValue()
        )
        .bind(
            JobPosting::getSalaryMax,
            JobPosting::setSalaryMax
        );

    binder.bindInstanceFields(this);

    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    saveButton.addClickListener(event -> {
        JobPosting jobPosting = binder.getBean();

        if(jobPosting != null && binder.validate().isOk()){
            if(saveListener != null){
                saveListener.accept(jobPosting);
            }
        }
    });

    cancelButton.addClickListener(event -> {
        if(cancelListener != null){
            cancelListener.run();
        }
    });

    deleteButton.addClickListener(event -> {

    JobPosting jobPosting = binder.getBean();

    if (jobPosting != null && deleteListener != null) {
        deleteListener.accept(jobPosting);
    }
    });

    add(position, companyName, location, tipeKerja, jobTipe, salaryMin, salaryMax, description, requirements,saveButton, cancelButton, deleteButton);
}

    public void setJobPosting(JobPosting jobPosting) {
        binder.setBean(jobPosting);
    }

    public void setSaveListener(Consumer<JobPosting> saveListener) {
        this.saveListener = saveListener;
    }

    public void setDeleteListener(Consumer<JobPosting> deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }

}
