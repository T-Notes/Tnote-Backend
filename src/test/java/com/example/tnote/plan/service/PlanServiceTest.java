package com.example.tnote.plan.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tnote.boundedContext.plan.dto.PlanDeleteResponse;
import com.example.tnote.boundedContext.plan.dto.PlanResponse;
import com.example.tnote.boundedContext.plan.dto.PlanSaveRequest;
import com.example.tnote.boundedContext.plan.entity.Plan;
import com.example.tnote.boundedContext.plan.exception.PlanException;
import com.example.tnote.boundedContext.plan.repository.PlanRepository;
import com.example.tnote.boundedContext.plan.service.PlanService;
import com.example.tnote.boundedContext.schedule.entity.Schedule;
import com.example.tnote.boundedContext.schedule.repository.ScheduleRepository;
import com.example.tnote.boundedContext.user.entity.User;
import com.example.tnote.boundedContext.user.repository.UserRepository;
import com.example.tnote.utils.TestSyUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;


    @Nested
    @DisplayName("일정 생성/삭제")
    class saveAndDelete {
        User user = mock(User.class);

        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        Schedule schedule = new Schedule(1L, "1학기", null,
                startDate, endDate, user);
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 11, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 19, 0, 0);

        @DisplayName("일정 생성")
        @Test
        void save() {
            PlanSaveRequest request = new PlanSaveRequest("Development", startDateTime, endDateTime, "Seoul",
                    "Project Development", "Team");

            when(userRepository.findUserById(1L)).thenReturn(user);
            when(scheduleRepository.findScheduleById(1L)).thenReturn(schedule);
            when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PlanResponse result = planService.save(1L, 1L, request, null);

            assertThat(result).isNotNull();
        }

        @DisplayName("일정 삭제")
        @Test
        void delete() {
            Long planId = 1L;
            Long userId = 1L;
            Plan plan = new Plan("New Year Plan", startDateTime, endDateTime, "New York",
                    "Celebration", "Everyone", user, schedule, new ArrayList<>());

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            doNothing().when(planRepository).delete(plan);

            PlanDeleteResponse response = planService.delete(planId, userId);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(plan.getId());
            verify(planRepository).delete(plan);
        }


        @Test
        @DisplayName("존재하지 않는 일정 삭제 시 예외")
        void deleteNotExist() {
            Long planId = 12L;
            Long userId = 1L;

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> {
                planService.delete(planId, userId);
            }).isInstanceOf(PlanException.class);

            verify(planRepository, never()).delete(any(Plan.class));
        }

    }
}