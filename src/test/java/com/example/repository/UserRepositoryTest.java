package com.example.repository;

import com.example.entity.CompanyMembershipStatus;
import com.example.entity.Role;
import com.example.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRepositoryTest {

    @Test
    void findForAdminMonitoring_normalizesNullKeywordToEmptyString_preventingPostgreSQLLowerByteaCrash() {
        UserRepository userRepository = mock(UserRepository.class);
        Page<User> dummyPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findForAdminMonitoring(any(), any(), any(), any(), any())).thenCallRealMethod();
        when(userRepository.findForAdminMonitoring(
                eq(Role.ADMIN),
                eq(null),
                eq(""),
                eq(null),
                eq(null),
                eq(Role.PELAMAR),
                eq(Role.PEMBERI_LAMARAN),
                eq(CompanyMembershipStatus.ACTIVE),
                eq(CompanyMembershipStatus.PENDING),
                any(Pageable.class)
        )).thenReturn(dummyPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findForAdminMonitoring(null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());

        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findForAdminMonitoring(
                eq(Role.ADMIN),
                eq(null),
                keywordCaptor.capture(),
                eq(null),
                eq(null),
                eq(Role.PELAMAR),
                eq(Role.PEMBERI_LAMARAN),
                eq(CompanyMembershipStatus.ACTIVE),
                eq(CompanyMembershipStatus.PENDING),
                eq(pageable)
        );
        assertEquals("", keywordCaptor.getValue());
    }

    @Test
    void findForAdminMonitoring_normalizesBlankKeywordToEmptyString() {
        UserRepository userRepository = mock(UserRepository.class);
        Page<User> dummyPage = new PageImpl<>(List.of());
        when(userRepository.findForAdminMonitoring(any(), any(), any(), any(), any())).thenCallRealMethod();
        when(userRepository.findForAdminMonitoring(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(dummyPage);

        Pageable pageable = PageRequest.of(0, 10);
        userRepository.findForAdminMonitoring(null, "   ", null, null, pageable);

        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findForAdminMonitoring(
                eq(Role.ADMIN),
                eq(null),
                keywordCaptor.capture(),
                eq(null),
                eq(null),
                eq(Role.PELAMAR),
                eq(Role.PEMBERI_LAMARAN),
                eq(CompanyMembershipStatus.ACTIVE),
                eq(CompanyMembershipStatus.PENDING),
                eq(pageable)
        );
        assertEquals("", keywordCaptor.getValue());
    }

    @Test
    void findForAdminMonitoring_normalizesMixedCaseKeywordToLowercase() {
        UserRepository userRepository = mock(UserRepository.class);
        Page<User> dummyPage = new PageImpl<>(List.of());
        when(userRepository.findForAdminMonitoring(any(), any(), any(), any(), any())).thenCallRealMethod();
        when(userRepository.findForAdminMonitoring(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(dummyPage);

        Pageable pageable = PageRequest.of(0, 10);
        userRepository.findForAdminMonitoring(Role.PEMBERI_LAMARAN, "HENDRA", 5L, CompanyMembershipStatus.PENDING, pageable);

        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findForAdminMonitoring(
                eq(Role.ADMIN),
                eq(Role.PEMBERI_LAMARAN),
                keywordCaptor.capture(),
                eq(5L),
                eq(CompanyMembershipStatus.PENDING),
                eq(Role.PELAMAR),
                eq(Role.PEMBERI_LAMARAN),
                eq(CompanyMembershipStatus.ACTIVE),
                eq(CompanyMembershipStatus.PENDING),
                eq(pageable)
        );
        assertEquals("hendra", keywordCaptor.getValue());
    }
}
