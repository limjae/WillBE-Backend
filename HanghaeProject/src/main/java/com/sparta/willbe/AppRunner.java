package com.sparta.willbe;

import com.sparta.willbe.category.model.CategoryEnum;
import com.sparta.willbe.question.model.Question;
import com.sparta.willbe.question.repostitory.QuestionRepository;
import com.sparta.willbe.user.model.Role;
import com.sparta.willbe.user.model.User;
import com.sparta.willbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@Slf4j
@RequiredArgsConstructor
//CommandLineRunner 인터페이스를 구현하고 @Component annotaion을 사용하면 Compoent Scan 이후 구동시점에 run이 실행됩니다.
//https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/CommandLineRunner.html
public class AppRunner implements CommandLineRunner {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        for (int i = 1; i < 10; i++) {
            int finalI = i;
            Arrays.stream(CategoryEnum.values()).forEach(c -> {
                Question question = new Question("Contents"+ finalI, "REF", c);
                questionRepository.save(question);
            });
        }

        User user = new User("Test1", "PSWD!@#$@124","mail@mail.mail", true, "url","url","hello", "token","default", false, Role.USER);
        userRepository.save(user);


    }

}