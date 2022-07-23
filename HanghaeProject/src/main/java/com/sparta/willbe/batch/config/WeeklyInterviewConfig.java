package com.sparta.willbe.batch.config;

import com.sparta.willbe.batch.repository.WeeklyInterviewRepository;
import com.sparta.willbe.batch.jobListener.JobListener;
import com.sparta.willbe.batch.tables.WeeklyInterview;
import com.sparta.willbe.interview.exception.InterviewNotFoundException;
import com.sparta.willbe.interview.model.Interview;
import com.sparta.willbe.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class WeeklyInterviewConfig {

    private final WeeklyInterviewRepository weeklyInterviewRepository;
    private final InterviewRepository interviewRepository;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job weeklyInterviewJob(Step MigrationStep) {
        return jobBuilderFactory.get("weeklyInterviewJob")
                .incrementer(new RunIdIncrementer())
                .listener(new JobListener())
                .start(MigrationStep)
                .build();
    }

    //step 한개
    @JobScope
    @Bean
    public Step MigrationStep(Tasklet weeklyInterviewTasklet) {
        return stepBuilderFactory.get("MigrationStep")
                .tasklet(weeklyInterviewTasklet)
                .build();
    }

    @StepScope
    @Bean
    @Transactional
    public Tasklet weeklyInterviewTasklet() {
        return (contribution, chunkContext) -> {

            //기존 인터뷰 뱃지 삭제
            //인터뷰 테이블에서 기존 면접왕의 badge를 "NONE"으로 초기화하기 위한, 지지난주 주차 구하기
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            String monthOfWeekBeforeLast = String.valueOf(calendar.get(Calendar.MONTH)+1);
            String weekBeforeLast = String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH)-2); //지지난주
            String twoWeeksAgo = monthOfWeekBeforeLast + "월 "+ weekBeforeLast + "째주";
            log.info("인터뷰 테이블에서 기존 면접왕의 badge를 초기화할 주차(지지난주): {}", twoWeeksAgo);
            
            //위클리 테이블에서 지난 면접왕(지지난주) 데이터 가져오기 
            //List<WeeklyInterview> lastWeeklyInterview = weeklyInterviewRepository.findAll();
            List<WeeklyInterview> lastWeeklyInterview = weeklyInterviewRepository.findByWeeklyBadgeContains(twoWeeksAgo);
            for (WeeklyInterview lastWeeklyInterviewEach : lastWeeklyInterview){
                //Interview lastInterview = lastWeeklyInterviewEach.getInterview();
                Interview lastInterview = interviewRepository.findById(lastWeeklyInterviewEach.getInterviewId())
                        //.orElseThrow(ErrorMessage.NOT_FOUND_INTERVIEW::throwError);
                        .orElse(null);

                //해당 인터뷰가 삭제되지 않았으면
                if (lastInterview != null){
                    lastInterview.updateBadge("NONE");
                    interviewRepository.save(lastInterview);
                }
            }

            //이번주 면접왕 인터뷰 선정 + (추후 추가: 좋아요 숫자, 동점은 인터뷰 최신순)
            List<WeeklyInterview> weeklyInterviews = weeklyInterviewRepository.findWeeklyInterview(PageRequest.of(0,5));
            log.info("weeklyInterview top 5 >> {}등까지 추출됨", weeklyInterviews.size());

            //기존 위클리 면접왕 삭제 -> 기록
            //weeklyInterviewRepository.deleteAll();

            //위클리 면접왕 저장
            int ranking = 0;
            for (WeeklyInterview weeklyInterview : weeklyInterviews){

                ranking ++;

                //인터뷰 뱃지 골드,실버,브론즈 저장
                String[] badge = {"Gold", "Silver", "Bronze"};
                String[] rankArr = {"1등", "2등", "3등", "4등", "5등"};

                //현재날짜의 지난주
                String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
                String week = String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH)-1);  //지난주
                String weeklyBadge = month + "월 "+ week + "째주 " + ranking + "등";
                log.info("New Weekly Interview >> {} (InterviewId: {})", weeklyBadge, weeklyInterview.getInterviewId());

                //인터뷰 뱃지 저장(1,2,3등만)
                if (ranking <= 3){
                    //interview레포지토리에서 새로 불러와서
                    //Interview interview = weeklyInterview.getInterview();
                    //Interview interview = interviewRepository.findById(weeklyInterview.getInterview().getId())
                    Interview interview = interviewRepository.findById(weeklyInterview.getInterviewId())
                            .orElseThrow(InterviewNotFoundException::new);

                    interview.updateBadge(badge[ranking-1]);
                    interview = interviewRepository.save(interview);

                    WeeklyInterview weeklyInterviewEach = new WeeklyInterview(interview, weeklyInterview.getScrapCount(), badge[ranking-1], weeklyBadge);
                    weeklyInterviewEach.setWeeklyBadge(weeklyBadge);
                    weeklyInterviewRepository.save(weeklyInterviewEach);
                }else{
                    Interview interview = interviewRepository.findById(weeklyInterview.getInterviewId())
                            .orElseThrow(InterviewNotFoundException::new);
                    WeeklyInterview weeklyInterviewEach = new WeeklyInterview(interview, weeklyInterview.getScrapCount(), "NONE", weeklyBadge);

                    weeklyInterviewEach.setWeeklyBadge(weeklyBadge);
                    weeklyInterviewRepository.save(weeklyInterviewEach);
                }
            }

           return RepeatStatus.FINISHED;
        };
    }
}
