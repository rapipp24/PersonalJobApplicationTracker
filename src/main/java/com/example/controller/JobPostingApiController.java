package com.example.controller;

import com.example.dto.JobPostingResponse;
import com.example.entity.JobPosting;
import com.example.service.JobPostingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
public class JobPostingApiController {

    private final JobPostingService jobPostingService;

    public JobPostingApiController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> getActiveJobs() {
        List<JobPosting> activeJobs = jobPostingService.findActiveJobPostings();

        List<JobPostingResponse> responseList = new ArrayList<>();
        if (activeJobs != null) {
            for (JobPosting job : activeJobs) {
                if (job != null && job.isActive()) {
                    responseList.add(JobPostingResponse.fromEntity(job));
                }
            }
        }

        return ResponseEntity.ok(responseList);
    }
}
