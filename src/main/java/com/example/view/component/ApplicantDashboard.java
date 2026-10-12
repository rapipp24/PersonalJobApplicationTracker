package com.example.view.component;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.entity.User;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class ApplicantDashboard extends VerticalLayout {

    // KPI Card Elements
    private final Span totalAppsValue = new Span("0");
    private final Span totalAppsSubtext = new Span("All recorded submissions");

    private final Span inProgressValue = new Span("0");
    private final Span inProgressSubtext = new Span("Across active recruitment stages");

    private final Span offeredValue = new Span("0");
    private final Span offeredSubtext = new Span("Applications with an offer");

    private final Span rejectedValue = new Span("0");
    private final Span rejectedSubtext = new Span("Closed applications");

    // Active Applications Gauge Component
    private final ApplicantApplicationsGauge applicationsGauge = new ApplicantApplicationsGauge();

    // Recent Applications Component
    private final ApplicantRecentApplications recentApplications = new ApplicantRecentApplications();

    public ApplicantDashboard() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("applicant-dashboard");

        createHeaderSection();
        createKpiCardsSection();
        add(applicationsGauge);
        add(recentApplications);
    }

    private void createHeaderSection() {
        Div headerLayout = new Div();
        headerLayout.addClassName("applicant-header-layout");

        H2 title = new H2("Dashboard");
        title.addClassName("applicant-dashboard-title");

        Span subtitle = new Span("Your job search at a glance.");
        subtitle.addClassName("applicant-dashboard-subtitle");

        headerLayout.add(title, subtitle);
        add(headerLayout);
    }

    private void createKpiCardsSection() {
        Div kpiRow = new Div();
        kpiRow.addClassName("applicant-kpi-row");

        // Card 1: Total Applications (clean, no decorative icon)
        Div totalCard = createKpiCard("Total Applications", totalAppsValue, "", totalAppsSubtext);

        // Card 2: In Progress (clean, no decorative icon)
        Div inProgressCard = createKpiCard("In Progress", inProgressValue, "", inProgressSubtext);

        // Card 3: Offered (subtle green accent on number)
        Div offeredCard = createKpiCard("Offered", offeredValue, "offered-color", offeredSubtext);

        // Card 4: Rejected (clean, neutral, no large error icon)
        Div rejectedCard = createKpiCard("Rejected", rejectedValue, "", rejectedSubtext);

        kpiRow.add(totalCard, inProgressCard, offeredCard, rejectedCard);
        add(kpiRow);
    }

    private Div createKpiCard(String label, Span valueSpan, String valueColorClass, Span subtextSpan) {
        Div card = new Div();
        card.addClassName("applicant-kpi-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("applicant-kpi-label");

        valueSpan.addClassName("applicant-kpi-value");
        if (valueColorClass != null && !valueColorClass.isBlank()) {
            valueSpan.addClassName(valueColorClass);
        }

        subtextSpan.addClassName("applicant-kpi-subtext");

        card.add(labelSpan, valueSpan, subtextSpan);
        return card;
    }

    public void updateData(User currentUser, List<JobApplication> applications) {
        int totalApplications = 0;
        int inProgress = 0;
        int offered = 0;
        int rejected = 0;

        int appliedCount = 0;
        int screeningCount = 0;
        int technicalTestCount = 0;
        int interviewCount = 0;

        if (applications != null) {
            totalApplications = applications.size();

            for (JobApplication application : applications) {
                ApplicationStatus status = application.getStatus();

                if (status == ApplicationStatus.APPLIED) {
                    appliedCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.SCREENING) {
                    screeningCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.TECHNICAL_TEST) {
                    technicalTestCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.INTERVIEW) {
                    interviewCount++;
                    inProgress++;
                } else if (status == ApplicationStatus.OFFERED) {
                    offered++;
                } else if (status == ApplicationStatus.REJECTED) {
                    rejected++;
                }
            }
        }

        // 1. Update KPI Cards
        totalAppsValue.setText(String.valueOf(totalApplications));
        inProgressValue.setText(String.valueOf(inProgress));
        offeredValue.setText(String.valueOf(offered));
        rejectedValue.setText(String.valueOf(rejected));

        // 2. Update Active Applications (Gauge & Legend)
        applicationsGauge.updateData(appliedCount, screeningCount, technicalTestCount, interviewCount, inProgress);

        // 3. Update Recent Applications (up to 5, latest first)
        recentApplications.updateData(applications);
    }
}
