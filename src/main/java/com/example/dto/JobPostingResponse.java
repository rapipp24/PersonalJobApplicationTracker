package com.example.dto;

import com.example.entity.ApplicationTipe;
import com.example.entity.JobPosting;
import com.example.entity.JobTipe;

import java.time.LocalDate;

public record JobPostingResponse(
        Long id,
        String position,
        String companyName,
        String location,
        ApplicationTipe tipeKerja,
        JobTipe jobTipe,
        Long salaryMin,
        Long salaryMax,
        String description,
        String requirements,
        LocalDate postedDate
) {
    public static JobPostingResponse fromEntity(JobPosting job) {
        if (job == null) {
            return null;
        }

        return new JobPostingResponse(
                job.getId(),
                job.getPosition(),
                job.getCompanyName(),
                job.getLocation(),
                job.getTipeKerja(),
                job.getJobTipe(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getDescription(),
                job.getRequirements(),
                job.getPostedDate()
        );
    }
}
