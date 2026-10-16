package com.example.view;

import com.example.entity.Company;
import com.example.entity.User;
import com.example.service.CompanyVerificationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Area Admin | Job Tracker")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    private final CompanyVerificationService companyVerificationService;
    private final Grid<Company> pendingGrid;
    private final Paragraph emptyNotice;

    public AdminView(CompanyVerificationService companyVerificationService) {
        this.companyVerificationService = companyVerificationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Verifikasi Perusahaan");
        Paragraph description = new Paragraph("Daftar pendaftaran perusahaan baru yang berstatus PENDING dan membutuhkan verifikasi administrator.");

        emptyNotice = new Paragraph("Tidak ada perusahaan yang menunggu verifikasi saat ini.");
        emptyNotice.getStyle()
                .set("color", "#64748B")
                .set("font-style", "italic")
                .set("margin-top", "16px");

        pendingGrid = new Grid<>(Company.class, false);
        configureGrid();

        add(title, description, emptyNotice, pendingGrid);

        refreshGrid();
    }

    private void configureGrid() {
        pendingGrid.addColumn(Company::getName).setHeader("Nama Perusahaan").setAutoWidth(true);
        pendingGrid.addColumn(company -> formatNullable(company.getIndustry())).setHeader("Industri").setAutoWidth(true);
        pendingGrid.addColumn(company -> formatNullable(company.getLocation())).setHeader("Lokasi").setAutoWidth(true);

        pendingGrid.addComponentColumn(company -> {
            Span badge = new Span("PENDING");
            badge.getStyle()
                    .set("background-color", "#FEF3C7")
                    .set("color", "#92400E")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-weight", "600")
                    .set("font-size", "12px");
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        pendingGrid.addComponentColumn(company -> {
            Button reviewButton = new Button("Review Detail", event -> openDetailDialog(company));
            reviewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return reviewButton;
        }).setHeader("Aksi").setAutoWidth(true);

        pendingGrid.setWidthFull();
    }

    private void openDetailDialog(Company company) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detail Verifikasi Perusahaan");
        dialog.setWidth("640px");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(true);
        dialogContent.setPadding(false);

        // Mengambil data registrant dari service (bukan query repository langsung)
        String registrantName;
        String registrantEmail;
        try {
            User registrant = companyVerificationService.getCompanyRegistrant(company);
            registrantName = registrant.getName();
            registrantEmail = registrant.getEmail();
        } catch (Exception ex) {
            registrantName = "Tidak tersedia (" + ex.getMessage() + ")";
            registrantEmail = "-";
        }

        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        infoLayout.addFormItem(new Span(formatNullable(company.getName())), "Nama Perusahaan");
        infoLayout.addFormItem(new Span(formatNullable(company.getIndustry())), "Industri");
        infoLayout.addFormItem(new Span(formatNullable(company.getLocation())), "Lokasi");
        infoLayout.addFormItem(new Span(formatNullable(company.getWebsite())), "Website");
        infoLayout.addFormItem(new Span(formatNullable(company.getOfficialContact())), "Official Contact");

        H3 registrantSectionTitle = new H3("Informasi Pendaftar");
        registrantSectionTitle.getStyle().set("margin-top", "12px").set("margin-bottom", "4px");

        FormLayout registrantLayout = new FormLayout();
        registrantLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );
        registrantLayout.addFormItem(new Span(formatNullable(registrantName)), "Nama Employer");
        registrantLayout.addFormItem(new Span(formatNullable(registrantEmail)), "Email Employer");

        VerticalLayout descLayout = new VerticalLayout();
        descLayout.setPadding(false);
        descLayout.setSpacing(false);
        Span descLabel = new Span("Deskripsi Perusahaan:");
        descLabel.getStyle().set("font-weight", "600").set("font-size", "13px").set("color", "#475569");
        Paragraph descText = new Paragraph(formatNullable(company.getDescription()));
        descText.getStyle().set("margin-top", "4px");
        descLayout.add(descLabel, descText);

        Hr divider = new Hr();

        TextArea reasonField = new TextArea("Alasan Penolakan (Wajib diisi jika menolak)");
        reasonField.setWidthFull();
        reasonField.setPlaceholder("Masukkan alasan penolakan jika perusahaan tidak memenuhi syarat...");

        dialogContent.add(
                infoLayout,
                descLayout,
                registrantSectionTitle,
                registrantLayout,
                divider,
                reasonField
        );

        dialog.add(dialogContent);

        Button cancelButton = new Button("Batal", event -> dialog.close());

        Button rejectButton = new Button("Reject", event -> {
            String reason = reasonField.getValue();
            if (reason == null || reason.trim().isEmpty()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Alasan penolakan wajib diisi");
                Notification.show("Alasan penolakan wajib diisi untuk menolak perusahaan", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                companyVerificationService.rejectCompany(company.getId(), reason);
                Notification.show("Perusahaan '" + company.getName() + "' telah ditolak.", 3000, Notification.Position.BOTTOM_START);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Gagal menolak perusahaan: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button approveButton = new Button("Approve", event -> {
            try {
                companyVerificationService.approveCompany(company.getId());
                Notification.show("Perusahaan '" + company.getName() + "' berhasil diverifikasi dan disetujui.", 3000, Notification.Position.BOTTOM_START);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Gagal menyetujui perusahaan: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        approveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelButton, rejectButton, approveButton);
        dialog.open();
    }

    private void refreshGrid() {
        List<Company> pendingCompanies = companyVerificationService.getPendingCompanies();
        pendingGrid.setItems(pendingCompanies);

        boolean hasData = !pendingCompanies.isEmpty();
        pendingGrid.setVisible(hasData);
        emptyNotice.setVisible(!hasData);
    }

    private String formatNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}
