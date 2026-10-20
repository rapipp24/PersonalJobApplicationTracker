package com.example.service;

import com.example.entity.ApplicantProfile;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.ApplicantProfileRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicantProfileServiceTest {

    private ApplicantProfileRepository applicantProfileRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ApplicantProfileService service;

    @BeforeEach
    void setUp() {
        applicantProfileRepository = Mockito.mock(ApplicantProfileRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        service = new ApplicantProfileService(applicantProfileRepository, userRepository, passwordEncoder);
    }

    private User createApplicantUser() {
        User user = new User();
        user.setId(10L);
        user.setName("Budi Pelamar");
        user.setEmail("budi@example.com");
        user.setPassword("encodedOldPassword");
        user.setRole(Role.PELAMAR);
        return user;
    }

    private User createEmployerUser() {
        User user = new User();
        user.setId(20L);
        user.setName("Pak Bos");
        user.setEmail("bos@example.com");
        user.setPassword("encodedEmployerPassword");
        user.setRole(Role.PEMBERI_LAMARAN);
        return user;
    }

    // ==========================================
    // GET / CREATE PROFILE
    // ==========================================

    @Test
    void harusMengembalikanProfileExistingPelamar() {
        User applicant = createApplicantUser();
        ApplicantProfile existingProfile = new ApplicantProfile(applicant);
        existingProfile.setId(1L);
        existingProfile.setPhone("+62812345678");

        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existingProfile));

        ApplicantProfile result = service.getOrCreateProfile(applicant);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("+62812345678", result.getPhone());
        verify(applicantProfileRepository, never()).save(any());
    }

    @Test
    void harusMembuatProfileJikaPelamarBelumMemilikiProfile() {
        User applicant = createApplicantUser();

        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.empty());
        when(applicantProfileRepository.save(any(ApplicantProfile.class))).thenAnswer(inv -> {
            ApplicantProfile p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        ApplicantProfile result = service.getOrCreateProfile(applicant);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(applicant, result.getUser());
        verify(applicantProfileRepository).save(any(ApplicantProfile.class));
    }

    @Test
    void harusMenolakProfileUntukUserNonPelamar() {
        User employer = createEmployerUser();

        assertThrows(
                SecurityException.class,
                () -> service.getOrCreateProfile(employer)
        );

        verify(applicantProfileRepository, never()).findByUser(any());
    }

    @Test
    void harusMenolakProfileJikaUserNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getOrCreateProfile(null)
        );
    }

    // ==========================================
    // UPDATE PROFILE
    // ==========================================

    @Test
    void harusBisaUpdatePhoneLinkedInPortfolio() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);
        existing.setId(1L);

        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));
        when(applicantProfileRepository.save(any(ApplicantProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicantProfile updated = service.updateProfile(
                applicant,
                "+628123456789",
                "https://linkedin.com/in/budi",
                "https://budiportfolio.dev"
        );

        assertEquals("+628123456789", updated.getPhone());
        assertEquals("https://linkedin.com/in/budi", updated.getLinkedIn());
        assertEquals("https://budiportfolio.dev", updated.getPortfolio());
        verify(applicantProfileRepository).save(existing);
    }

    @Test
    void harusTrimDataProfile() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);

        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));
        when(applicantProfileRepository.save(any(ApplicantProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicantProfile updated = service.updateProfile(
                applicant,
                "  +628123456789  ",
                "  https://linkedin.com/in/budi  ",
                "  https://budiportfolio.dev  "
        );

        assertEquals("+628123456789", updated.getPhone());
        assertEquals("https://linkedin.com/in/budi", updated.getLinkedIn());
        assertEquals("https://budiportfolio.dev", updated.getPortfolio());
    }

    @Test
    void harusMengubahBlankProfileFieldMenjadiNull() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);
        existing.setPhone("+628123456789");
        existing.setLinkedIn("https://linkedin.com/in/budi");
        existing.setPortfolio("https://budiportfolio.dev");

        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));
        when(applicantProfileRepository.save(any(ApplicantProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicantProfile updated = service.updateProfile(applicant, "   ", "", null);

        assertNull(updated.getPhone());
        assertNull(updated.getLinkedIn());
        assertNull(updated.getPortfolio());
    }

    @Test
    void harusMenolakPhoneMelebihiBatas() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);
        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));

        String longPhone = "1234567890123456789012345678901"; // 31 chars

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProfile(applicant, longPhone, null, null)
        );

        assertEquals("Nomor telepon tidak boleh melebihi 30 karakter", ex.getMessage());
        verify(applicantProfileRepository, never()).save(any());
    }

    @Test
    void harusMenolakLinkedInMelebihiBatas() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);
        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));

        String longLinkedIn = "https://linkedin.com/in/" + "a".repeat(240); // 264 chars

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProfile(applicant, null, longLinkedIn, null)
        );

        assertEquals("LinkedIn tidak boleh melebihi 255 karakter", ex.getMessage());
        verify(applicantProfileRepository, never()).save(any());
    }

    @Test
    void harusMenolakPortfolioMelebihiBatas() {
        User applicant = createApplicantUser();
        ApplicantProfile existing = new ApplicantProfile(applicant);
        when(applicantProfileRepository.findByUser(applicant)).thenReturn(Optional.of(existing));

        String longPortfolio = "https://portfolio.com/" + "a".repeat(240); // 262 chars

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProfile(applicant, null, null, longPortfolio)
        );

        assertEquals("Portfolio tidak boleh melebihi 255 karakter", ex.getMessage());
        verify(applicantProfileRepository, never()).save(any());
    }

    // ==========================================
    // FULL NAME
    // ==========================================

    @Test
    void harusBisaUpdateNamaPelamar() {
        User applicant = createApplicantUser();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.updateFullName(applicant, "Budi Hartono");

        assertEquals("Budi Hartono", updated.getName());
        verify(userRepository).save(applicant);
    }

    @Test
    void harusTrimNamaPelamar() {
        User applicant = createApplicantUser();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.updateFullName(applicant, "   Budi Santoso   ");

        assertEquals("Budi Santoso", updated.getName());
        verify(userRepository).save(applicant);
    }

    @Test
    void harusMenolakNamaKosong() {
        User applicant = createApplicantUser();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateFullName(applicant, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateFullName(applicant, "   ")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakUpdateNamaUntukNonPelamar() {
        User employer = createEmployerUser();

        assertThrows(
                SecurityException.class,
                () -> service.updateFullName(employer, "Nama Baru")
        );

        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // CHANGE PASSWORD
    // ==========================================

    @Test
    void harusBisaGantiPasswordJikaCurrentPasswordBenar() {
        User applicant = createApplicantUser();
        applicant.setPassword("encodedOldHash");

        when(passwordEncoder.matches("oldPass123", "encodedOldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("encodedNewHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword(applicant, "oldPass123", "newPass456", "newPass456");

        verify(passwordEncoder).matches("oldPass123", "encodedOldHash");
        verify(passwordEncoder).encode("newPass456");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("encodedNewHash", userCaptor.getValue().getPassword());
    }

    @Test
    void harusMenolakGantiPasswordJikaCurrentPasswordSalah() {
        User applicant = createApplicantUser();
        applicant.setPassword("encodedOldHash");

        when(passwordEncoder.matches("wrongPass", "encodedOldHash")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(applicant, "wrongPass", "newPass456", "newPass456")
        );

        assertEquals("Password saat ini salah", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakGantiPasswordJikaConfirmTidakSama() {
        User applicant = createApplicantUser();
        applicant.setPassword("encodedOldHash");

        when(passwordEncoder.matches("oldPass123", "encodedOldHash")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(applicant, "oldPass123", "newPass456", "differentPass")
        );

        assertEquals("Konfirmasi password tidak cocok", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakGantiPasswordKosong() {
        User applicant = createApplicantUser();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(applicant, "", "newPass", "newPass")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(applicant, "oldPass", "", "newPass")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(applicant, "oldPass", "newPass", "")
        );

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void harusMenolakGantiPasswordUntukNonPelamar() {
        User employer = createEmployerUser();

        assertThrows(
                SecurityException.class,
                () -> service.changePassword(employer, "old", "new", "new")
        );

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }
}
