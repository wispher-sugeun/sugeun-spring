package com.jamsil_team.sugeun.service;

import com.jamsil_team.sugeun.domain.folder.Folder;
import com.jamsil_team.sugeun.domain.folder.FolderRepository;
import com.jamsil_team.sugeun.domain.folder.FolderType;
import com.jamsil_team.sugeun.domain.link.Link;
import com.jamsil_team.sugeun.domain.link.LinkRepository;
import com.jamsil_team.sugeun.domain.phrase.Phrase;
import com.jamsil_team.sugeun.domain.phrase.PhraseRepository;
import com.jamsil_team.sugeun.domain.schedule.Schedule;
import com.jamsil_team.sugeun.domain.schedule.ScheduleRepository;
import com.jamsil_team.sugeun.domain.scheduleSelect.ScheduleSelect;
import com.jamsil_team.sugeun.domain.scheduleSelect.ScheduleSelectRepository;
import com.jamsil_team.sugeun.domain.timeout.Timeout;
import com.jamsil_team.sugeun.domain.timeout.TimeoutRepository;
import com.jamsil_team.sugeun.domain.timeoutSelect.TimeoutSelect;
import com.jamsil_team.sugeun.domain.timeoutSelect.TimeoutSelectRepository;
import com.jamsil_team.sugeun.domain.user.User;
import com.jamsil_team.sugeun.domain.user.UserRepository;
import com.jamsil_team.sugeun.dto.user.BookmarkDTO;
import com.jamsil_team.sugeun.dto.user.UserResDTO;
import com.jamsil_team.sugeun.dto.user.UserSignupDTO;
import com.jamsil_team.sugeun.handler.exception.CustomApiException;
import com.jamsil_team.sugeun.service.user.UserService;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class UserServiceImplTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired FolderRepository folderRepository;
    @Autowired PhraseRepository phraseRepository;
    @Autowired LinkRepository linkRepository;
    @Autowired TimeoutRepository timeoutRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired TimeoutSelectRepository timeoutSelectRepository;
    @Autowired ScheduleSelectRepository scheduleSelectRepository;

    @Test
    void ????????????() throws Exception{
        //given
        UserSignupDTO signUpDTOUser = createSignUpDTO();

        User user = signUpDTOUser.toEntity();
        userRepository.save(user);

        //when
        Boolean result = userService.isDuplicateNickname("??????2");

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void ????????????() throws Exception{
        //given
        UserSignupDTO signUpDTOUser = createSignUpDTO();

        String rawPassword = signUpDTOUser.getPassword();
        //when
        User user = userService.join(signUpDTOUser);

        //then
        Assertions.assertThat(user.getUserId()).isNotNull();
        Assertions.assertThat(user.getNickname()).isEqualTo(signUpDTOUser.getNickname());

        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        Assertions.assertThat(matches).isTrue();

    }

    @Test
    void ????????????_??????() throws Exception{
        //given
        //?????? ????????? ?????????
        UserSignupDTO userSignupDTO1 = createSignUpDTO();// loginId = ??????
        User user1 = userSignupDTO1.toEntity();
        userRepository.save(user1);

        //????????????
        UserSignupDTO userSignupDTO2 = createSignUpDTO();// loginId = ??????

        //when
        CustomApiException e = assertThrows(CustomApiException.class,
                () -> userService.join(userSignupDTO2));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("?????? ????????? ID ?????????.");
    }


    @Test
    void ??????????????????_??????_??????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPassword(user.getUserId(), "1111");

        //then
        Assertions.assertThat(result).isTrue();
    }




    @Test
    void ??????????????????_??????_??????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPassword(user.getUserId(), "2222");

        //then
        Assertions.assertThat(result).isFalse();

    }

/*
    @Test
    void ???????????????_????????????_??????x() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), file);

        //then
        User savedUser = userRepository.findById(user.getUserId()).get();
        String storeFilename = savedUser.getStoreFilename();

        Assertions.assertThat(savedUser.getFolderPath()).isNotBlank();
        Assertions.assertThat(storeFilename.substring(storeFilename.lastIndexOf("_")+1)).isEqualTo(file.getOriginalFilename());

    }

    @Test
    void ???????????????_????????????_??????o() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        MockMultipartFile file1 = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());
        //savedUserB??? folderPath ??? ??????
        User savedUserA = userRepository.findById(user.getUserId()).get();


        //????????? ?????? ??????
        userService.modifyUserImg(user.getUserId(), file1);

        MockMultipartFile file2 = new MockMultipartFile("file", "filename-2.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), file2);

        //then
        User savedUserB = userRepository.findById(user.getUserId()).get();
        String storeFilename = savedUserB.getStoreFilename();

        Assertions.assertThat(savedUserB.getFolderPath()).isNotNull();
        Assertions.assertThat(savedUserB.getFolderPath()).isNotBlank();
        Assertions.assertThat(savedUserB.getFolderPath()).isEqualTo(savedUserA.getFolderPath());
        Assertions.assertThat(storeFilename.substring(storeFilename.lastIndexOf("_")+1)).isEqualTo(file2.getOriginalFilename());
    }
    */


    @Test
    void ???????????????_????????????_??????o_?????????NULL() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), null);

        //then
        User savedUser = userRepository.findById(user.getUserId()).get();

        Assertions.assertThat(savedUser.getFolderPath()).isBlank();
        Assertions.assertThat(savedUser.getStoreFilename()).isBlank();
    }

    @Test
    void ???????????????_????????????_???????????????x() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "json/jpeg", "some-image".getBytes());

        //when
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> userService.modifyUserImg(user.getUserId(), file));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("????????? ????????? ????????????.");
    }


    @Test
    void ?????????_??????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        userService.modifyUserId(user.getUserId(), "????????????");

        //then
        Assertions.assertThat(userRepository.findByNickname("????????????")).isNotNull();

        User savedUser = userRepository.findById(user.getUserId()).get();
        Assertions.assertThat(savedUser.getNickname()).isEqualTo("????????????");

        boolean matches = passwordEncoder.matches("1111", savedUser.getPassword());
        Assertions.assertThat(matches).isTrue();

        Assertions.assertThat(savedUser.getPhone()).isEqualTo("010-0000-0000");
    }



    @Test
    void ????????????_??????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        userService.modifyPassword(user.getUserId(), "2222");

        //then
        Optional<User> result = userRepository.findById(user.getUserId());
        User savedUser = result.get();

        boolean matches = passwordEncoder.matches("2222", savedUser.getPassword());

        Assertions.assertThat(matches).isTrue();
    }

    @Test
    void ?????????_??????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        UserResDTO userResDTO = userService.getUser(user.getUserId());

        //then
        Assertions.assertThat(userResDTO.getUserId()).isEqualTo(user.getUserId());
        Assertions.assertThat(userResDTO.getPhone()).isEqualTo(user.getPhone());
//        Assertions.assertThat(userDTO.getFolderPath()).isEqualTo(user.getFolderPath());
//        Assertions.assertThat(userDTO.getStoreFilename()).isEqualTo(user.getStoreFilename());
    }


    @Test
    void ?????????_?????????() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);
        //?????? ??????
        Folder folderA = createFolder(user, FolderType.PHRASE); //?????? ??????
        Folder folderB = createFolder(user, FolderType.LINK); //?????? ??????

        //?????? true : 1, false: 1 , ?????? true : 0, false : 1
        Phrase phraseTrue = Phrase.builder()
                .user(user)
                .folder(folderA)
                .text("????????? ?????????")
                .bookmark(true) //true
                .build();

        phraseRepository.save(phraseTrue);

        Phrase phraseFalse = Phrase.builder()
                .user(user)
                .folder(folderA)
                .text("????????? ?????????")
                .bookmark(false) //true
                .build();

        phraseRepository.save(phraseFalse);

        Link linkFalse = Link.builder()
                .user(user)
                .folder(folderB)
                .title("???????????? test")
                .link("????????? ?????????")
                .bookmark(false) //false
                .build();

        linkRepository.save(linkFalse);

        //when
        BookmarkDTO bookmarkDTO = userService.getListOfBookmark(user.getUserId());

        //then phrase.bookmark = true ??? ?????? 1?????? ??????, link ??? ????????? ??????
        Assertions.assertThat(bookmarkDTO.getPhraseResDTOList().size()).isEqualTo(1);
        Assertions.assertThat(bookmarkDTO.getPhraseResDTOList().get(0).getBookmark()).isTrue();
        Assertions.assertThat(bookmarkDTO.getLinkResDTOList()).isEmpty();
    }

    @Test
    void ???????????????_??????_??????o() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        String nickname = userService.findNickname(user.getPhone());

        //then
        Assertions.assertThat(nickname).isEqualTo(user.getNickname());
    }

    @Test
    void ???????????????_??????_??????x() throws Exception{
        //given
        //??? ??????: 010-0000-0000 ??? ????????? ???????????? ?????????.
        //when
        CustomApiException e = assertThrows(CustomApiException.class,
                () -> userService.findNickname("010-0000-0000"));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("???????????? ?????? ???????????????.");
    }

    @Test
    void ??????????????????_???????????????_??????O(){

        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        Long userId = userService.checkNickname("??????");

        //then
        Assertions.assertThat(userId).isEqualTo(user.getUserId());

    }

    @Test
    void ??????????????????_???????????????_??????x() throws Exception{
        //given
        //nickname: ?????? ??? ????????? ???????????? ?????????.

        //when
        Long userId = userService.checkNickname("??????");

        //then
        Assertions.assertThat(userId).isEqualTo(-1);
    }
    
    @Test
    void ??????????????????_?????????????????????_??????o() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPhone(user.getUserId(), "010-0000-0000");

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void ??????????????????_?????????????????????_??????x() throws Exception{
        //given
        User user = User.builder()
                .nickname("??????")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPhone(user.getUserId(), "010-1111-1111");

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void ????????????() throws Exception{
        //given
        //user ??????
        User user = User.builder()
                .nickname("??????1")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0001")
                .build();

        userRepository.save(user);

        //???????????? ??????
        Timeout timeout = Timeout.builder()
                .user(user)
                .title("???")
                .deadline(LocalDateTime.of(2021, 8, 01, 23, 59))
                .build();

        timeoutRepository.save(timeout);

        //timeout 1,3?????? ?????? ??????
        LocalDateTime before1 = timeout.getDeadline().minusDays(1).toLocalDate().atTime(12, 00);
        LocalDateTime before3 = timeout.getDeadline().minusDays(3).toLocalDate().atTime(12, 00);

        TimeoutSelect timeoutSelectA = TimeoutSelect.builder()
                .timeout(timeout) //timeout
                .alarmDateTime(before1) //1??????
                .selected(1)
                .build();

        timeoutSelectRepository.save(timeoutSelectA);

        TimeoutSelect timeoutSelectB = TimeoutSelect.builder()
                .timeout(timeout) //timeout
                .alarmDateTime(before3) //3??????
                .selected(3)
                .build();

        timeoutSelectRepository.save(timeoutSelectB);

        //schedule ??????
        Schedule schedule = Schedule.builder()
                .user(user)
                .title("2??? 30??? ?????????")
                .scheduleDate(LocalDateTime.of(2021, 7, 28, 14, 30))
                .build();

        scheduleRepository.save(schedule);

        //schedule 1,3?????? ?????? ??????
        ScheduleSelect scheduleSelectA = ScheduleSelect.builder()
                .schedule(schedule) //schedule
                .alarmDateTime(before1) //1??????
                .selected(1)
                .build();

        scheduleSelectRepository.save(scheduleSelectA);

        ScheduleSelect scheduleSelectB = ScheduleSelect.builder()
                .schedule(schedule) //schedule
                .alarmDateTime(before3) //3??????
                .selected(3)
                .build();

        scheduleSelectRepository.save(scheduleSelectB);

        //folder ??????
        Folder folderA = createFolder(user, FolderType.PHRASE);
        Folder folderB = createFolder(user, FolderType.LINK);

        //folderA ?????? ?????? ??????
        Folder folderC = Folder.builder()
                .type(FolderType.PHRASE)
                .user(user)
                .parentFolder(folderA)
                .folderName("????????????")
                .build();

        folderRepository.save(folderC);

        //phrase ??????
        Phrase phrase = Phrase.builder()
                .folder(folderA)
                .user(user)
                .text("??????A")
                .build();

        phraseRepository.save(phrase);

        //link ??????
        Link link = Link.builder()
                .folder(folderB)
                .user(user)
                .title("????????????")
                .link("naver.com")
                .build();

        linkRepository.save(link);


        /**
         * user ?????? -
         * timeout 1???, timeoutSelect 2???, schedule 1???, scheduleSelect 2???,
         * folder 3??? - (folderC??? folderA??? ????????????), phrase 1???, link 1??? ??????
         */

        //when
        userService.removeUser(user.getUserId());

        //then
        //????????? ???????????? ?????? ??????
        NoSuchElementException e1 = assertThrows(NoSuchElementException.class,
                () -> (scheduleSelectRepository.findById(scheduleSelectA.getScheduleSelectId())).get());
        NoSuchElementException e2 = assertThrows(NoSuchElementException.class,
                () -> (scheduleSelectRepository.findById(scheduleSelectB.getScheduleSelectId())).get());

        //????????? ????????? ??????
        NoSuchElementException e3 = assertThrows(NoSuchElementException.class,
                () -> (scheduleRepository.findById(schedule.getScheduleId())).get());
        //????????? ??????????????? ?????? ??????
        NoSuchElementException e4 = assertThrows(NoSuchElementException.class,
                () -> (timeoutSelectRepository.findById(timeoutSelectA.getTimeoutSelectId())).get());
        NoSuchElementException e5 = assertThrows(NoSuchElementException.class,
                () -> (timeoutSelectRepository.findById(timeoutSelectB.getTimeoutSelectId())).get());

        //????????? ???????????? ??????
        NoSuchElementException e6 = assertThrows(NoSuchElementException.class,
                () -> (timeoutRepository.findById(timeout.getTimeoutId())).get());

        //????????? ?????? ??????
        NoSuchElementException e7 = assertThrows(NoSuchElementException.class,
                () -> (phraseRepository.findById(phrase.getPhraseId())).get());
        //????????? ?????? ??????
        NoSuchElementException e8 = assertThrows(NoSuchElementException.class,
                () -> (linkRepository.findById(link.getLinkId())).get());
        //????????? ?????? ??????
        NoSuchElementException e9 = assertThrows(NoSuchElementException.class,
                () -> folderRepository.findById(folderA.getFolderId()).get());
        NoSuchElementException e10 = assertThrows(NoSuchElementException.class,
                () -> folderRepository.findById(folderB.getFolderId()).get());
        NoSuchElementException e11 = assertThrows(NoSuchElementException.class,
                () -> folderRepository.findById(folderC.getFolderId()).get());

        //????????? ?????? ??????
        NoSuchElementException e12 = assertThrows(NoSuchElementException.class,
                () -> userRepository.findById(user.getUserId()).get());

        Assertions.assertThat(e1.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e2.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e3.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e4.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e5.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e6.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e7.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e8.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e9.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e10.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e11.getMessage()).isEqualTo("No value present");
        Assertions.assertThat(e12.getMessage()).isEqualTo("No value present");


    }

    private Folder createFolder(User user, FolderType type) {
        Folder folder = Folder.builder()
                .user(user)
                .type(type)
                .folderName("??????")
                .build();

        folderRepository.save(folder);

        return folder;
    }


    private UserSignupDTO createSignUpDTO() {
        UserSignupDTO signUpDTOUser = UserSignupDTO.builder()
                .nickname("?????????")
                .password("1111")
                .phone("010-0000-0000")
                .build();

        return signUpDTOUser;
    }
}