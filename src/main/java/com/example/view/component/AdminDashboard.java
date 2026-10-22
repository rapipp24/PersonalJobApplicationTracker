package com.example.view.component;

import com.example.entity.Company;
import com.example.entity.JobApplication;
import com.example.entity.User;
import com.example.service.CompanyVerificationService;
import com.example.view.AdminView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;

import java.util.List;

public class AdminDashboard extends VerticalLayout {

    public AdminDashboard() {
        setWidthFull();
        setSpacing(true);
        setPadding(false);
        attachResponsiveStyles();
    }

    private void attachResponsiveStyles() {
        Element styleEl = new Element("style");
        styleEl.setText(
                ".admin-metrics-grid {" +
                "  display: grid;" +
                "  grid-template-columns: repeat(3, minmax(0, 1fr));" +
                "  gap: 16px;" +
                "  width: 100%;" +
                "  box-sizing: border-box;" +
                "}" +
                "@media (max-width: 900px) {" +
                "  .admin-metrics-grid {" +
                "    grid-template-columns: repeat(2, minmax(0, 1fr));" +
                "  }" +
                "}" +
                "@media (max-width: 600px) {" +
                "  .admin-metrics-grid {" +
                "    grid-template-columns: 1fr;" +
                "  }" +
                "}"
        );
        getElement().appendChild(styleEl);
    }

    public void updateData(
            long platformUsers,
            long registeredCompanies,
            long pendingVerification,
            long totalJobPostings,
            long activeJobPostings,
            long totalApplications,
            List<Company> pendingCompanies,
            CompanyVerificationService companyVerificationService
    ) {
        removeAll();
        attachResponsiveStyles();

        // 1. PAGE HEADER
        Div headerContainer = new Div();
        headerContainer.setWidthFull();

        H2 title = new H2("Platform Overview");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#0F172A");

        Paragraph subtitle = new Paragraph("Monitor users, companies, job postings, and applications across the platform.");
        subtitle.getStyle()
                .set("margin", "0 0 22px 0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        headerContainer.add(title, subtitle);

        // 2. METRIC CARDS (3 columns x 2 rows on desktop)
        Div metricsGrid = new Div();
        metricsGrid.addClassName("admin-metrics-grid");
        metricsGrid.setWidthFull();
        metricsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, minmax(0, 1fr))")
                .set("gap", "16px")
                .set("margin-bottom", "26px");

        // Row 1
        metricsGrid.add(
                createMetricCard("Platform Users", platformUsers, false),
                createMetricCard("Registered Companies", registeredCompanies, false),
                createMetricCard("Pending Verification", pendingVerification, true)
        );

        // Row 2
        metricsGrid.add(
                createMetricCard("Total Job Postings", totalJobPostings, false),
                createMetricCard("Active Job Postings", activeJobPostings, false),
                createMetricCard("Total Applications", totalApplications, false)
        );

        // 3. PENDING COMPANY VERIFICATION PREVIEW
        Div previewCard = new Div();
        previewCard.setWidthFull();
        previewCard.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("padding", "22px 24px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.04)")
                .set("box-sizing", "border-box");

        H3 sectionTitle = new H3("Pending Company Verification");
        sectionTitle.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "18px")
                .set("font-weight", "700")
                .set("color", "#0F172A");

        Paragraph sectionSubtitle = new Paragraph("Companies waiting for platform review");
        sectionSubtitle.getStyle()
                .set("margin", "0 0 18px 0")
                .set("font-size", "13.5px")
                .set("color", "#64748B");

        previewCard.add(sectionTitle, sectionSubtitle);

        if (pendingCompanies == null || pendingCompanies.isEmpty()) {
            Paragraph emptyNotice = new Paragraph("No pending company verification.");
            emptyNotice.getStyle()
                    .set("color", "#64748B")
                    .set("font-size", "14px")
                    .set("margin", "0");
            previewCard.add(emptyNotice);
        } else {
            List<Company> previewList = pendingCompanies.size() > 5
                    ? pendingCompanies.subList(0, 5)
                    : pendingCompanies;

            Grid<Company> grid = new Grid<>(Company.class, false);
            grid.setAllRowsVisible(true);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.setWidthFull();
            grid.getStyle().set("--vaadin-grid-cell-padding", "10px 14px");

            grid.addComponentColumn(company -> {
                Span name = new Span(formatNullable(company.getName()));
                name.getStyle()
                        .set("font-size", "13.5px")
                        .set("font-weight", "600")
                        .set("color", "#0F172A");
                return name;
            }).setHeader(createTableHeader("COMPANY")).setWidth("220px").setFlexGrow(2);

            grid.addComponentColumn(company -> {
                Span industry = new Span(formatNullable(company.getIndustry()));
                industry.getStyle()
                        .set("font-size", "13.5px")
                        .set("font-weight", "400")
                        .set("color", "#475569");
                return industry;
            }).setHeader(createTableHeader("INDUSTRY")).setWidth("160px").setFlexGrow(1);

            grid.addComponentColumn(company -> {
                Span location = new Span(formatNullable(company.getLocation()));
                location.getStyle()
                        .set("font-size", "13.5px")
                        .set("font-weight", "400")
                        .set("color", "#475569");
                return location;
            }).setHeader(createTableHeader("LOCATION")).setWidth("150px").setFlexGrow(1);

            grid.addComponentColumn(company -> {
                String registrantName = "-";
                String registrantEmail = "";
                if (companyVerificationService != null) {
                    try {
                        User registrant = companyVerificationService.getCompanyRegistrant(company);
                        if (registrant != null) {
                            registrantName = registrant.getName() != null && !registrant.getName().isBlank()
                                    ? registrant.getName().trim()
                                    : "-";
                            registrantEmail = registrant.getEmail() != null
                                    ? registrant.getEmail().trim()
                                    : "";
                        }
                    } catch (Exception ignored) {
                    }
                }

                Div submittedLayout = new Div();
                submittedLayout.getStyle()
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("gap", "2px")
                        .set("line-height", "1.25");

                Span nameSpan = new Span(registrantName);
                nameSpan.getStyle()
                        .set("font-size", "13.5px")
                        .set("font-weight", "500")
                        .set("color", "#0F172A");
                submittedLayout.add(nameSpan);

                if (!registrantEmail.isBlank()) {
                    Span emailSpan = new Span(registrantEmail);
                    emailSpan.getStyle()
                            .set("font-size", "12px")
                            .set("font-weight", "400")
                            .set("color", "#64748B");
                    submittedLayout.add(emailSpan);
                }

                return submittedLayout;
            }).setHeader(createTableHeader("SUBMITTED BY")).setWidth("220px").setFlexGrow(2);

            grid.addComponentColumn(company -> {
                Span badge = new Span("PENDING");
                badge.getStyle()
                        .set("background-color", "#FEF3C7")
                        .set("color", "#92400E")
                        .set("padding", "3px 8px")
                        .set("border-radius", "4px")
                        .set("font-weight", "600")
                        .set("font-size", "11px")
                        .set("letter-spacing", "0.04em")
                        .set("display", "inline-block");
                return badge;
            }).setHeader(createTableHeader("STATUS")).setWidth("100px").setFlexGrow(0);

            grid.addComponentColumn(company -> {
                Button reviewButton = new Button("Review", event -> UI.getCurrent().navigate(AdminView.class));
                reviewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                reviewButton.getStyle()
                        .set("border", "1px solid #CBD5E1")
                        .set("background-color", "#FFFFFF")
                        .set("color", "#0F172A")
                        .set("font-size", "12px")
                        .set("font-weight", "500")
                        .set("padding", "2px 12px")
                        .set("height", "28px")
                        .set("border-radius", "6px")
                        .set("cursor", "pointer");
                return reviewButton;
            }).setHeader(createTableHeader("ACTION")).setWidth("100px").setFlexGrow(0);

            grid.setItems(previewList);
            previewCard.add(grid);
        }

        add(headerContainer, metricsGrid, previewCard);
    }

    @Deprecated
    public void updateData(List<JobApplication> data) {
        // Fallback for backwards compatibility if needed
    }

    private Span createTableHeader(String title) {
        Span header = new Span(title);
        header.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("color", "#64748B")
                .set("letter-spacing", "0.04em");
        return header;
    }

    private Div createMetricCard(String label, long value, boolean isAmber) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("padding", "18px 22px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.04)")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "6px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "13.5px")
                .set("font-weight", "500")
                .set("color", "#64748B");

        Span valueSpan = new Span(String.format("%,d", value));
        valueSpan.getStyle()
                .set("font-size", "30px")
                .set("font-weight", "700")
                .set("line-height", "1.15")
                .set("color", isAmber ? "#D97706" : "#0F172A");

        card.add(labelSpan, valueSpan);
        return card;
    }

    private String formatNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}
