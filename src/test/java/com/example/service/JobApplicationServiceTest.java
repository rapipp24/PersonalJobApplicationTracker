package com.example.service;

import com.example.entity.ApplicationStatus;
import com.example.entity.JobApplication;
import com.example.repository.JobApplicationRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobApplicationServiceTest {

    @Test
    void harusBisaMenyimpanLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication jobApplication =
                new JobApplication();

        service.simpanLamaran(jobApplication);

        verify(repository).save(jobApplication);
    }


    @Test
    void harusBisaMenghapusLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication jobApplication =
                new JobApplication();

        service.hapusLamaran(jobApplication);

        verify(repository).delete(jobApplication);
    }


    @Test
    void harusBisaMengambilSemuaLamaran() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication lamaran1 = new JobApplication();
        JobApplication lamaran2 = new JobApplication();

        List<JobApplication> data =
                List.of(lamaran1, lamaran2);

        when(repository.findAll()).thenReturn(data);

        List<JobApplication> hasil =
                service.findAll();

        assertEquals(2, hasil.size());

        verify(repository).findAll();
    }


    @Test
    void harusBisaFilterBerdasarkanKeywordDanStatus() {

        JobApplicationRepository repository =
                Mockito.mock(JobApplicationRepository.class);

        JobApplicationService service =
                new JobApplicationService(repository);

        JobApplication lamaran1 = new JobApplication();
        lamaran1.setCompanyName("Winosa");
        lamaran1.setPosition("Java Developer");
        lamaran1.setStatus(ApplicationStatus.INTERVIEW);
        lamaran1.setNotes("Interview tahap akhir");

        JobApplication lamaran2 = new JobApplication();
        lamaran2.setCompanyName("Xdemia");
        lamaran2.setPosition("QA Engineer");
        lamaran2.setStatus(ApplicationStatus.APPLIED);
        lamaran2.setNotes("Menunggu screening");

        List<JobApplication> data =
                List.of(lamaran1, lamaran2);

        when(repository.findAll()).thenReturn(data);

        List<JobApplication> hasil =
                service.findAll(
                        "winosa",
                        ApplicationStatus.INTERVIEW
                );

        assertEquals(1, hasil.size());

        assertEquals(
                "Winosa",
                hasil.get(0).getCompanyName()
        );

        verify(repository).findAll();
    }
}