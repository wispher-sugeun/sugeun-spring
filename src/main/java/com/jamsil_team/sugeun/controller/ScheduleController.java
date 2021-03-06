package com.jamsil_team.sugeun.controller;

import com.jamsil_team.sugeun.domain.schedule.Schedule;
import com.jamsil_team.sugeun.dto.schedule.ScheduleDTO;
import com.jamsil_team.sugeun.dto.schedule.ScheduleResDTO;
import com.jamsil_team.sugeun.handler.exception.CustomApiException;
import com.jamsil_team.sugeun.security.dto.AuthUserDTO;
import com.jamsil_team.sugeun.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/users/{user-id}/schedules")
@RequiredArgsConstructor
@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;


    /**
     *  스케줄 목록
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResDTO>> scheduleList(@PathVariable("user-id") Long userId,
                                                             @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!userId.equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("조회 권한이 없습니다.");
        }

        List<ScheduleResDTO> scheduleDTOList = scheduleService.getListOfSchedule(userId);

        return new ResponseEntity<>(scheduleDTOList, HttpStatus.OK);

    }

    /**
     *  스케줄 생성
     */
    @PostMapping
    public ResponseEntity<Long> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO, BindingResult bindingResult,
                                                 @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!scheduleDTO.getUserId().equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("생성 권한이 없습니다.");
        }

        Schedule schedule = scheduleService.createSchedule(scheduleDTO);

        return new ResponseEntity<>(schedule.getScheduleId(), HttpStatus.OK);
    }


    /**
     *  스케줄 변경
     */
    @PutMapping("{schedule-id}")
    public ResponseEntity<String> modifySchedule(@Valid @RequestBody ScheduleDTO scheduleDTO, BindingResult bindingResult,
                                                 @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!scheduleDTO.getUserId().equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("변경 권한이 없습니다.");
        }

        scheduleService.modifySchedule(scheduleDTO);

        return new ResponseEntity<>("스케줄 변경 완료", HttpStatus.OK);
    }


    /**
     *  스케줄 삭제
     */
    @DeleteMapping("{schedule-id}")
    public ResponseEntity<String> removeSchedule(@PathVariable("user-id") Long userId,
                                                 @PathVariable("schedule-id") Long scheduleId,
                                                 @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!userId.equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("삭제 권한이 없습니다.");
        }

        scheduleService.removeSchedule(scheduleId);

        return new ResponseEntity<>("스케줄삭제 완료", HttpStatus.OK);
    }

}
