package com.jamsil_team.sugeun.service.user;

import com.jamsil_team.sugeun.domain.folder.Folder;
import com.jamsil_team.sugeun.domain.folder.FolderRepository;
import com.jamsil_team.sugeun.domain.link.Link;
import com.jamsil_team.sugeun.domain.link.LinkRepository;
import com.jamsil_team.sugeun.domain.phrase.Phrase;
import com.jamsil_team.sugeun.domain.phrase.PhraseRepository;
import com.jamsil_team.sugeun.domain.schedule.Schedule;
import com.jamsil_team.sugeun.domain.schedule.ScheduleRepository;
import com.jamsil_team.sugeun.domain.timeout.Timeout;
import com.jamsil_team.sugeun.domain.timeout.TimeoutRepository;
import com.jamsil_team.sugeun.domain.user.User;
import com.jamsil_team.sugeun.domain.user.UserRepository;

import com.jamsil_team.sugeun.dto.link.LinkResDTO;
import com.jamsil_team.sugeun.dto.phrase.PhraseResDTO;
import com.jamsil_team.sugeun.dto.user.BookmarkDTO;
import com.jamsil_team.sugeun.dto.user.UserResDTO;
import com.jamsil_team.sugeun.dto.user.UserSignupDTO;
import com.jamsil_team.sugeun.file.FileStore;
import com.jamsil_team.sugeun.file.ResultFileStore;
import com.jamsil_team.sugeun.handler.exception.CustomApiException;
import com.jamsil_team.sugeun.service.folder.FolderService;
import com.jamsil_team.sugeun.service.schedule.ScheduleService;
import com.jamsil_team.sugeun.service.timeout.TimeoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Transactional
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStore fileStore;
    private final PhraseRepository phraseRepository;
    private final LinkRepository linkRepository;
    private final TimeoutService timeoutService;
    private final TimeoutRepository timeoutRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final FolderService folderService;
    private final FolderRepository folderRepository;

    /**
     * ????????? ????????????
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean isDuplicateNickname(String nickname) {

        Optional<User> result = userRepository.findByNickname(nickname);

        if(result.isPresent()){
            return false;
        }

        return true;
    }

    /**
     * ????????????
     */
    @Transactional
    @Override
    public User join(UserSignupDTO userSignupDTO) {


        Optional<User> result = userRepository.findByNickname(userSignupDTO.getNickname());

        if(result.isPresent()){
            throw new CustomApiException("?????? ????????? ID ?????????.");
        }


        String rawPassword = userSignupDTO.getPassword();
        String encPassword = passwordEncoder.encode(rawPassword);
        userSignupDTO.setPassword(encPassword);

        User user = userSignupDTO.toEntity();

        userRepository.save(user);

        return user;

    }

    /**
     * ??????????????? ????????????
     */
    @Transactional
    @Override
    public void modifyUserImg(Long userId, MultipartFile multipartFile) throws IOException {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        //?????? ???????????? ????????? ?????? ????????? ?????? ??????
        fileRemove(user);

        ResultFileStore resultFileStore = fileStore.storeFile(multipartFile);

        //?????? ??????
        user.changeUserImg(resultFileStore);
    }


    /**
     *  ????????? ??????
     */
    @Transactional
    @Override
    public void modifyUserId(Long userId, String updateNickname) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        user.changeUserId(updateNickname);
    }


    /**
     * ???????????? ??????
     */
    @Transactional
    @Override
    public void modifyPassword(Long userId, String password) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        //?????????
        String encPassword = passwordEncoder.encode(password);

        user.changePassword(encPassword);
    }


    /**
     * ?????? ???????????? ??????
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean verifyPassword(Long userId, String password) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        System.out.println(password);

        if(passwordEncoder.matches(password, user.getPassword())){
            return true;
        }

        return false;
    }


    /**
     * ????????? ??????
     */
    @Transactional(readOnly = true)
    @Override
    public UserResDTO getUser(Long userId) throws IOException {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        UserResDTO userResDTO = user.toDTO();

        //????????? ?????? ?????????
        if(!(user.getStoreFilename().isBlank())){
            File file = new File(fileStore.getFullPath(user.getFolderPath(), user.getStoreFilename()));
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            userResDTO.setImageData(bytes);
        }


        return userResDTO;
    }


    /**
     * ????????? DTO ?????????
     */
    @Transactional(readOnly = true)
    @Override
    public BookmarkDTO getListOfBookmark(Long userId) {

        //bookmark = true ??? phrase, link ?????????
        List<Phrase> phraseList = phraseRepository.getPhraseBookmarkList(userId);
        List<Link> linkList = linkRepository.getLinkBookmarkList(userId);

        //bookmark = true ??? phraseDTO ?????????
        List<PhraseResDTO> phraseResDTOList = phraseList.stream().map(phrase -> {
            PhraseResDTO phraseResDTO = phrase.toResDTO();
            return phraseResDTO;
        }).collect(Collectors.toList());


        //bookmark = true ??? linkDTO ?????????
        List<LinkResDTO> linkResDTOList = linkList.stream().map(link -> {
            LinkResDTO linkResDTO = link.toResDTO();
            return linkResDTO;
        }).collect(Collectors.toList());


        return BookmarkDTO.builder()
                .phraseResDTOList(phraseResDTOList)
                .linkResDTOList(linkResDTOList)
                .build();
    }

    /**
     * ????????? ??????
     */
    @Transactional(readOnly = true)
    @Override
    public String findNickname(String phone) {

        String nickname = userRepository.nicknameFindByPhone(phone).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        return nickname;
    }

    /**
     * ????????? ?????? (???????????? ??????)
     */
    @Transactional(readOnly = true)
    @Override
    public Long checkNickname(String nickname) {

        User user = userRepository.findByNickname(nickname).orElseGet(() ->
                User.builder().userId(-1L).build());

        return user.getUserId();
    }

    /**
     * ????????? ?????? ?????? (???????????? ??????)
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean verifyPhone(Long userId, String phone) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        if(user.getPhone().equals(phone)){
            return true;
        }
        else{
            return false;
        }
    }


    /**
     * ?????? ??????
     */
    @Transactional
    @Override
    public void removeUser(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomApiException("???????????? ?????? ???????????????."));

        //scheduleSelect -> schedule -> timeoutSelect -> ??????????????? timeout ?????? -> timeout ->
        // phrase -> link -> ??????????????? folder ?????? -> folder ->
        // userRoleSet -> ??????????????? user ?????? -> user ????????? ??????


        //scheduleSelect, schedule ??????
        List<Schedule> scheduleList = scheduleRepository.getScheduleList(userId);
        scheduleList.stream().forEach(schedule -> scheduleService.removeSchedule(schedule.getScheduleId()));

        //timeoutSelect, ?????? ????????? timeout ??????, timeout ??????
        List<Timeout> timeoutList = timeoutRepository.getTimeoutList(userId);
        timeoutList.stream().forEach(timeout -> timeoutService.removeTimeout(timeout.getTimeoutId()));

        //phrase, link, ?????? ????????? folder ??????, folder ??????
        List<Folder> folderList = folderRepository.topFolderList(userId); //??????????????? ?????? (??????)
        folderList.stream().forEach(folder -> folderService.removeFolder(folder.getFolderId()));


        //?????? ????????? user ?????? ??????
        fileRemove(user);

        //user ??????
        userRepository.deleteById(userId);
    }

    private void fileRemove(User user) {

        //?????? ????????? ???????????? ?????? ??????
        if(user.getStoreFilename() != null && !(user.getStoreFilename().equals(""))){
            String folderPath = user.getFolderPath();
            String storeFilename = user.getStoreFilename();

            //?????? ????????? ??????
            File file = new File(fileStore.getFullPath(folderPath, storeFilename));
            file.delete();

            //????????? ????????? ??????
            File thumbnail = new File(fileStore.getThumbnailFullPath(folderPath, storeFilename));
            thumbnail.delete();
        }
    }


}
