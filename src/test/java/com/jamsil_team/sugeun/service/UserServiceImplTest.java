package com.jamsil_team.sugeun.service;

import com.jamsil_team.sugeun.domain.folder.Folder;
import com.jamsil_team.sugeun.domain.folder.FolderRepository;
import com.jamsil_team.sugeun.domain.folder.FolderType;
import com.jamsil_team.sugeun.domain.link.Link;
import com.jamsil_team.sugeun.domain.link.LinkRepository;
import com.jamsil_team.sugeun.domain.phrase.Phrase;
import com.jamsil_team.sugeun.domain.phrase.PhraseRepository;
import com.jamsil_team.sugeun.domain.user.User;
import com.jamsil_team.sugeun.domain.user.UserRepository;
import com.jamsil_team.sugeun.dto.BookmarkDTO;
import com.jamsil_team.sugeun.dto.SignupDTO;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
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

    @Test
    void 중복확인() throws Exception{
        //given
        SignupDTO signUpDTO = createSignUpDTO();
        User user = signUpDTO.toEntity();
        userRepository.save(user);

        //when
        Boolean result = userService.isDuplicateNickname("형우2");

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void 회원가입() throws Exception{
        //given
        SignupDTO signUpDTO = createSignUpDTO();

        //when
        User user = userService.join(signUpDTO);

        //then
        Assertions.assertThat(user.getUserId()).isEqualTo("형우");
        Assertions.assertThat(user.getDeviceToken()).isNull();
    }

    @Test
    void 회원가입_실패() throws Exception{
        //given
        //기존 등록된 아이디
        SignupDTO signupDTO1 = createSignUpDTO();// loginId = 형우
        User user1 = signupDTO1.toEntity();
        userRepository.save(user1);

        //회원가입
        SignupDTO signupDTO2 = createSignUpDTO();// loginId = 형우

        //when
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> userService.join(signupDTO2));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("이미 등록된 ID 입니다.");
    }

    /*
    @Test
    void 기존비밀번호_검증_성공() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPassword("형우2", "1111");

        //then
        Assertions.assertThat(result).isTrue();
    }
    */


    /*
    @Test
    void 기존비밀번호_검증_실패() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        //when
        Boolean result = userService.verifyPassword("형우", "2222");

        //then
        Assertions.assertThat(result).isFalse();

    }

     */
    @Test
    @Commit
    void 프로필사진_업데이트_기존x() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), file);

        //then
        User savedUser = userRepository.findByUserId(user.getUserId()).get();
        String storeFilename = savedUser.getStoreFilename();

        Assertions.assertThat(savedUser.getFolderPath()).isNotBlank();
        Assertions.assertThat(storeFilename.substring(storeFilename.lastIndexOf("_")+1)).isEqualTo(file.getOriginalFilename());

    }

    @Test
    void 프로필사진_업데이트_기존o() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        MockMultipartFile file1 = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());
        //savedUserB의 folderPath 와 비교
        User savedUserA = userRepository.findByUserId(user.getUserId()).get();


        //프로필 사진 저장
        userService.modifyUserImg(user.getUserId(), file1);

        MockMultipartFile file2 = new MockMultipartFile("file", "filename-2.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), file2);

        //then
        User savedUserB = userRepository.findByUserId(user.getUserId()).get();
        String storeFilename = savedUserB.getStoreFilename();

        Assertions.assertThat(savedUserB.getFolderPath()).isNotNull();
        Assertions.assertThat(savedUserB.getFolderPath()).isNotBlank();
        Assertions.assertThat(savedUserB.getFolderPath()).isEqualTo(savedUserA.getFolderPath());
        Assertions.assertThat(storeFilename.substring(storeFilename.lastIndexOf("_")+1)).isEqualTo(file2.getOriginalFilename());
    }

    @Test
    void 프로필사진_업데이트_기존o_변경값NULL() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "image/jpeg", "some-image".getBytes());

        //when
        userService.modifyUserImg(user.getUserId(), null);

        //then
        User savedUser = userRepository.findByUserId(user.getUserId()).get();

        Assertions.assertThat(savedUser.getFolderPath()).isBlank();
        Assertions.assertThat(savedUser.getStoreFilename()).isBlank();
    }

    @Test
    void 프로필사진_업데이트_이미지파일x() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile("file", "filename-1.jpeg", "json/jpeg", "some-image".getBytes());

        //when
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> userService.modifyUserImg(user.getUserId(), file));

        //then
        Assertions.assertThat(e.getMessage()).isEqualTo("이미지 파일이 아닙니다.");
    }
    
    

    @Test
    void 비밀번호_변경() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);

        //when
        userService.modifyPassword("형우", "2222");

        //then
        Optional<User> result = userRepository.findByUserId("형우");
        User savedUser = result.get();

        boolean matches = passwordEncoder.matches("2222", savedUser.getPassword());

        Assertions.assertThat(matches).isTrue();
    }

    @Test
    void 북마크_리스트() throws Exception{
        //given
        User user = User.builder()
                .userId("형우")
                .password(passwordEncoder.encode("1111"))
                .phone("010-0000-0000")
                .deviceToken("adsf1r@Afdfas")
                .build();

        userRepository.save(user);
        //폴더 생성
        Folder folderA = createFolder(user, FolderType.PHRASE); //글귀 폴더
        Folder folderB = createFolder(user, FolderType.LINK); //링크 폴더

        //글귀 true : 1, false: 1 , 링크 true : 0, false : 1
        Phrase phraseTrue = Phrase.builder()
                .user(user)
                .folder(folderA)
                .text("북마크 테스트")
                .bookmark(true) //true
                .build();

        phraseRepository.save(phraseTrue);

        Phrase phraseFalse = Phrase.builder()
                .user(user)
                .folder(folderA)
                .text("북마크 테스트")
                .bookmark(false) //true
                .build();

        phraseRepository.save(phraseFalse);

        Link linkFalse = Link.builder()
                .user(user)
                .folder(folderB)
                .link("북마크 테스트")
                .bookmark(false) //false
                .build();

        linkRepository.save(linkFalse);

        //when
        BookmarkDTO bookmarkDTO = userService.getListOfBookmark(user.getUserId());

        //then phrase.bookmark = true 인 글귀 1개만 출력, link 빈 리스트 출력
        Assertions.assertThat(bookmarkDTO.getPhraseDTOList().size()).isEqualTo(1);
        Assertions.assertThat(bookmarkDTO.getPhraseDTOList().get(0).getBookmark()).isTrue();
        Assertions.assertThat(bookmarkDTO.getLinkDTOList()).isEmpty();
    }

    private Folder createFolder(User user, FolderType type) {
        Folder folder = Folder.builder()
                .user(user)
                .type(type)
                .folderName("폴더")
                .build();

        folderRepository.save(folder);

        return folder;
    }


    private SignupDTO createSignUpDTO() {
        SignupDTO signUpDTO = SignupDTO.builder()
                .userId("형우")
                .password("1111")
                .phone("010-0000-0000")
                .build();

        return signUpDTO;
    }
}