package com.jamsil_team.sugeun.dto.phrase;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jamsil_team.sugeun.domain.folder.Folder;
import com.jamsil_team.sugeun.domain.phrase.Phrase;
import com.jamsil_team.sugeun.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhraseDTO {

    private Long phraseId;

    @NotNull
    private Long userId;

    @NotNull
    private Long folderId;

    @NotBlank
    private String text;

    private Boolean bookmark;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Seoul")
    private LocalDate textDate;

    public Phrase toEntity(){

        Phrase phrase;

        if(this.getFolderId() == null){
            phrase = Phrase.builder()
                    .user(User.builder().userId(this.userId).build())
                    .text(this.text)
                    .build();

        }else {
            phrase = Phrase.builder()
                    .user(User.builder().userId(this.userId).build())
                    .folder(Folder.builder().folderId(this.folderId).build())
                    .text(this.text)
                    .build();

        }
        return phrase;

    }

}
