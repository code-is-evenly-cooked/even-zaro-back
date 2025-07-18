package com.even.zaro.unit.service;

import com.even.zaro.dto.user.*;
import com.even.zaro.entity.Gender;
import com.even.zaro.entity.Mbti;
import com.even.zaro.entity.Status;
import com.even.zaro.entity.User;
import com.even.zaro.global.ErrorCode;
import com.even.zaro.global.exception.user.UserException;
import com.even.zaro.repository.UserRepository;
import com.even.zaro.repository.WithdrawalHistoryRepository;
import com.even.zaro.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private WithdrawalHistoryRepository withdrawalHistoryRepository;

    @Nested
    class updateProfileImageTest {
        private User user;

        static User createTestUser() {
            return User.builder()
                    .id(1L)
                    .status(Status.ACTIVE)
                    .build();
        }

        @BeforeEach
        void setUp() {
            user = createTestUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }

        @Test
        void successfully_updated_profileImage() {
            UpdateProfileImageRequestDto requestDto = new UpdateProfileImageRequestDto("/images/profile/2-uuid.png");

            UpdateProfileImageResponseDto responseDto = userService.updateProfileImage(1L, requestDto);

            assertEquals("/images/profile/2-uuid.png", responseDto.getProfileImage());
        }

        @Test
        void shouldThrowException_whenUserDoesNotExist() {
            UpdateProfileImageRequestDto requestDto = new UpdateProfileImageRequestDto("/images/profile/2-uuid.png");
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UserException ex = assertThrows(UserException.class, () -> userService.updateProfileImage(1L, requestDto));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenUserIsNotVerified() {
            user.changeStatus(Status.PENDING);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            UpdateProfileImageRequestDto requestDto = new UpdateProfileImageRequestDto("/images/profile/2-uuid.png");

            UserException ex = assertThrows(UserException.class, () -> userService.updateProfileImage(1L, requestDto));

            assertEquals(ErrorCode.MAIL_NOT_VERIFIED, ex.getErrorCode());
        }
    }

    @Nested
    class updateNicknameTest {
        private User user;

        static User createTestUser() {
            return User.builder()
                    .id(1L)
                    .status(Status.ACTIVE)
                    .nickname("기존닉네임")
                    .build();
        }

        @BeforeEach
        void setUp() {
            user = createTestUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }

        @Test
        void successfully_updated_nickname() {
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("새닉네임");

            UpdateNicknameResponseDto responseDto = userService.updateNickname(1L, requestDto);

            assertEquals("새닉네임", responseDto.getNickname());
            assertNotNull(user.getLastNicknameUpdatedAt());
        }

        @Test
        void shouldThrowException_whenUserDoesNotExist() {
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("새닉네임");
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenUserIsNotVerified() {
            user.changeStatus(Status.PENDING);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("새닉네임");

            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));

            assertEquals(ErrorCode.MAIL_NOT_VERIFIED, ex.getErrorCode());
        }

        // 리팩토링으로 로직 테스트 불필요
//        @Test
//        void shouldThrowException_whenNewNicknameIsBlank() {
//            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("");
//
//            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));
//
//            assertEquals(ErrorCode.NEW_NICKNAME_REQUIRED, ex.getErrorCode());
//        }
//
//        @Test
//        void shouldThrowException_whenNicknameFormatIsInvalid() {
//            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("형식파괴!!!");
//
//            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));
//
//            assertEquals(ErrorCode.INVALID_NICKNAME_FORMAT, ex.getErrorCode());
//        }

        @Test
        void shouldThrowException_whenNicknameAlreadyExists() {
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("이미있니");
            when(userRepository.existsByNickname("이미있니")).thenReturn(true);

            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));

            assertEquals(ErrorCode.NICKNAME_ALREADY_EXISTED, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenNewNicknameIsSameAsOriginalOne() {
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("기존닉네임");

            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));

            assertEquals(ErrorCode.NEW_NICKNAME_EQUALS_ORIGINAL_NICKNAME, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenNicknameChangedWithinRestrictionPeriod() {
            user.updateLastNicknameUpdatedAt(LocalDateTime.now().minusDays(13));
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("새닉네임");

            UserException ex = assertThrows(UserException.class, () -> userService.updateNickname(1L, requestDto));

            assertEquals(ErrorCode.NICKNAME_UPDATE_COOLDOWN, ex.getErrorCode());
        }
    }

    @Nested
    class updateProfileTest {
        private User user;

        static User createTestUser() {
            return User.builder()
                    .id(1L)
                    .status(Status.ACTIVE)
                    .build();
        }

        @BeforeEach
        void setUp() {
            user = createTestUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }

        @Test
        void successfully_updates_all_profile_fields() {
            UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto(
                    LocalDate.parse("2000-10-10"),
                    LocalDate.parse("2024-02-04"),
                    "MALE",
                    "ENFP");

            userService.updateProfile(1L, requestDto);

            assertEquals(LocalDate.parse("2000-10-10"), user.getBirthday());
            assertEquals(LocalDate.parse("2024-02-04"), user.getLiveAloneDate());
            assertEquals(Gender.MALE, user.getGender());
            assertEquals(Mbti.ENFP, user.getMbti());
        }

        @Test
        void successfully_updates_profile_with_partial_fields() {
            UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto(
                    null,
                    LocalDate.parse("2024-02-04"),
                    null,
                    "ENFP");

            userService.updateProfile(1L, requestDto);

            assertNull(user.getBirthday());
            assertEquals(LocalDate.parse("2024-02-04"), user.getLiveAloneDate());
            assertNull(user.getGender());
            assertEquals(Mbti.ENFP, user.getMbti());
        }

        @Test
        void shouldThrowException_whenUserDoesNotExist() {
            UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto(
                    null,
                    LocalDate.parse("2024-02-04"),
                    null,
                    "ENFP");
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UserException ex = assertThrows(UserException.class, () -> userService.updateProfile(1L, requestDto));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenUserIsNotVerified() {
            user.changeStatus(Status.PENDING);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto(
                    null,
                    LocalDate.parse("2024-02-04"),
                    null,
                    "ENFP");

            UserException ex = assertThrows(UserException.class, () -> userService.updateProfile(1L, requestDto));

            assertEquals(ErrorCode.MAIL_NOT_VERIFIED, ex.getErrorCode());
        }
    }

    @Nested
    class updatePasswordTest {

        private User user;

        static User createTestUser() {
            return User.builder()
                    .id(1L)
                    .status(Status.ACTIVE)
                    .password("OldEncoded1!")
                    .build();
        }

        @BeforeEach
        void setUp() {
            user = createTestUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }


        @Test
        void successfully_password_updated() {
            //g
            UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("Old1234!", "New1234!");
            when(passwordEncoder.matches("Old1234!", "OldEncoded1!")).thenReturn(true);
            when(passwordEncoder.encode("New1234!")).thenReturn("NewEncoded1!");

            //w
            userService.updatePassword(1L, requestDto);

            //t
            assertEquals("NewEncoded1!", user.getPassword());
        }

        @Test
        void shouldThrowException_whenUserIsNotVerified() {
            user.changeStatus(Status.PENDING);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("Old1234!", "New1234!");

            UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));

            assertEquals(ErrorCode.MAIL_NOT_VERIFIED, ex.getErrorCode());
        }

        // 리팩토링으로 로직 테스트 불필요
//        @Test
//        void shouldThrowException_whenCurrentPasswordIsBlank() {
//            UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("", "New1234!");
//
//            UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));
//
//            assertEquals(ErrorCode.CURRENT_PASSWORD_REQUIRED, ex.getErrorCode());
//        }

        @Test
        void shouldThrowException_whenCurrentPasswordIsIncorrect() {
            UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("ThisIsWrong1!", "New1234!");
            when(passwordEncoder.matches("ThisIsWrong1!", "OldEncoded1!")).thenReturn(false);

            UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));

            assertEquals(ErrorCode.CURRENT_PASSWORD_WRONG, ex.getErrorCode());
        }
    }

    @Nested
    class softDelete {

        static User createTestUser() {
            return User.builder()
                    .id(1L)
                    .status(Status.ACTIVE)
                    .build();
        }

        @Test
        void successfully_softDeleted() {
            User user = createTestUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            WithdrawalRequestDto requestDto = new WithdrawalRequestDto("그냥요");

            userService.softDelete(user.getId(), requestDto);

            assertEquals(Status.DELETED, user.getStatus());
            assertNotNull(user.getDeletedAt());
        }

        @Test
        void shouldThrowException_whenUserIsAlreadyWithdrawn() {
            User user = createTestUser();
            user.changeStatus(Status.DELETED);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            WithdrawalRequestDto requestDto = new WithdrawalRequestDto("그냥요");


            UserException ex = assertThrows(UserException.class, () -> userService.softDelete(user.getId(), requestDto));

            assertEquals(ErrorCode.USER_ALREADY_DELETED, ex.getErrorCode());
        }
    }

    @Nested
    class ExceptionCases {

        @Test
        void shouldThrowException_whenUserDoesNotExistById() {
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            UserException ex = assertThrows(UserException.class, () -> userService.findUserById(userId));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void shouldThrowException_whenUserStatusIsPending() {
            User user = User.builder().status(Status.PENDING).build();

            UserException ex = assertThrows(UserException.class, () -> userService.validateActiveUser(user));

            assertEquals(ErrorCode.MAIL_NOT_VERIFIED, ex.getErrorCode());
        }


        @Nested
        class ValidateNewPassword {

            static User createTestUser() {
                return User.builder()
                        .id(1L)
                        .status(Status.ACTIVE)
                        .password("OldEncoded1!")
                        .build();
            }

            @BeforeEach
            void setUp() {
                User user = createTestUser();
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            }

            // 리팩토링으로 로직 테스트 불필요
//            @Test
//            void shouldThrowException_whenNewPasswordIsBlank() {
//                UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("Old1234!", "");
//                when(passwordEncoder.matches("Old1234!", "OldEncoded1!")).thenReturn(true);
//
//                UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));
//
//                assertEquals(ErrorCode.PASSWORD_REQUIRED, ex.getErrorCode());
//            }
//
//            @Test
//            void shouldThrowException_whenNewPasswordFormatIsInvalid() {
//                UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("Old1234!", "wrongFormat");
//                when(passwordEncoder.matches("Old1234!", "OldEncoded1!")).thenReturn(true);
//
//                UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));
//
//                assertEquals(ErrorCode.INVALID_PASSWORD_FORMAT, ex.getErrorCode());
//            }

            @Test
            void shouldThrowException_whenCurrentPasswordIsSameAsNewPassword() {
                UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("Old1234!", "Old1234!");
                when(passwordEncoder.matches("Old1234!", "OldEncoded1!")).thenReturn(true);

                UserException ex = assertThrows(UserException.class, () -> userService.updatePassword(1L, requestDto));

                assertEquals(ErrorCode.CURRENT_PASSWORD_EQUALS_NEW_PASSWORD, ex.getErrorCode());
            }
        }
    }
}