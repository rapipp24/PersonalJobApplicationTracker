package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyVerificationStatus;
import com.example.repository.CompanyRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value = "admin/companies", layout = MainLayout.class)
@PageTitle("Companies | Job Application Tracker")
@RolesAllowed("ADMIN")
public class AdminCompaniesView extends VerticalLayout {

    private static final int PAGE_SIZE = 7;

    private final CompanyRepository companyRepository;

    private final TextField searchField = new TextField();
    private final Select<CompanyVerificationStatus> statusSelect = new Select<>();
    private final Span countSpan = new Span();
    private final Grid<Company> grid = new Grid<>(Company.class, false);
    private final Div emptyStateContainer = new Div();
    private final HorizontalLayout paginationContainer = new HorizontalLayout();
    private final Span paginationRangeSpan = new Span();
    private final HorizontalLayout paginationControls = new HorizontalLayout();

    private int currentPage = 0;
    private List<Company> allFilteredCompanies = new ArrayList<>();

    public AdminCompaniesView(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;

        addClassName("view-container");
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        buildView();
        refreshCompanies();
    }

    private void buildView() {
        // 1. PAGE HEADER
        Div headerContainer = new Div();
        headerContainer.setWidthFull();

        H2 title = new H2("Companies");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#0F172A");

        Paragraph subtitle = new Paragraph("View registered companies across the platform");
        subtitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        headerContainer.add(title, subtitle);

        // 2. TOOLBAR
        Div toolbar = new Div();
        toolbar.setWidthFull();
        toolbar.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "16px")
                .set("margin-bottom", "16px")
                .set("flex-wrap", "wrap");

        Div leftFilters = new Div();
        leftFilters.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px")
                .set("flex-wrap", "wrap");

        searchField.setPlaceholder("Search companies...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("320px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(event -> onFilterChanged());

        statusSelect.setEmptySelectionAllowed(true);
        statusSelect.setEmptySelectionCaption("All statuses");
        statusSelect.setItems(CompanyVerificationStatus.values());
        statusSelect.setItemLabelGenerator(status -> {
            if (status == null) {
                return "All statuses";
            }
            switch (status) {
                case VERIFIED:
                    return "Verified";
                case PENDING:
                    return "Pending";
                case REJECTED:
                    return "Rejected";
                default:
                    return status.name();
            }
        });
        statusSelect.setWidth("140px");
        statusSelect.addValueChangeListener(event -> onFilterChanged());

        leftFilters.add(searchField, statusSelect);

        countSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("font-weight", "500");

        toolbar.add(leftFilters, countSpan);

        // 3. TABLE CONTAINER & GRID
        Div tableContainer = new Div();
        tableContainer.setWidthFull();
        tableContainer.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "10px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.04)")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden");

        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setWidthFull();
        grid.getStyle().set("--vaadin-grid-cell-padding", "12px 16px");

        // Column 1: COMPANY (Name + Website if present)
        grid.addComponentColumn(company -> {
            Div compCell = new Div();
            compCell.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "2px")
                    .set("line-height", "1.25");

            Span nameSpan = new Span(formatNullable(company.getName()));
            nameSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "600")
                    .set("color", "#0F172A");
            compCell.add(nameSpan);

            String website = company.getWebsite();
            if (website != null && !website.isBlank()) {
                Span webSpan = new Span(website.trim());
                webSpan.getStyle()
                        .set("font-size", "12px")
                        .set("color", "#64748B");
                compCell.add(webSpan);
            }

            return compCell;
        }).setHeader(createTableHeader("COMPANY")).setWidth("240px").setFlexGrow(2);

        // Column 2: INDUSTRY
        grid.addComponentColumn(company -> {
            Span indSpan = new Span(formatNullable(company.getIndustry()));
            indSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return indSpan;
        }).setHeader(createTableHeader("INDUSTRY")).setWidth("170px").setFlexGrow(1);

        // Column 3: LOCATION
        grid.addComponentColumn(company -> {
            Span locSpan = new Span(formatNullable(company.getLocation()));
            locSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return locSpan;
        }).setHeader(createTableHeader("LOCATION")).setWidth("160px").setFlexGrow(1);

        // Column 4: OFFICIAL CONTACT
        grid.addComponentColumn(company -> {
            Span contactSpan = new Span(formatNullable(company.getOfficialContact()));
            contactSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return contactSpan;
        }).setHeader(createTableHeader("OFFICIAL CONTACT")).setWidth("210px").setFlexGrow(1);

        // Column 5: VERIFICATION STATUS
        grid.addComponentColumn(company -> createStatusBadge(company.getVerificationStatus()))
                .setHeader(createTableHeader("VERIFICATION STATUS"))
                .setWidth("205px")
                .setFlexGrow(0);

        // 4. EMPTY STATE
        emptyStateContainer.setWidthFull();
        emptyStateContainer.getStyle()
                .set("padding", "48px 24px")
                .set("text-align", "center")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "4px");

        Span emptyTitle = new Span("No companies found.");
        emptyTitle.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#0F172A");

        Span emptyDesc = new Span("Try adjusting your search or status filter.");
        emptyDesc.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B");

        emptyStateContainer.add(emptyTitle, emptyDesc);

        // 5. PAGINATION CONTAINER
        paginationContainer.setWidthFull();
        paginationContainer.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        paginationContainer.getStyle()
                .set("padding", "14px 20px")
                .set("border-top", "1px solid #E2E8F0")
                .set("box-sizing", "border-box")
                .set("justify-content", "space-between");

        paginationRangeSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("font-weight", "400");

        paginationControls.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        paginationControls.getStyle()
                .set("gap", "6px")
                .set("align-items", "center");

        paginationContainer.add(paginationRangeSpan, paginationControls);

        tableContainer.add(grid, emptyStateContainer, paginationContainer);

        add(headerContainer, toolbar, tableContainer);
    }

    private void onFilterChanged() {
        currentPage = 0;
        refreshCompanies();
    }

    private void refreshCompanies() {
        String keyword = searchField.getValue() != null ? searchField.getValue().trim() : "";
        CompanyVerificationStatus selectedStatus = statusSelect.getValue();

        // 4 condition branches
        if (keyword.isBlank() && selectedStatus == null) {
            allFilteredCompanies = companyRepository.findAll();
        } else if (!keyword.isBlank() && selectedStatus == null) {
            allFilteredCompanies = companyRepository.findByNameContainingIgnoreCase(keyword);
        } else if (keyword.isBlank() && selectedStatus != null) {
            allFilteredCompanies = companyRepository.findByVerificationStatus(selectedStatus);
        } else {
            allFilteredCompanies = companyRepository.findByNameContainingIgnoreCaseAndVerificationStatus(keyword, selectedStatus);
        }

        int totalResults = allFilteredCompanies.size();

        // Update count in toolbar
        if (keyword.isBlank() && selectedStatus == null) {
            countSpan.setText(totalResults + " registered companies");
        } else {
            countSpan.setText(totalResults + " " + (totalResults == 1 ? "company found" : "companies found"));
        }

        // Empty state check
        if (totalResults == 0) {
            grid.setVisible(false);
            emptyStateContainer.setVisible(true);
            paginationContainer.setVisible(false);
            return;
        }

        grid.setVisible(true);
        emptyStateContainer.setVisible(false);

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) totalResults / PAGE_SIZE);
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalResults);
        List<Company> pageItems = allFilteredCompanies.subList(fromIndex, toIndex);
        grid.setItems(pageItems);

        // Conditional pagination visibility
        if (totalResults <= PAGE_SIZE) {
            paginationContainer.setVisible(false);
        } else {
            paginationContainer.setVisible(true);
            paginationRangeSpan.setText("Showing " + (fromIndex + 1) + " to " + toIndex + " of " + totalResults + " companies");
            buildPaginationControls(totalPages);
        }
    }

    private void renderCurrentPageSubset() {
        int totalResults = allFilteredCompanies.size();
        int totalPages = (int) Math.ceil((double) totalResults / PAGE_SIZE);

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalResults);
        List<Company> pageItems = allFilteredCompanies.subList(fromIndex, toIndex);
        grid.setItems(pageItems);

        paginationRangeSpan.setText("Showing " + (fromIndex + 1) + " to " + toIndex + " of " + totalResults + " companies");
        buildPaginationControls(totalPages);
    }

    private void buildPaginationControls(int totalPages) {
        paginationControls.removeAll();

        // Previous Button
        Button prevBtn = new Button("< Previous", event -> {
            if (currentPage > 0) {
                currentPage--;
                renderCurrentPageSubset();
            }
        });
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        prevBtn.getStyle()
                .set("font-size", "13px")
                .set("color", currentPage == 0 ? "#94A3B8" : "#475569")
                .set("cursor", currentPage == 0 ? "default" : "pointer");
        prevBtn.setEnabled(currentPage > 0);
        paginationControls.add(prevBtn);

        // Page numbers
        if (totalPages <= 7) {
            for (int i = 0; i < totalPages; i++) {
                paginationControls.add(createPageNumberButton(i));
            }
        } else {
            if (currentPage < 3) {
                for (int i = 0; i <= 3; i++) {
                    paginationControls.add(createPageNumberButton(i));
                }
                paginationControls.add(createEllipsis());
                paginationControls.add(createPageNumberButton(totalPages - 1));
            } else if (currentPage >= totalPages - 3) {
                paginationControls.add(createPageNumberButton(0));
                paginationControls.add(createEllipsis());
                for (int i = totalPages - 4; i < totalPages; i++) {
                    paginationControls.add(createPageNumberButton(i));
                }
            } else {
                paginationControls.add(createPageNumberButton(0));
                paginationControls.add(createEllipsis());
                paginationControls.add(createPageNumberButton(currentPage - 1));
                paginationControls.add(createPageNumberButton(currentPage));
                paginationControls.add(createPageNumberButton(currentPage + 1));
                paginationControls.add(createEllipsis());
                paginationControls.add(createPageNumberButton(totalPages - 1));
            }
        }

        // Next Button
        Button nextBtn = new Button("Next >", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                renderCurrentPageSubset();
            }
        });
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        nextBtn.getStyle()
                .set("font-size", "13px")
                .set("color", currentPage == totalPages - 1 ? "#94A3B8" : "#475569")
                .set("cursor", currentPage == totalPages - 1 ? "default" : "pointer");
        nextBtn.setEnabled(currentPage < totalPages - 1);
        paginationControls.add(nextBtn);
    }

    private Span createEllipsis() {
        Span ellipsis = new Span("...");
        ellipsis.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("padding", "0 4px");
        return ellipsis;
    }

    private Button createPageNumberButton(int pageIndex) {
        boolean isActive = (pageIndex == currentPage);
        Button btn = new Button(String.valueOf(pageIndex + 1), event -> {
            if (currentPage != pageIndex) {
                currentPage = pageIndex;
                renderCurrentPageSubset();
            }
        });
        btn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btn.getStyle()
                .set("min-width", "32px")
                .set("height", "32px")
                .set("padding", "0 6px")
                .set("font-size", "13px")
                .set("font-weight", isActive ? "600" : "500")
                .set("border-radius", "6px")
                .set("cursor", isActive ? "default" : "pointer");

        if (isActive) {
            btn.getStyle()
                    .set("background-color", "#1E2858")
                    .set("color", "#FFFFFF")
                    .set("border", "none");
        } else {
            btn.getStyle()
                    .set("background-color", "transparent")
                    .set("color", "#334155")
                    .set("border", "none");
        }
        return btn;
    }

    private Span createTableHeader(String title) {
        Span header = new Span(title);
        header.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("color", "#64748B")
                .set("letter-spacing", "0.04em")
                .set("white-space", "nowrap");
        return header;
    }

    private Span createStatusBadge(CompanyVerificationStatus status) {
        Span badge = new Span();
        badge.getStyle()
                .set("padding", "3px 8px")
                .set("border-radius", "4px")
                .set("font-weight", "600")
                .set("font-size", "11px")
                .set("letter-spacing", "0.04em")
                .set("display", "inline-block");

        if (status == null) {
            badge.setText("-");
            badge.getStyle()
                    .set("background-color", "transparent")
                    .set("color", "#64748B");
        } else if (status == CompanyVerificationStatus.VERIFIED) {
            badge.setText("VERIFIED");
            badge.getStyle()
                    .set("background-color", "#DEF7EC")
                    .set("color", "#03543F");
        } else if (status == CompanyVerificationStatus.PENDING) {
            badge.setText("PENDING");
            badge.getStyle()
                    .set("background-color", "#FEF3C7")
                    .set("color", "#92400E");
        } else if (status == CompanyVerificationStatus.REJECTED) {
            badge.setText("REJECTED");
            badge.getStyle()
                    .set("background-color", "#FEE2E2")
                    .set("color", "#991B1B");
        } else {
            badge.setText(status.name());
            badge.getStyle()
                    .set("background-color", "#F1F5F9")
                    .set("color", "#475569");
        }
        return badge;
    }

    private String formatNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}
