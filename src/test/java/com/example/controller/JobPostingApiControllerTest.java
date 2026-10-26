package com.example.controller;

import com.example.dto.JobPostingResponse;
import com.example.entity.ApplicationTipe;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.service.JobPostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingApiControllerTest {

    @Mock
    private JobPostingService jobPostingService;

    private JobPostingApiController controller;

    @BeforeEach
    void setUp() {
        controller = new JobPostingApiController(jobPostingService);
    }

    @Test
    void getActiveJobs_returns200OkWithListOfJobPostingResponse_whenActiveJobsExist() {
        User employer = new User("Recruiter John", "john@example.com", "secretHash", Role.PEMBERI_LAMARAN);
        employer.setId(99L);

        JobPosting job1 = new JobPosting();
        job1.setId(1L);
        job1.setPosition("Backend Engineer");
        job1.setCompanyName("PT Muara Buana");
        job1.setLocation("Jakarta Selatan");
        job1.setTipeKerja(ApplicationTipe.HYBRID);
        job1.setJobTipe(JobTipe.FULL_TIME);
        job1.setSalaryMin(10_000_000L);
        job1.setSalaryMax(15_000_000L);
        job1.setDescription("Mengembangkan REST API scalable.");
        job1.setRequirements("Minimal 2 tahun Java Spring Boot.");
        job1.setPostedDate(LocalDate.of(2026, 10, 24));
        job1.setActive(true);
        job1.setEmployer(employer);

        JobPosting job2 = new JobPosting();
        job2.setId(2L);
        job2.setPosition("Frontend Engineer");
        job2.setCompanyName("PT Sentosa Tech");
        job2.setLocation("Bandung");
        job2.setTipeKerja(ApplicationTipe.WFH);
        job2.setJobTipe(JobTipe.CONTRACT);
        job2.setSalaryMin(null); // testing nullable field
        job2.setSalaryMax(null);
        job2.setDescription("Membangun UI responsif.");
        job2.setRequirements("Pengalaman Vue/React.");
        job2.setPostedDate(LocalDate.of(2026, 10, 23));
        job2.setActive(true);
        job2.setEmployer(employer);

        when(jobPostingService.findActiveJobPostings()).thenReturn(List.of(job1, job2));

        ResponseEntity<List<JobPostingResponse>> response = controller.getActiveJobs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        JobPostingResponse dto1 = response.getBody().get(0);
        assertEquals(1L, dto1.id());
        assertEquals("Backend Engineer", dto1.position());
        assertEquals("PT Muara Buana", dto1.companyName());
        assertEquals("Jakarta Selatan", dto1.location());
        assertEquals(ApplicationTipe.HYBRID, dto1.tipeKerja());
        assertEquals(JobTipe.FULL_TIME, dto1.jobTipe());
        assertEquals(10_000_000L, dto1.salaryMin());
        assertEquals(15_000_000L, dto1.salaryMax());
        assertEquals("Mengembangkan REST API scalable.", dto1.description());
        assertEquals("Minimal 2 tahun Java Spring Boot.", dto1.requirements());
        assertEquals(LocalDate.of(2026, 10, 24), dto1.postedDate());

        JobPostingResponse dto2 = response.getBody().get(1);
        assertEquals(2L, dto2.id());
        assertNull(dto2.salaryMin());
        assertNull(dto2.salaryMax());

        verify(jobPostingService).findActiveJobPostings();
    }

    @Test
    void getActiveJobs_returns200OkWithEmptyList_whenNoActiveJobsExist() {
        when(jobPostingService.findActiveJobPostings()).thenReturn(Collections.emptyList());

        ResponseEntity<List<JobPostingResponse>> response = controller.getActiveJobs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(jobPostingService).findActiveJobPostings();
    }

    @Test
    void getActiveJobs_defensivelyFiltersOutInactiveJobs_ifReturnedByService() {
        JobPosting activeJob = new JobPosting();
        activeJob.setId(10L);
        activeJob.setPosition("Active Role");
        activeJob.setCompanyName("PT Aktif");
        activeJob.setActive(true);

        JobPosting inactiveJob = new JobPosting();
        inactiveJob.setId(11L);
        inactiveJob.setPosition("Inactive Role");
        inactiveJob.setCompanyName("PT Inaktif");
        inactiveJob.setActive(false);

        when(jobPostingService.findActiveJobPostings()).thenReturn(List.of(activeJob, inactiveJob));

        ResponseEntity<List<JobPostingResponse>> response = controller.getActiveJobs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).id());
    }

    @Test
    void getActiveJobs_handlesNullServiceReturnSafely() {
        when(jobPostingService.findActiveJobPostings()).thenReturn(null);

        ResponseEntity<List<JobPostingResponse>> response = controller.getActiveJobs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void jobPostingResponse_fromEntity_returnsNull_whenInputIsNull() {
        assertNull(JobPostingResponse.fromEntity(null));
    }
}
