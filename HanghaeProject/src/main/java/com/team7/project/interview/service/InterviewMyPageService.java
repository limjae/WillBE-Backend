package com.team7.project.interview.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.team7.project._global.pagination.dto.PaginationResponseDto;
import com.team7.project.advice.RestException;
import com.team7.project.interview.dto.InterviewInfoResponseDto;
import com.team7.project.interview.dto.InterviewListResponseDto;
import com.team7.project.interview.dto.InterviewUpdateRequestDto;
import com.team7.project.interview.model.Interview;
import com.team7.project.interview.repository.InterviewRepository;
import com.team7.project.scrap.model.Scrap;
import com.team7.project.user.model.User;
import com.team7.project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class InterviewMyPageService {
    private final InterviewGeneralService interviewGeneralService;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    private static final long ONE_HOUR = 1000 * 60 * 60; // 1시간
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름

    public String generatePresignedUrl(String objectKey) {

        Date expireTime = new Date();
        expireTime.setTime(expireTime.getTime() + ONE_HOUR);

        // Generate the pre-signed URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expireTime);

        URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }

    public InterviewListResponseDto readAllMyInterviews(Pageable pageable, Long loginUserId){

        User user = userRepository.findById(loginUserId)
                .orElseThrow(
                        () -> new RestException(HttpStatus.BAD_REQUEST, "해당 유저가 존재하지 않습니다.")
                );

        Page<Interview> interviews = interviewRepository.findAllByIsDoneAndUser_Id(true, loginUserId, pageable);

        List<InterviewInfoResponseDto.Data> responses = new ArrayList<>();

        Set<Long> userScrapsId = interviewGeneralService.createUserScrapIds(user);

        for(Interview interview: interviews.getContent()){

            InterviewInfoResponseDto response = interviewGeneralService.createInterviewResponse(loginUserId, userScrapsId, interview);

            responses.add(response.getInterview());

        }

        PaginationResponseDto pagination = new PaginationResponseDto((long) pageable.getPageSize(),
                interviews.getTotalElements(),
                (long) pageable.getPageNumber() + 1);

        return new InterviewListResponseDto(responses, pagination);
    }

    public InterviewListResponseDto readAllMyScraps(Pageable pageable, Long loginUserId){

        User user = userRepository.findById(loginUserId)
                .orElseThrow(
                        () -> new RestException(HttpStatus.BAD_REQUEST, "해당 유저가 존재하지 않습니다.")
                );

        Page<Interview> interviews = interviewRepository.findAllByIsDoneAndScraps_User_Id(true, loginUserId, pageable);

        List<InterviewInfoResponseDto.Data> responses = new ArrayList<>();

        Set<Long> userScrapsId = interviewGeneralService.createUserScrapIds(user);

        for(Interview interview: interviews.getContent()){

            InterviewInfoResponseDto response = interviewGeneralService.createInterviewResponse(loginUserId, userScrapsId, interview);

            responses.add(response.getInterview());

        }

        PaginationResponseDto pagination = new PaginationResponseDto((long) pageable.getPageSize(),
                interviews.getTotalElements(),
                (long) pageable.getPageNumber() + 1);

        return new InterviewListResponseDto(responses, pagination);
    }


}
