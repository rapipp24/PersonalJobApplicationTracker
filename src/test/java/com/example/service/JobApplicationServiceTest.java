package com.example.service;

import com.example.entity.JobApplication;
import com.example.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

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
}