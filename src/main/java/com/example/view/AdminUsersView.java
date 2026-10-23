package com.example.view;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.CompanyRepository;
import com.example.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Locale;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Users | Job Application Tracker")
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private static final int PAGE_SIZE = 8;

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // Filter states
    private int currentPage = 0;
    private Role selectedRole = null; // null = All Users, PELAMAR = Applicants, PEMBERI_LAMARAN = Employers
    private Long selectedCompanyId = null;
    private String selectedCompanyName = null;
    private CompanyMembershipStatus selectedMembershipStatus = null;

    // UI Components
    private final Button allUsersTab = new Button("All Users");
    private final Button applicantsTab = new Button("Applicants");
    private final Button employersTab = new Button("Employers");

    private final TextField searchField = new TextField();
    private final Button filterBtn = new Button("Filters");
    private final Span countSpan = new Span();

    private final HorizontalLayout chipsLayout = new HorizontalLayout();
    private final Grid<User> grid = new Grid<>(User.class, false);
    private final Div emptyStateContainer = new Div();

    private final HorizontalLayout paginationContainer = new HorizontalLayout();
    private final Span paginationRangeSpan = new Span();
    private final HorizontalLayout paginationControls = new HorizontalLayout();

    // Filter Dialog components
    private final Dialog filterDialog = new Dialog();
    private final Select<Company> dialogCompanySelect = new Select<>();
    private final Select<CompanyMembershipStatus> dialogStatusSelect = new Select<>();

    public AdminUsersView(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;

        addClassName("view-container");
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        buildView();
        buildFilterDialog();
        refreshUsers();
    }

    private void buildView() {
        // 1. PAGE HEADER
        Div headerContainer = new Div();
        headerContainer.setWidthFull();

        H2 title = new H2("Users");
        title.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "24px")
                .set("font-weight", "700")
                .set("color", "#0F172A");

        Paragraph subtitle = new Paragraph("View registered users across the platform");
        subtitle.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "14px")
                .set("color", "#64748B");

        headerContainer.add(title, subtitle);

        // 2. QUICK ROLE TABS
        HorizontalLayout roleTabsLayout = new HorizontalLayout();
        roleTabsLayout.setWidthFull();
        roleTabsLayout.setSpacing(true);
        roleTabsLayout.getStyle()
                .set("border-bottom", "1px solid #E2E8F0")
                .set("margin-bottom", "16px")
                .set("gap", "12px");

        configureRoleTab(allUsersTab, null);
        configureRoleTab(applicantsTab, Role.PELAMAR);
        configureRoleTab(employersTab, Role.PEMBERI_LAMARAN);

        updateRoleTabStyles();
        roleTabsLayout.add(allUsersTab, applicantsTab, employersTab);

        // 3. TOOLBAR
        Div toolbar = new Div();
        toolbar.setWidthFull();
        toolbar.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "12px")
                .set("margin-bottom", "12px")
                .set("flex-wrap", "wrap");

        Div leftControls = new Div();
        leftControls.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("flex-wrap", "wrap");

        searchField.setPlaceholder("Search users...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(event -> {
            currentPage = 0;
            refreshUsers();
        });

        filterBtn.setIcon(VaadinIcon.FILTER.create());
        filterBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        filterBtn.getStyle()
                .set("font-size", "13px")
                .set("cursor", "pointer");
        filterBtn.addClickListener(event -> openFilterDialog());

        leftControls.add(searchField, filterBtn);

        countSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B")
                .set("font-weight", "500");

        toolbar.add(leftControls, countSpan);

        // 4. ACTIVE CHIPS LAYOUT
        chipsLayout.setWidthFull();
        chipsLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        chipsLayout.getStyle()
                .set("gap", "8px")
                .set("margin-bottom", "12px")
                .set("flex-wrap", "wrap");
        chipsLayout.setVisible(false);

        // 5. TABLE CONTAINER & GRID
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

        // Column 1: USER (Name + Email)
        grid.addComponentColumn(user -> {
            Div userCell = new Div();
            userCell.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "2px")
                    .set("line-height", "1.25");

            Span nameSpan = new Span(formatNullable(user.getName()));
            nameSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "600")
                    .set("color", "#0F172A");
            userCell.add(nameSpan);

            String email = user.getEmail();
            if (email != null && !email.isBlank()) {
                Span emailSpan = new Span(email.trim());
                emailSpan.getStyle()
                        .set("font-size", "12px")
                        .set("color", "#64748B");
                userCell.add(emailSpan);
            }

            return userCell;
        }).setHeader(createTableHeader("USER")).setWidth("250px").setFlexGrow(2);

        // Column 2: ROLE
        grid.addComponentColumn(user -> {
            Span roleSpan = new Span(formatRole(user.getRole()));
            roleSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return roleSpan;
        }).setHeader(createTableHeader("ROLE")).setWidth("130px").setFlexGrow(0);

        // Column 3: COMPANY
        grid.addComponentColumn(user -> {
            String companyName = user.getCompany() != null ? user.getCompany().getName() : "-";
            Span compSpan = new Span(formatNullable(companyName));
            compSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return compSpan;
        }).setHeader(createTableHeader("COMPANY")).setWidth("200px").setFlexGrow(1);

        // Column 4: PERMISSION
        grid.addComponentColumn(user -> {
            Span permSpan = new Span(formatPermission(user.getCompanyPermission()));
            permSpan.getStyle()
                    .set("font-size", "13.5px")
                    .set("font-weight", "400")
                    .set("color", "#475569");
            return permSpan;
        }).setHeader(createTableHeader("PERMISSION")).setWidth("150px").setFlexGrow(0);

        // Column 5: MEMBERSHIP STATUS
        grid.addComponentColumn(user -> createMembershipStatusBadge(user.getMembershipStatus()))
                .setHeader(createTableHeader("MEMBERSHIP STATUS"))
                .setWidth("190px")
                .setFlexGrow(0);

        // 6. EMPTY STATE
        emptyStateContainer.setWidthFull();
        emptyStateContainer.getStyle()
                .set("padding", "48px 24px")
                .set("text-align", "center")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "4px");

        Span emptyTitle = new Span("No users found.");
        emptyTitle.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "600")
                .set("color", "#0F172A");

        Span emptyDesc = new Span("Try adjusting your search or filters.");
        emptyDesc.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748B");

        emptyStateContainer.add(emptyTitle, emptyDesc);

        // 7. PAGINATION CONTAINER
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

        add(headerContainer, roleTabsLayout, toolbar, chipsLayout, tableContainer);
    }

    private void configureRoleTab(Button tab, Role role) {
        tab.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tab.addClickListener(event -> {
            if (this.selectedRole != role) {
                this.selectedRole = role;
                this.currentPage = 0;
                updateRoleTabStyles();
                refreshUsers();
            }
        });
    }

    private void updateRoleTabStyles() {
        applyTabStyle(allUsersTab, selectedRole == null);
        applyTabStyle(applicantsTab, selectedRole == Role.PELAMAR);
        applyTabStyle(employersTab, selectedRole == Role.PEMBERI_LAMARAN);
    }

    private void applyTabStyle(Button tab, boolean isActive) {
        tab.getStyle()
                .set("font-size", "13.5px")
                .set("border-radius", "0")
                .set("padding", "8px 12px")
                .set("cursor", "pointer");

        if (isActive) {
            tab.getStyle()
                    .set("color", "#1E2858")
                    .set("font-weight", "600")
                    .set("border-bottom", "2px solid #1E2858");
        } else {
            tab.getStyle()
                    .set("color", "#64748B")
                    .set("font-weight", "500")
                    .set("border-bottom", "2px solid transparent");
        }
    }

    private void buildFilterDialog() {
        filterDialog.setHeaderTitle("Filters");
        filterDialog.setWidth("380px");
        filterDialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);
        dialogContent.setWidthFull();

        // Company filter select
        dialogCompanySelect.setLabel("Company");
        dialogCompanySelect.setWidthFull();
        dialogCompanySelect.setEmptySelectionAllowed(true);
        dialogCompanySelect.setEmptySelectionCaption("All companies");
        List<Company> companies = companyRepository.findAll(Sort.by("name").ascending());
        dialogCompanySelect.setItems(companies);
        dialogCompanySelect.setItemLabelGenerator(comp -> comp != null ? comp.getName() : "All companies");

        // Status filter select
        dialogStatusSelect.setLabel("Membership Status");
        dialogStatusSelect.setWidthFull();
        dialogStatusSelect.setEmptySelectionAllowed(true);
        dialogStatusSelect.setEmptySelectionCaption("All statuses");
        dialogStatusSelect.setItems(CompanyMembershipStatus.values());
        dialogStatusSelect.setItemLabelGenerator(status -> {
            if (status == null) {
                return "All statuses";
            }
            switch (status) {
                case ACTIVE:
                    return "Active";
                case PENDING:
                    return "Pending";
                case REJECTED:
                    return "Rejected";
                default:
                    return status.name();
            }
        });

        dialogContent.add(dialogCompanySelect, dialogStatusSelect);
        filterDialog.add(dialogContent);

        // Footer buttons
        Button clearBtn = new Button("Clear filters", event -> {
            dialogCompanySelect.setValue(null);
            dialogStatusSelect.setValue(null);
        });
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button applyBtn = new Button("Apply", event -> {
            Company chosenCompany = dialogCompanySelect.getValue();
            this.selectedCompanyId = chosenCompany != null ? chosenCompany.getId() : null;
            this.selectedCompanyName = chosenCompany != null ? chosenCompany.getName() : null;
            this.selectedMembershipStatus = dialogStatusSelect.getValue();

            this.currentPage = 0;
            filterDialog.close();
            refreshUsers();
        });
        applyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filterDialog.getFooter().add(clearBtn, applyBtn);
    }

    private void openFilterDialog() {
        // Pre-populate with current values
        if (selectedCompanyId != null) {
            companyRepository.findById(selectedCompanyId).ifPresent(dialogCompanySelect::setValue);
        } else {
            dialogCompanySelect.setValue(null);
        }
        dialogStatusSelect.setValue(selectedMembershipStatus);

        filterDialog.open();
    }

    private void refreshUsers() {
        String rawKeyword = searchField.getValue();
        String keyword = rawKeyword == null
                ? ""
                : rawKeyword.trim().toLowerCase(Locale.ROOT);

        PageRequest pageRequest = PageRequest.of(currentPage, PAGE_SIZE, Sort.by("id").descending());

        // Single database query with server-side pagination
        Page<User> userPage = userRepository.findForAdminMonitoring(
                selectedRole,
                keyword,
                selectedCompanyId,
                selectedMembershipStatus,
                pageRequest
        );

        long totalResults = userPage.getTotalElements();
        int totalPages = userPage.getTotalPages();

        // Update active filter badge & chips
        updateFilterBadgeAndChips();

        // Update count text
        boolean isFiltered = (selectedRole != null)
                || (!keyword.isEmpty())
                || (selectedCompanyId != null)
                || (selectedMembershipStatus != null);

        if (!isFiltered) {
            countSpan.setText(totalResults + " registered users");
        } else {
            countSpan.setText(totalResults + " " + (totalResults == 1 ? "user found" : "users found"));
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
        grid.setItems(userPage.getContent());

        // Conditional pagination visibility (totalPages > 1)
        if (totalPages <= 1) {
            paginationContainer.setVisible(false);
        } else {
            paginationContainer.setVisible(true);
            int fromIndex = currentPage * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, (int) totalResults);
            paginationRangeSpan.setText("Showing " + (fromIndex + 1) + " to " + toIndex + " of " + totalResults + " users");
            buildPaginationControls(totalPages);
        }
    }

    private void updateFilterBadgeAndChips() {
        int filterCount = 0;
        if (selectedCompanyId != null) {
            filterCount++;
        }
        if (selectedMembershipStatus != null) {
            filterCount++;
        }

        if (filterCount > 0) {
            filterBtn.setText("Filters (" + filterCount + ")");
        } else {
            filterBtn.setText("Filters");
        }

        // Active Chips
        chipsLayout.removeAll();
        if (filterCount > 0) {
            chipsLayout.setVisible(true);

            Span label = new Span("Active:");
            label.getStyle()
                    .set("font-size", "12px")
                    .set("font-weight", "600")
                    .set("color", "#64748B");
            chipsLayout.add(label);

            if (selectedCompanyId != null) {
                chipsLayout.add(createFilterChip("Company: " + (selectedCompanyName != null ? selectedCompanyName : "Selected"), () -> {
                    selectedCompanyId = null;
                    selectedCompanyName = null;
                    currentPage = 0;
                    refreshUsers();
                }));
            }

            if (selectedMembershipStatus != null) {
                chipsLayout.add(createFilterChip("Status: " + formatMembershipStatus(selectedMembershipStatus), () -> {
                    selectedMembershipStatus = null;
                    currentPage = 0;
                    refreshUsers();
                }));
            }

            Button clearAllBtn = new Button("Clear all", event -> {
                selectedCompanyId = null;
                selectedCompanyName = null;
                selectedMembershipStatus = null;
                currentPage = 0;
                refreshUsers();
            });
            clearAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            clearAllBtn.getStyle()
                    .set("font-size", "12px")
                    .set("color", "#2563EB")
                    .set("cursor", "pointer");
            chipsLayout.add(clearAllBtn);
        } else {
            chipsLayout.setVisible(false);
        }
    }

    private Div createFilterChip(String text, Runnable onRemove) {
        Div chip = new Div();
        chip.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "6px")
                .set("padding", "3px 8px")
                .set("background-color", "#F1F5F9")
                .set("border-radius", "14px")
                .set("font-size", "12px")
                .set("color", "#334155");

        Span chipText = new Span(text);
        Span removeIcon = new Span("×");
        removeIcon.getStyle()
                .set("cursor", "pointer")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("line-height", "1")
                .set("color", "#64748B");
        removeIcon.addClickListener(e -> onRemove.run());

        chip.add(chipText, removeIcon);
        return chip;
    }

    private void buildPaginationControls(int totalPages) {
        paginationControls.removeAll();

        // Previous Button
        Button prevBtn = new Button("< Previous", event -> {
            if (currentPage > 0) {
                currentPage--;
                refreshUsers();
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
                refreshUsers();
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
                refreshUsers();
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

    // Neutral badge styling (No green, amber, or red)
    private Span createMembershipStatusBadge(CompanyMembershipStatus status) {
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
        } else if (status == CompanyMembershipStatus.ACTIVE) {
            badge.setText("ACTIVE");
            badge.getStyle()
                    .set("background-color", "#EEF2F6")
                    .set("color", "#1E2858");
        } else if (status == CompanyMembershipStatus.PENDING) {
            badge.setText("PENDING");
            badge.getStyle()
                    .set("background-color", "#F1F5F9")
                    .set("color", "#475569");
        } else if (status == CompanyMembershipStatus.REJECTED) {
            badge.setText("REJECTED");
            badge.getStyle()
                    .set("background-color", "#FFFFFF")
                    .set("border", "1px solid #CBD5E1")
                    .set("color", "#334155");
        }
        return badge;
    }

    private String formatRole(Role role) {
        if (role == null) {
            return "-";
        }
        switch (role) {
            case PELAMAR:
                return "Applicant";
            case PEMBERI_LAMARAN:
                return "Employer";
            case ADMIN:
                return "Admin";
            default:
                return role.name();
        }
    }

    private String formatPermission(CompanyPermission permission) {
        if (permission == null) {
            return "-";
        }
        switch (permission) {
            case COMPANY_ADMIN:
                return "Company Admin";
            case MEMBER:
                return "Member";
            default:
                return permission.name();
        }
    }

    private String formatMembershipStatus(CompanyMembershipStatus status) {
        if (status == null) {
            return "All statuses";
        }
        switch (status) {
            case ACTIVE:
                return "Active";
            case PENDING:
                return "Pending";
            case REJECTED:
                return "Rejected";
            default:
                return status.name();
        }
    }

    private String formatNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}
