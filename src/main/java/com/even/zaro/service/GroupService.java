package com.even.zaro.service;

import com.even.zaro.dto.group.GroupCreateRequest;
import com.even.zaro.dto.group.GroupEditRequest;
import com.even.zaro.dto.group.GroupResponse;
import com.even.zaro.entity.Favorite;
import com.even.zaro.entity.FavoriteGroup;
import com.even.zaro.entity.User;
import com.even.zaro.global.ErrorCode;
import com.even.zaro.global.exception.favorite.FavoriteException;
import com.even.zaro.global.exception.group.GroupException;
import com.even.zaro.global.exception.user.UserException;
import com.even.zaro.mapper.GroupMapper;
import com.even.zaro.repository.FavoriteGroupRepository;
import com.even.zaro.repository.FavoriteRepository;
import com.even.zaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GroupService {
    private final UserRepository userRepository;
    private final FavoriteGroupRepository favoriteGroupRepository;
    private final GroupMapper groupMapper;
    private final FavoriteRepository favoriteRepository;

    public GroupResponse createGroup(GroupCreateRequest request, long userid) {

        User user = userRepository.findById(userid).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        boolean dupCheck = groupNameDuplicateCheck(request.getGroupName(), userid);

        // 해당 유저가 이미 있는 그룹 이름을 입력했을 때
        if (dupCheck) {
            throw new GroupException(ErrorCode.GROUP_ALREADY_EXIST);
        }

        FavoriteGroup favoriteGroup = FavoriteGroup.builder()
                .user(user) // 유저 설정
                .name(request.getGroupName()) // Group 이름 설정
                .build();
        favoriteGroupRepository.save(favoriteGroup);

        return groupMapper.toGroupResponse(favoriteGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getFavoriteGroups(long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        // userId 값이 일치하고, 삭제된 데이터를 제외하고 조회
        List<FavoriteGroup> activeGroups = favoriteGroupRepository.findAllByUserAndDeletedFalse(user);

        return groupMapper.toGroupResponseList(activeGroups);
    }

    public void deleteGroup(long groupId, long userId) {
        FavoriteGroup group = favoriteGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(ErrorCode.GROUP_NOT_FOUND));

        // 다른 사용자의 그룹 삭제 방지
        if (group.getUser().getId() != userId) {
            throw new GroupException(ErrorCode.UNAUTHORIZED_GROUP_DELETE);
        }

        // 이미 삭제 처리 된 경우
        if (group.isDeleted()) {
            throw new GroupException(ErrorCode.GROUP_ALREADY_DELETE);
        }

        // 그룹 내의 즐겨찾기 전부 삭제 처리
        favoriteRepository.findAllByGroup(group).forEach(Favorite::setDeleteTrue);

        group.setIsDeleted();

        favoriteGroupRepository.save(group);
    }

    public void editGroup(long groupId, GroupEditRequest request, long userId) {
        FavoriteGroup group = favoriteGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(ErrorCode.GROUP_NOT_FOUND));

        // 다른 사용자의 즐겨찾기 그룹 수정 방지
        if (group.getUser().getId() != userId) {
            throw new GroupException(ErrorCode.UNAUTHORIZED_GROUP_UPDATE);
        }

        boolean dupCheck = groupNameDuplicateCheck(request.getGroupName(), group.getUser().getId());

        // 해당 유저가 이미 있는 즐겨찾기 그룹 이름을 입력했을 때
        if (dupCheck) {
            throw new GroupException(ErrorCode.GROUP_ALREADY_EXIST);
        }
        group.editGroupName(request.getGroupName());
    }


    // 입력한 그룹 이름이 이미 해당 userId가 가지고 있는지 확인
    public boolean groupNameDuplicateCheck(String groupName, long userId) {
        return favoriteGroupRepository.existsByUserIdAndName(userId, groupName);
    }
}
