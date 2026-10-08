package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.JobPosting;
import com.example.entity.User;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployerDashboard extends VerticalLayout {

    private final StatSummaryCard totalJobsCard = new StatSummaryCard("Total Lowongan");
    private final StatSummaryCard activeJobsCard = new StatSummaryCard("Lowongan Aktif");
    private final StatSummaryCard inactiveJobsCard = new StatSummaryCard("Lowongan Nonaktif");
    private final StatSummaryCard totalApplicantsCard = new StatSummaryCard("Total Pelamar");

    private final StatSummaryCard diprosesCard = new StatSummaryCard("Pelamar Diproses");
    private final StatSummaryCard diterimaCard = new StatSummaryCard("Pelamar Diterima");
    private final StatSummaryCard ditolakCard = new StatSummaryCard("Pelamar Ditolak");

    private final Paragraph companyInfo = new Paragraph();
    private final Paragraph emptyNotice = new Paragraph("Belum ada pelamar untuk lowongan Anda.");
    private final Grid<JobApplication> recentApplicantsGrid = new Grid<>(JobApplication.class, false);

    public EmployerDashboard() {
        setPadding(false);
        setSpacing(true);
        setWidthFull();

        createHeaderSection();
        createCardsSection();
        createRecentApplicantsSection();
    }

    private void createHeaderSection() {
        companyInfo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-top", "0");
        add(companyInfo);
    }

    private void createCardsSection() {
        HorizontalLayout postingStatsLayout = new HorizontalLayout(
                totalJobsCard,
                activeJobsCard,
                inactiveJobsCard,
                totalApplicantsCard
        );
        postingStatsLayout.setWidthFull();
        postingStatsLayout.addClassName("filter-layout");

        HorizontalLayout applicantStatsLayout = new HorizontalLayout(
                diprosesCard,
                diterimaCard,
                ditolakCard
        );
        applicantStatsLayout.setWidthFull();
        applicantStatsLayout.addClassName("filter-layout");

        add(postingStatsLayout, applicantStatsLayout);
    }

    private void createRecentApplicantsSection() {
        H3 recentTitle = new H3("Pelamar Terbaru");

        emptyNotice.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)");

        recentApplicantsGrid.addColumn(jobApplication -> {
            if (jobApplication.getApplicant() != null && jobApplication.getApplicant().getName() != null) {
                return jobApplication.getApplicant().getName();
            }
            return "Data Pelamar Tidak Tersedia";
        }).setHeader("Nama Pelamar");

        recentApplicantsGrid.addColumn(jobApplication -> {
            if (jobApplication.getJobPosting() != null && jobApplication.getJobPosting().getPosition() != null) {
                return jobApplication.getJobPosting().getPosition();
            }
            if (jobApplication.getPosition() != null) {
                return jobApplication.getPosition();
            }
            return "-";
        }).setHeader("Posisi Lowongan");

        recentApplicantsGrid.addComponentColumn(this::createStatusBadge).setHeader("Status");

        recentApplicantsGrid.addColumn(jobApplication -> {
            if (jobApplication.getApplicationDate() != null) {
                return jobApplication.getApplicationDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
            return "-";
        }).setHeader("Tanggal Melamar");

        recentApplicantsGrid.setWidthFull();
        recentApplicantsGrid.setAllRowsVisible(true);

        add(recentTitle, emptyNotice, recentApplicantsGrid);
    }

    public void updateData(User currentUser, List<JobPosting> jobPostings, List<JobApplication> applications) {
        // Tampilkan info perusahaan jika tersedia
        if (currentUser != null && currentUser.getCompany() != null && currentUser.getCompany().getName() != null) {
            companyInfo.setText("Perusahaan: " + currentUser.getCompany().getName());
            companyInfo.setVisible(true);
        } else {
            companyInfo.setVisible(false);
        }

        // Hitung statistik lowongan kerja (Job Posting)
        int totalJobPostings = jobPostings != null ? jobPostings.size() : 0;
        int activeJobPostings = 0;
        int inactiveJobPostings = 0;

        if (jobPostings != null) {
            for (JobPosting jobPosting : jobPostings) {
                if (jobPosting.isActive()) {
                    activeJobPostings++;
                } else {
                    inactiveJobPostings++;
                }
            }
        }

        totalJobsCard.setValue(totalJobPostings);
        activeJobsCard.setValue(activeJobPostings);
        inactiveJobsCard.setValue(inactiveJobPostings);

        // Hitung statistik pelamar (Job Application)
        int totalApplicants = applications != null ? applications.size() : 0;
        totalApplicantsCard.setValue(totalApplicants);

        int pelamarDiproses = 0;
        int pelamarDiterima = 0;
        int pelamarDitolak = 0;

        if (applications != null) {
            for (JobApplication jobApplication : applications) {
                ApplicationStatus status = jobApplication.getStatus();
                if (status == ApplicationStatus.OFFERED) {
                    pelamarDiterima++;
                } else if (status == ApplicationStatus.REJECTED) {
                    pelamarDitolak++;
                } else if (status != null) {
                    pelamarDiproses++;
                }
            }
        }

        diprosesCard.setValue(pelamarDiproses);
        diterimaCard.setValue(pelamarDiterima);
        ditolakCard.setValue(pelamarDitolak);

        // Tampilkan tabel pelamar terbaru (Recent Applicants)
        if (applications == null || applications.isEmpty()) {
            emptyNotice.setVisible(true);
            recentApplicantsGrid.setVisible(false);
        } else {
            emptyNotice.setVisible(false);
            recentApplicantsGrid.setVisible(true);
            List<JobApplication> recentList = applications.stream().limit(5).toList();
            recentApplicantsGrid.setItems(recentList);
        }
    }

    private Span createStatusBadge(JobApplication jobApplication) {
        ApplicationStatus status = jobApplication.getStatus();
        Span badge = new Span(status != null ? status.name() : "-");
        badge.addClassName("status-badge");

        if (status != null) {
            String cssClass = status.name().toLowerCase().replace('_', '-');
            badge.addClassName(cssClass);
        }

        return badge;
    }
}
