package com.example.view;

import com.example.entity.CompanyMembershipStatus;
import com.example.entity.CompanyPermission;
import com.example.entity.CompanyVerificationStatus;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.CompanyMembershipService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "pemberi-lamaran/member-requests", layout = MainLayout.class)
@PageTitle("Member Requests | Job Application Tracker")
@RolesAllowed("PEMBERI_LAMARAN")
public class CompanyMemberRequestsView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final CompanyMembershipService companyMembershipService;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    private final Grid<User> requestsGrid;
    private final Paragraph emptyNotice;

    public CompanyMemberRequestsView(
            CompanyMembershipService companyMembershipService,
            AuthenticationContext authenticationContext,
            UserRepository userRepository) {

        this.companyMembershipService = companyMembershipService;
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Permintaan Bergabung Anggota");
        Paragraph description = new Paragraph("Daftar calon anggota yang ingin bergabung dengan perusahaan Anda.");

        emptyNotice = new Paragraph("Tidak ada permintaan bergabung yang menunggu persetujuan saat ini.");
        emptyNotice.getStyle()
                .set("color", "#64748B")
                .set("font-style", "italic")
                .set("margin-top", "16px");

        requestsGrid = new Grid<>(User.class, false);
        configureGrid();

        add(title, description, emptyNotice, requestsGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String email = authenticationContext.getPrincipalName().orElse(null);
        if (email == null) {
            event.forwardTo(DashboardView.class);
            return;
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            event.forwardTo(DashboardView.class);
            return;
        }

        User currentUser = userOptional.get();

        if (currentUser.getCompany() == null
                || currentUser.getCompany().getVerificationStatus() != CompanyVerificationStatus.VERIFIED
                || currentUser.getMembershipStatus() != CompanyMembershipStatus.ACTIVE
                || currentUser.getCompanyPermission() != CompanyPermission.COMPANY_ADMIN) {
            event.forwardTo(DashboardView.class);
            return;
        }

        refreshGrid();
    }

    private void configureGrid() {
        requestsGrid.addColumn(User::getName)
                .setHeader("Nama")
                .setAutoWidth(true);

        requestsGrid.addColumn(User::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        requestsGrid.addColumn(this::formatRequestDate)
                .setHeader("Tanggal Request")
                .setAutoWidth(true);

        requestsGrid.addComponentColumn(candidate -> {
            Button approveButton = new Button("Approve", event -> handleApprove(candidate));
            approveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

            Button rejectButton = new Button("Reject", event -> handleReject(candidate));
            rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            HorizontalLayout actions = new HorizontalLayout(approveButton, rejectButton);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Aksi").setAutoWidth(true);

        requestsGrid.setWidthFull();
    }

    private String formatRequestDate(User user) {
        if (user == null || user.getMembershipRequestedAt() == null) {
            return "-";
        }
        return user.getMembershipRequestedAt().format(DATE_FORMATTER);
    }

    private void handleApprove(User candidate) {
        try {
            User currentUser = getCurrentUser();
            companyMembershipService.approveMember(candidate.getId(), currentUser);
            Notification.show("Anggota " + candidate.getName() + " berhasil disetujui.", 3000, Notification.Position.BOTTOM_START);
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Gagal menyetujui anggota: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void handleReject(User candidate) {
        try {
            User currentUser = getCurrentUser();
            companyMembershipService.rejectMember(candidate.getId(), currentUser);
            Notification.show("Permintaan bergabung " + candidate.getName() + " ditolak.", 3000, Notification.Position.BOTTOM_START);
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Gagal menolak anggota: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void refreshGrid() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            List<User> pendingMembers = companyMembershipService.getPendingMembers(currentUser);
            requestsGrid.setItems(pendingMembers);
            emptyNotice.setVisible(pendingMembers.isEmpty());
            requestsGrid.setVisible(!pendingMembers.isEmpty());
        } catch (Exception ex) {
            Notification.show("Gagal memuat daftar permintaan anggota: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private User getCurrentUser() {
        return authenticationContext.getPrincipalName()
                .flatMap(userRepository::findByEmail)
                .orElse(null);
    }
}
