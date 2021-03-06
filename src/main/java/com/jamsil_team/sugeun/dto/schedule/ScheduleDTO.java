package com.jamsil_team.sugeun.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jamsil_team.sugeun.domain.schedule.Schedule;
import com.jamsil_team.sugeun.domain.scheduleSelect.ScheduleSelect;
import com.jamsil_team.sugeun.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {

    private Long scheduleId;

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @Builder.Default
    private List<Integer> selected = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm", timezone = "Asia/Seoul")
    @NotNull
    private LocalDateTime scheduleDate;

    public Map<String, Object> toEntity(){

        HashMap<String, Object> entityMap = new HashMap<>();

        //schedule 엔티티 생성
        Schedule schedule = Schedule.builder()
                .user(User.builder().userId(this.userId).build())
                .title(this.title)
                .scheduleDate(this.scheduleDate)
                .build();

        entityMap.put("schedule", schedule);

        List<ScheduleSelect> scheduleSelectList = getScheduleSelectEntities(selected, schedule);

        entityMap.put("scheduleSelectList", scheduleSelectList);

        return entityMap;

    }

    public List<ScheduleSelect> getScheduleSelectEntities(List<Integer> selected, Schedule schedule) {

        if(selected != null && selected.size() > 0){


            List<ScheduleSelect> scheduleSelectList = selected.stream().map(integer -> {

                LocalDate localDate = this.scheduleDate.minusDays(integer.longValue()).toLocalDate();

                ScheduleSelect scheduleSelect = ScheduleSelect.builder()
                        .schedule(schedule)
                        .selected(integer.intValue())
                        .alarmDateTime(localDate.atTime(12,00))
                        .build();

                return scheduleSelect;
            }).collect(Collectors.toList());

            return scheduleSelectList;

        }

        return null;
    }

}
