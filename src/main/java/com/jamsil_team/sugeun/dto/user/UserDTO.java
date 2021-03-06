package com.jamsil_team.sugeun.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long userId;

    private String nickname;

    private String password;

    private String phone;

    private Boolean alarm;

    private String folderPath;

    private String storeFilename;

}
