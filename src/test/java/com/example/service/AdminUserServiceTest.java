package com.example.service;

import com.example.entity.Company;
import com.example.entity.CompanyMembershipStatus;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        adminUserService = new AdminUserService(userRepository, passwordEncoder);
    }

    @Test
    void resetPassword_berhasil_untuk_applicant() {
        User applicant = new User();
        applicant.setId(1L);
        applicant.setName("Budi Pelamar");
        applicant.setEmail("budi@example.com");
        applicant.setRole(Role.PELAMAR);
        applicant.setPassword("oldEncodedHash");

        when(userRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedHash123");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserService.resetPassword(1L, "newPassword123");

        verify(passwordEncoder).encode("newPassword123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals("newEncodedHash123", captor.getValue().getPassword());
    }

    @Test
    void resetPassword_berhasil_untuk_employer_active() {
        User employer = new User();
        employer.setId(2L);
        employer.setName("Hendra Kusuma");
        employer.setEmail("hendra@muarabuana.co.id");
        employer.setRole(Role.PEMBERI_LAMARAN);
        employer.setMembershipStatus(CompanyMembershipStatus.ACTIVE);
        employer.setPassword("oldEncodedHash");

        when(userRepository.findById(2L)).thenReturn(Optional.of(employer));
        when(passwordEncoder.encode("securePass888")).thenReturn("newHash888");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserService.resetPassword(2L, "securePass888");

        verify(passwordEncoder).encode("securePass888");
        verify(userRepository).save(employer);
        assertEquals("newHash888", employer.getPassword());
    }

    @Test
    void resetPassword_berhasil_untuk_employer_rejected() {
        User employerRejected = new User();
        employerRejected.setId(3L);
        employerRejected.setName("Rian Pratama");
        employerRejected.setEmail("rian@muarabuana.co.id");
        employerRejected.setRole(Role.PEMBERI_LAMARAN);
        employerRejected.setMembershipStatus(CompanyMembershipStatus.REJECTED);
        employerRejected.setPassword("oldHash");

        when(userRepository.findById(3L)).thenReturn(Optional.of(employerRejected));
        when(passwordEncoder.encode("newSecretPass")).thenReturn("newSecretHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserService.resetPassword(3L, "newSecretPass");

        verify(passwordEncoder).encode("newSecretPass");
        verify(userRepository).save(employerRejected);
        assertEquals("newSecretHash", employerRejected.getPassword());
    }

    @Test
    void resetPassword_gagal_jika_user_tidak_ditemukan() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(99L, "newPassword123")
        );

        assertEquals("User tidak ditemukan", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_gagal_jika_target_user_adalah_admin() {
        User adminUser = new User();
        adminUser.setId(10L);
        adminUser.setName("Platform Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setPassword("adminSecret");

        when(userRepository.findById(10L)).thenReturn(Optional.of(adminUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(10L, "newPassword123")
        );

        assertEquals("Tidak dapat mereset password akun Admin", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_gagal_jika_userId_null() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(null, "newPassword123")
        );

        assertEquals("User ID tidak boleh null", ex.getMessage());
        verify(userRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_gagal_jika_password_null_atau_kosong() {
        IllegalArgumentException exNull = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(1L, null)
        );
        assertEquals("Password tidak boleh kosong", exNull.getMessage());

        IllegalArgumentException exEmpty = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(1L, "   ")
        );
        assertEquals("Password tidak boleh kosong", exEmpty.getMessage());

        verify(userRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_gagal_jika_password_kurang_dari_8_karakter() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.resetPassword(1L, "1234567")
        );

        assertEquals("Password must contain at least 8 characters", ex.getMessage());
        verify(userRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
