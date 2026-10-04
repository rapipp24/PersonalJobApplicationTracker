package com.example.view.component;

import com.example.entity.JobPosting;

import com.vaadin.flow.component.grid.Grid;

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
}
