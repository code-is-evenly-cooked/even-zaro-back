package com.even.zaro.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 예시 - ApiTestController
    EXAMPLE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "예시 예외 발생"),
    EXAMPLE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 인증 Authentication
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 알 수 없는 오류가 발생했습니다."),
    INVALID_ENUM(HttpStatus.BAD_REQUEST, "지원하지 않는 enum 입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST,  "userId는 필수입니다."),
    UNAUTHORIZED_IMAGE_UPLOAD(HttpStatus.UNAUTHORIZED, "이미지 업로드 권한이 있는 사용자가 아닙니다."),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST,  "postId는 필수입니다."),
    INVALID_UPLOAD_TYPE(HttpStatus.BAD_REQUEST, "type은 'profile' 또는 'post' 여야 합니다."),
    UNAUTHORIZED_IMAGE_DELETE(HttpStatus.UNAUTHORIZED, "이미지 삭제 권한이 았는 사용자가 아닙니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST,  "지원하지 않는 이미지 확장자입니다."),

    // 회원 User
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일은 필수 입력 값입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "올바른 이메일 형식을 입력해주세요."),
    EMAIL_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수 입력 값입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함한 6자 이상이어야 합니다."),

    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "닉네임은 필수 입력 값입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "닉네임은 2-12자, 영문, 한글, 숫자, -, _만 가능합니다."),
    NICKNAME_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "가입된 이메일이 아닙니다."),
    INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_SIGNED_UP_NOT_VERIFIED_YET(HttpStatus.CONFLICT, "이미 가입한 계정입니다. 인증 메일을 다시 전송했습니다."),

    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 소셜 인증 토큰입니다."),

    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "탈퇴한 회원입니다."),

        // 이메일 인증
    EMAIL_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 인증 요청입니다."),
    EMAIL_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "인증 유효시간이 만료되었습니다."),
    EMAIL_TOKEN_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증이 완료된 요청입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    RESET_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 비밀번호 재설정 요청입니다."),
    RESET_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 비밀번호 재설정 요청입니다."),
    RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "비밀번호 재설정 유효시간이 만료되었습니다."),

        //jwt
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레쉬 토큰을 찾을 수 없습니다."),

        // 계정
    MAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다. 메일함을 확인해주세요."),
    DORMANT_USER(HttpStatus.FORBIDDEN, "휴면 계정입니다."),
    DELETED_USER(HttpStatus.FORBIDDEN, "탈퇴한 회원입니다."),
    CURRENT_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "현재 비밀번호는 필수 입력 값입니다."),
    CURRENT_PASSWORD_WRONG(HttpStatus.FORBIDDEN, "현재 비밀번호가 올바르지 않습니다."),
    CURRENT_PASSWORD_EQUALS_NEW_PASSWORD(HttpStatus.BAD_REQUEST,"이전과 다른 비밀번호를 입력해주세요."),
    NEW_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "새 닉네임을 입력해주세요."),
    NEW_NICKNAME_EQUALS_ORIGINAL_NICKNAME(HttpStatus.BAD_REQUEST, "이전 닉네임과 다른 닉네임을 입력해주세요."),
    NICKNAME_UPDATE_COOLDOWN(HttpStatus.CONFLICT, "닉네임은 최근 변경일로부터 14일 후에 변경할 수 있습니다."),

    // 알림 Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "조회된 알림이 없습니다."),
    ACTOR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 생성 주체 유저를 찾을 수 없습니다."),

    // 게시글 Post
    IMAGE_REQUIRED_FOR_RANDOM_BUY(HttpStatus.BAD_REQUEST, "이미지는 필수입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "올바르지 않은 카테고리입니다."),
    INVALID_TAG(HttpStatus.BAD_REQUEST, "올바르지 않은 태그입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND,"게시글을 찾을 수 없습니다."),
    INVALID_POST_OWNER(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_TAG_FOR_CATEGORY(HttpStatus.BAD_REQUEST, "해당 카테고리에는 사용할 수 없는 태그입니다."),
    THUMBNAIL_NOT_IN_IMAGE_LIST(HttpStatus.BAD_REQUEST, "썸네일은 이미지 목록에 포함되어야 합니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다."),
    LIKE_NOT_POST(HttpStatus.NOT_FOUND, "좋아요 정보가 존재하지 않습니다."),
    EMAIL_NOT_VERIFIED_LIKE(HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않아 좋아요 기능을 이용할 수 없습니다."),
    SEARCH_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "검색어는 필수입니다."),
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리는 필수입니다."),
    SEARCH_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과가 없습니다."),
    ALREADY_REPORTED_POST(HttpStatus.CONFLICT, "이미 신고한 게시글입니다."),
    CANNOT_REPORT_OWN_POST(HttpStatus.BAD_REQUEST,"본인의 게시글은 신고할 수 없습니다."),
    REASON_TEXT_REQUIRED_FOR_ETC(HttpStatus.BAD_REQUEST, "기타 사유를 선택한 경우 상세 내용을 입력해야 합니다."),

    // 댓글 Comments
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_NO_ASSOCIATED_POST(HttpStatus.INTERNAL_SERVER_ERROR, "댓글에 연결된 게시글이 존재하지 않습니다."),
    COMMENT_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "댓글 내용을 입력하지 않았습니다."),
    NOT_COMMENT_OWNER(HttpStatus.BAD_REQUEST, "댓글 작성자가 아닙니다."),
    MENTIONED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "멘션한 사용자를 찾을 수 없습니다."),
    CANNOT_REPORT_OWN_COMMENT(HttpStatus.BAD_REQUEST,"본인의 댓글은 신고할 수 없습니다."),
    ALREADY_REPORTED_COMMENT(HttpStatus.CONFLICT,"이미 신고한 댓글입니다."),
    HIDDEN_BY_REPORT_COMMENT(HttpStatus.FORBIDDEN, "신고 누적으로 숨김 처리된 댓글입니다."),
    COMMENT_TOO_LONG(HttpStatus.BAD_REQUEST, "댓글은 500자 이내로 작성해주세요."),
    COMMENT_REPORTED_CANNOT_EDIT(HttpStatus.BAD_REQUEST, "신고된 댓글은 수정하실 수 없습니다."),

    // 프로필 Profile
    FOLLOW_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팔로우하고 있는 사용자입니다."),
    FOLLOW_UNFOLLOW_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 언팔로우할 수 없습니다."),
    FOLLOW_NOT_EXIST(HttpStatus.NOT_FOUND, "팔로우하지 않은 사용자는 언팔로우할 수 없습니다."),

    // 그룹 Group
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹을 찾지 못했습니다."),
    GROUP_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹 리스트가 존재하지 않습니다"),
    GROUP_ALREADY_DELETE(HttpStatus.NOT_FOUND, "이미 삭제한 그룹입니다."),
    GROUP_ALREADY_EXIST(HttpStatus.NOT_FOUND, "이미 존재하는 그룹 이름입니다."),
    UNAUTHORIZED_GROUP_DELETE(HttpStatus.UNAUTHORIZED, "다른 사용자의 그룹 삭제 시도입니다."),
    UNAUTHORIZED_GROUP_UPDATE(HttpStatus.UNAUTHORIZED, "다른 사용자의 그룹 수정 시도입니다."),

    // 즐겨찾기 favorite
    FAVORITE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 즐겨찾기에 존재하는 장소는 추가할 수 없습니다."),
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 즐겨찾기를 찾지 못했습니다."),
    FAVORITE_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹에 즐겨찾기가 존재하지 않습니다."),
    UNAUTHORIZED_FAVORITE_UPDATE(HttpStatus.UNAUTHORIZED, "다른 사용자의 즐겨찾기 메모 수정 시도입니다."),
    UNAUTHORIZED_FAVORITE_DELETE(HttpStatus.UNAUTHORIZED, "다른 사용자의 즐겨찾기 삭제 시도입니다."),



    // 지도 Map
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장소를 찾지 못했습니다."),
    BY_COORDINATE_NOT_FOUND_PLACE_LIST(HttpStatus.NOT_FOUND, "인근에 조회된 장소가 없습니다."),



    // Health
    DB_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DB 연결 실패"),

    // 기본 예외
    UNKNOWN_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류 요청 URL을 다시 확인해보십시오."),
    ILLEGAL_STATE(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 상태입니다."),
    NULL_POINTER(HttpStatus.BAD_REQUEST, "필수 데이터가 누락되었습니다."),
    INVALID_ARGUMENT( HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 파라미터 타입이 일치하지 않습니다."),
    NUMBER_FORMAT_ERROR( HttpStatus.BAD_REQUEST, "숫자 형식 오류입니다."),
    DB_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 접근 오류입니다."),
    NO_RESULT(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),
    EMPTY_RESULT(HttpStatus.NOT_FOUND, "결과가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public String getCode() {
        return this.name();
    }
}