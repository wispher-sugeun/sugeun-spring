package com.jamsil_team.sugeun.controller;

import com.jamsil_team.sugeun.domain.folder.FolderType;
import com.jamsil_team.sugeun.dto.folder.DetailFolderDTO;
import com.jamsil_team.sugeun.dto.folder.FolderDTO;
import com.jamsil_team.sugeun.dto.folder.FolderResDTO;
import com.jamsil_team.sugeun.handler.exception.CustomApiException;
import com.jamsil_team.sugeun.security.dto.AuthUserDTO;
import com.jamsil_team.sugeun.service.folder.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RequestMapping("/users/{user-id}/folders")
@RequiredArgsConstructor
@RestController
public class FolderController {

    private final FolderService folderService;


    /**
     *  폴더 목록
     */
    @GetMapping
    public ResponseEntity<List<FolderResDTO>> folderList(@PathVariable("user-id") Long userId,
                                                         @RequestParam(value = "type",required = false) String type,
                                                         @AuthenticationPrincipal AuthUserDTO authUserDTO){

        Long tokenUserId = authUserDTO.getUser().getUserId();

        if(!userId.equals(tokenUserId)){
            throw new CustomApiException("조회 권한이 없습니다.");
        }

        //폴더 전체보기일 경우
        if(type == null){
            List<FolderResDTO> folderResDTOListA = folderService.getListOfFolder(tokenUserId, null, null);
            return new ResponseEntity<>(folderResDTOListA, HttpStatus.OK);
        }

        List<FolderResDTO> folderResDTOListB = folderService.getListOfFolder(tokenUserId, FolderType.valueOf(type), null);

        return new ResponseEntity<>(folderResDTOListB, HttpStatus.OK);
    }

    /**
     *  폴더 생성
     */
    @PostMapping
    public ResponseEntity<String> createFolder(@Valid FolderDTO folderDTO, BindingResult bindingResult,
                                               @AuthenticationPrincipal AuthUserDTO authUserDTO) throws IOException {

        if(!folderDTO.getUserId().equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("생성 권한이 없습니다.");
        }

        folderService.createFolder(folderDTO);

        return new ResponseEntity<>("폴더생성 완료", HttpStatus.OK);
    }


    /**
     *  폴더 조회
     */
    @GetMapping("/{folder-id}")
    public ResponseEntity<DetailFolderDTO> readFolder(@PathVariable("user-id") Long userId,
                                                      @PathVariable("folder-id") Long folderId,
                                                      @AuthenticationPrincipal AuthUserDTO authUserDTO){

        Long tokenUserId = authUserDTO.getUser().getUserId();

        if(!userId.equals(tokenUserId)){
            throw new CustomApiException("조회 권한이 없습니다.");
        }

        DetailFolderDTO detailFolderDTO = folderService.getFolder(tokenUserId, folderId);

        return new ResponseEntity(detailFolderDTO, HttpStatus.OK);
    }

    /**
     * 폴더정보 변경
     */
    @PatchMapping("{folder-id}")
    public ResponseEntity<String> modifyFolder(@PathVariable("user-id") Long userId,
                                               @PathVariable("folder-id") Long folderId,
                                               FolderDTO folderDTO,
                                               @AuthenticationPrincipal AuthUserDTO authUserDTO) throws IOException {

        if(!userId.equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("변경 권한이 없습니다.");
        }

        if(folderDTO.getImageFile() != null){
            folderService.modifyFolderImage(folderId, folderDTO.getImageFile());
            return new ResponseEntity<>("이미지 업로드 완료", HttpStatus.OK);
        }

        if(folderDTO.getFolderName() != null){
            folderService.modifyFolderName(folderId, folderDTO.getFolderName());
            return new ResponseEntity<>("폴더이름 변경 완료", HttpStatus.OK);
        }

        return null;
    }

    /**
     * 폴더 삭제
     */
    @DeleteMapping("/{folder-id}")
    public ResponseEntity<String> removeFolder(@PathVariable("user-id") Long userId,
                                               @PathVariable("folder-id") Long folderId,
                                               @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!userId.equals(authUserDTO.getUser().getUserId())){
            throw new CustomApiException("삭제 권한이 없습니다.");
        }

        folderService.removeFolder(folderId);

        return new ResponseEntity<>("폴더삭제 완료", HttpStatus.OK);
    }
}
