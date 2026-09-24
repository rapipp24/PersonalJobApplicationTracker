package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

public class ApplicationDashboard extends HorizontalLayout {

    private StatSummaryCard totalCard =
        new StatSummaryCard("Total Lamaran");

    private StatSummaryCard diprosesCard =
            new StatSummaryCard("Diproses");

    private StatSummaryCard diterimaCard =
            new StatSummaryCard("Diterima");

    private StatSummaryCard ditolakCard =
            new StatSummaryCard("Ditolak");


    public ApplicationDashboard() {
    
        add(
            totalCard,
            diprosesCard,
            diterimaCard,
            ditolakCard
            );
    }

    public void updateStats(
        long total,
        long diproses,
        long diterima,
        long ditolak
    )
    {
        totalCard.setValue(total);
        diprosesCard.setValue(diproses);
        diterimaCard.setValue(diterima);
        ditolakCard.setValue(ditolak);
    }

    public void updateData(List<JobApplication> data) {
        long total = data.size();

        long diproses = data.stream()
        .filter(jobApplication -> 
                jobApplication.getStatus() == ApplicationStatus.APPLIED
                 ||
                jobApplication.getStatus() == ApplicationStatus.SCREENING
                 ||
                jobApplication.getStatus() == ApplicationStatus.TECHNICAL_TEST
                 ||
                jobApplication.getStatus() == ApplicationStatus.INTERVIEW
        )
        .count();

        long diterima = data.stream()
        .filter(jobApplication ->
                jobApplication.getStatus() == ApplicationStatus.OFFERED
        )
        .count();

        long ditolak = data.stream()
        .filter(jobApplication ->
                jobApplication.getStatus() == ApplicationStatus.REJECTED
        )
        .count();

        updateStats(total, diproses, diterima, ditolak);
    }
}