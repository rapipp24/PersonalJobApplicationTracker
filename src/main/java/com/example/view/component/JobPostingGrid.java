package com.example.view.component;

import com.example.entity.JobPosting;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;

import java.util.function.Consumer;

public class JobPostingGrid extends Grid<JobPosting> {

    public JobPostingGrid() {
        super(JobPosting.class, false);

        addColumn(JobPosting::getPosition)
                .setHeader("Posisi");

        addColumn(JobPosting::getCompanyName)
                .setHeader("Perusahaan");

        addColumn(JobPosting::getLocation)
                .setHeader("Lokasi");

        addColumn(JobPosting::getTipeKerja)
                .setHeader("Tipe Kerja");

        addColumn(JobPosting::getJobTipe)
                .setHeader("Jenis Pekerjaan");
    }

    public void addEmployerColumn() {
        addColumn(jobPosting -> jobPosting.getEmployer().getName())
                .setHeader("Pemberi Lamaran");
    }

    public void addStatusColumn() {
        addComponentColumn(this::createStatusBadge)
                .setHeader("Status");
    }

    private Span createStatusBadge(JobPosting jobPosting) {
        Span badge = new Span();
        badge.addClassName("status-badge");

        if (jobPosting.isActive()) {
            badge.setText("ACTIVE");
            badge.addClassName("active");
        } else {
            badge.setText("INACTIVE");
            badge.addClassName("inactive");
        }

        return badge;
    }

    public void addActiveActionColumn(Consumer<JobPosting> toggleListener) {
        addComponentColumn(jobPosting -> {
            Button actionButton = new Button();
            actionButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

            if (jobPosting.isActive()) {
                actionButton.setText("Deactivate");
                actionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            } else {
                actionButton.setText("Activate");
                actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }

            actionButton.addClickListener(event -> {
                if (toggleListener != null) {
                    toggleListener.accept(jobPosting);
                }
            });

            return actionButton;
        }).setHeader("Aksi");
    }

    public void addApplicantsActionColumn(Consumer<JobPosting> viewListener) {
        addComponentColumn(jobPosting -> {
            Button viewButton = new Button("Lihat Pelamar");
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewButton.addClickListener(event -> {
                if (viewListener != null) {
                    viewListener.accept(jobPosting);
                }
            });
            return viewButton;
        }).setHeader("Pelamar");
    }
}
