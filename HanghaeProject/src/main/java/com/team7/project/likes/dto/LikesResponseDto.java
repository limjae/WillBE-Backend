package com.team7.project.likes.dto;

import com.team7.project.likes.model.Likes;
//import com.team7.project.likes.model.LikesData;
import com.team7.project.user.dto.UserInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class LikesResponseDto {
    private Map<Integer,Integer> likesData ;
    private Long TopOne;
    private Long Toptwo;
    private Long TopThree;
    private UserInfoResponseDto userInfoResponseDto;
}