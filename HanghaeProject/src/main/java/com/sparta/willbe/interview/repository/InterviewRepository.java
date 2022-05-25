package com.sparta.willbe.interview.repository;

import com.sparta.willbe.category.model.CategoryEnum;
import com.sparta.willbe.interview.model.Interview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    //인터뷰 전체보기
    Page<Interview> findAllByIsDoneAndIsPublicAndUser_IsDeleted(Boolean isDone, Boolean isPublic, Boolean isDeleted, Pageable pageable);
    Page<Interview> findAllByIsDoneAndIsPublicAndUser_IsDeletedAndQuestion_Category(Boolean isDone, Boolean isPublic, Boolean isDeleted, CategoryEnum categoryEnum, Pageable pageable);
    List<Interview> findTop4ByIsDoneTrueAndIsPublicTrueOrderByCreatedAtDesc();

    @Query(value = "select p from Interview p JOIN p.user u where p.isDone = true and p.isPublic = true and u.isDeleted = false Order By size(p.scraps) desc")
    Page<Interview> findAllOrderByScrapsCountDesc(Pageable pageable);

    @Query(value = "select p from Interview p JOIN p.user u where p.isDone = true and p.isPublic = true and u.isDeleted = false and p.question.category = ?1 Order By size(p.scraps) desc")
    Page<Interview> findAllByQuestion_CategoryOrderByScrapsCountDesc(CategoryEnum categoryEnum, Pageable pageable);

    Page<Interview> findAllByIsDoneAndUser_IdAndUser_IsDeleted(Boolean isDone, Long userId, Boolean isDeleted, Pageable pageable);

    Page<Interview> findAllByIsDoneAndScraps_User_IdAndUser_IsDeleted(Boolean isDone, Long userId, Boolean isDeleted, Pageable pageable);

    @Query("SELECT q.category FROM Interview i LEFT JOIN i.question q WHERE i.question.id = q.id GROUP BY q.category ORDER BY COUNT(q.category ) DESC ")
    List<CategoryEnum> findCategoriesOrderedByCategoryCount(Pageable pageable);

//    void deleteById(Long id);
}