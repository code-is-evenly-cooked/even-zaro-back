package com.even.zaro.service;

import com.even.zaro.dto.PageResponse;
import com.even.zaro.dto.post.*;
import com.even.zaro.entity.Post;
import com.even.zaro.entity.Status;
import com.even.zaro.entity.User;
import com.even.zaro.global.event.event.PostDeletedEvent;
import com.even.zaro.global.event.event.PostSavedEvent;
import com.even.zaro.global.ErrorCode;
import com.even.zaro.global.exception.post.PostException;
import com.even.zaro.mapper.PostMapper;
import com.even.zaro.repository.FollowRepository;
import com.even.zaro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final PostMapper postMapper;
    private final PostRankBaselineMemoryStore postRankBaselineMemoryStore;
    private final FollowRepository followRepository;

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest request, Long userId) {

        Post.Category category = parseCategory(request.getCategory());
        validateImageRequirement(category, request.getPostImageList());

        Post.Tag tag = convertTag(request.getTag());
        validateTagForCategory(category, tag);

        User user = userService.findUserById(userId);
        userService.validateActiveUser(user);

        String thumbnailImage = resolveThumbnail(request.getThumbnailImage(), request.getPostImageList());

        Post post = Post.create(
                request.getTitle(),
                request.getContent(),
                category,
                tag,
                thumbnailImage,
                request.getPostImageList(),
                user
        );

        Post saved = postRepository.save(post);
        eventPublisher.publishEvent(new PostSavedEvent(saved));

        return postMapper.toPostDetailDto(saved);
    }

    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, Long userId) {
        Post post = findPostOrThrow(postId);

        User user = userService.findUserById(userId);
        userService.validateActiveUser(user);

        validatePostNotOwner(post, user);

        validateImageRequirement(post.getCategory(), request.getPostImageList());
        Post.Tag tag = convertTag(request.getTag());
        validateTagForCategory(post.getCategory(), tag);

        String thumbnailImage = resolveThumbnail(request.getThumbnailImage(), request.getPostImageList());

        post.update(
                request.getTitle(),
                request.getContent(),
                tag,
                request.getPostImageList(),
                thumbnailImage
        );
        eventPublisher.publishEvent(new PostSavedEvent(post));
    }

    @Transactional(readOnly = true)
    public PageResponse<PostPreviewDto> getPostListPage(String category, String tag, Pageable pageable) {
        Page<Post> page = resolvePostList(category, tag, pageable);
        return new PageResponse<>(page.map(postMapper::toPostPreviewDto));
    }

    private Page<Post> resolvePostList(String category, String tag, Pageable pageable) {

        boolean categoryEmpty = isBlank(category);
        boolean tagEmpty = isBlank(tag);

        if (categoryEmpty && tagEmpty) {
            return postRepository.findByIsDeletedFalseAndIsReportedFalse(pageable);
        }
        if (!categoryEmpty && tagEmpty) {
            Post.Category postCategory = parseCategory(category);
            return postRepository.findByCategoryAndIsDeletedFalseAndIsReportedFalse(postCategory, pageable);
        }
        if (!categoryEmpty) {
            Post.Category postCategory = parseCategory(category);
            Post.Tag postTag = convertTag(tag);
            validateTagForCategory(postCategory, postTag);
            return postRepository.findByCategoryAndTagAndIsDeletedFalseAndIsReportedFalse(postCategory, postTag, pageable);
        }
            throw new PostException(ErrorCode.INVALID_CATEGORY);
        }
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }


    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        Post post = findPostOrThrow(postId);
        PostDetailResponse response = postMapper.toPostDetailDto(post);

        User postOwner  = post.getUser();
        User loginUser = userService.findUserById(currentUserId);

        boolean isDeleted = postOwner.getStatus() == Status.DELETED;

        String nickname =  isDeleted ? "알 수 없는 사용자" : response.getUser().getNickname();
        String profileImage = isDeleted ? null : response.getUser().getProfileImage();
        LocalDate liveAloneDate = isDeleted ? null : response.getUser().getLiveAloneDate();

        boolean isFollowing = !loginUser.equals(postOwner) &&
                followRepository.existsByFollowerAndFollowee(loginUser, postOwner);

        PostDetailResponse.UserInfo followUser = PostDetailResponse.UserInfo.builder()
                .userId(response.getUser().getUserId())
                .nickname(nickname)
                .profileImage(profileImage)
                .liveAloneDate(liveAloneDate)
                .following(isFollowing)
                .build();

        return PostDetailResponse.builder()
                .postId(response.getPostId())
                .title(response.getTitle())
                .content(response.getContent())
                .thumbnailImage(response.getThumbnailImage())
                .category(response.getCategory())
                .tag(response.getTag())
                .likeCount(response.getLikeCount())
                .commentCount(response.getCommentCount())
                .postImageList(response.getPostImageList())
                .createdAt(response.getCreatedAt())
                .user(followUser)
                .build();
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findPostOrThrow(postId);

        User user = userService.findUserById(userId);
        userService.validateActiveUser(user);

        validatePostNotOwner(post, user);

        post.markAsDeleted();
        eventPublisher.publishEvent(new PostDeletedEvent(postId));
    }


    @Transactional
    public HomePostPreviewResponse getHomePostPreview() {
        List<Post> togetherPosts = postRepository.findTop5ByCategoryAndIsDeletedFalseAndIsReportedFalseOrderByCreatedAtDesc(Post.Category.TOGETHER);
        List<Post> dailyLifePosts = postRepository.findTop5ByCategoryAndIsDeletedFalseAndIsReportedFalseOrderByCreatedAtDesc(Post.Category.DAILY_LIFE);
        List<Post> randomBuyPosts = postRepository.findTop5ByCategoryAndIsDeletedFalseAndIsReportedFalseOrderByCreatedAtDesc(Post.Category.RANDOM_BUY);

        List<HomePostPreviewResponse.SimplePostDto> together = togetherPosts.stream()
                .map(postMapper::toSimplePostDto)
                .toList();

        List<HomePostPreviewResponse.SimplePostDto> dailyLife = dailyLifePosts.stream()
                .map(postMapper::toSimplePostDto)
                .toList();

        List<HomePostPreviewResponse.RandomBuyPostDto> randomBuy = randomBuyPosts.stream()
                .map(postMapper::toRandomBuyDto)
                .toList();

        return HomePostPreviewResponse.builder()
                .together(together)
                .dailyLife(dailyLife)
                .randomBuy(randomBuy)
                .build();
    }

    @Transactional
    public List<PostRankResponseDto> getRankedPosts() {
        List<Post> posts = postRepository.findTopPosts(0, PageRequest.of(0, 5));

        // 최초 baseline 순위 등록 (처음 진입 시 기준 순위 기록용)
        if (postRankBaselineMemoryStore.getBaselineRankIndexMap().isEmpty()) {
            postRankBaselineMemoryStore.updateBaselineRank(posts);
        }

        // 최초 prevRank 등록 (처음 진입 시 이전 순위 기록용)
        if (postRankBaselineMemoryStore.getPrevRankIndexMap().isEmpty()) {
            postRankBaselineMemoryStore.updatePrevRank(posts);
        }

        // 직전 순위 기준으로 rankChange 계산해야하므로 prevRankIndexMap 복사본을 확보
        Map<Long, Integer> prevRankMapBeforeUpdate = new ConcurrentHashMap<>(postRankBaselineMemoryStore.getPrevRankIndexMap());

        // 현재 순위 계산( 1 ~ 5 순위 )
        AtomicInteger currentIndex = new AtomicInteger(1);

        Map<Long, Integer> baselineMap = postRankBaselineMemoryStore.getBaselineRankIndexMap();

        List<PostRankResponseDto> result =  posts.stream()
                .map(post -> {
                    int currentRankIndex = currentIndex.getAndIncrement();
                    int baselineRankIndex = baselineMap.getOrDefault(post.getId(), currentRankIndex);
                    int prevRankIndex = prevRankMapBeforeUpdate.getOrDefault(post.getId(), currentRankIndex);

                    // 직전순위 - 현재순위
                    int rankChange = prevRankIndex - currentRankIndex;

                    return PostRankResponseDto.from(post,baselineRankIndex,currentRankIndex,rankChange);
                })
                .toList();

        postRankBaselineMemoryStore.updatePrevRank(posts);
        return result;
    }

    @Transactional
    public void updatePostScore(Post post) {
        post.updateScore();
    }

    // 공통 로직 분리
    /// 게시글을 찾을 수 없을때
    public Post findPostOrThrow(Long postId) {
        return postRepository.findByIdAndIsDeletedFalseAndIsReportedFalse(postId)
                .orElseThrow(() -> new PostException(ErrorCode.POST_NOT_FOUND));
    }

    /// 게시글 찾을 없을때 - 삭제만 검사 (postLike)
    public Post findUndeletedPostOrThrow(Long postId) {
        return postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new PostException(ErrorCode.POST_NOT_FOUND));
    }


    /// 올바르지 않은 카테고리 일때
    private Post.Category parseCategory(String category) {
        try {
            return Post.Category.valueOf(category);
        } catch (IllegalArgumentException e){
            throw new PostException(ErrorCode.INVALID_CATEGORY);
        }
    }


    /// 올바르지 않은 태그 일때
    private Post.Tag convertTag(String tag) {
        try {
            return Post.Tag.valueOf(tag);
        } catch (IllegalArgumentException e){
            throw new PostException(ErrorCode.INVALID_TAG);
        }
    }


    /// 텅장일기 카테고리 - 이미지 없을때
    private void validateImageRequirement(Post.Category category, List<String> postImages) {
        if (category == Post.Category.RANDOM_BUY &&
                (postImages == null || postImages.isEmpty())) {
            throw new PostException(ErrorCode.IMAGE_REQUIRED_FOR_RANDOM_BUY);
        }
    }

    /// 카테고리랑 일치하지 않은 태그를 선택했을때
    private void validateTagForCategory(Post.Category category, Post.Tag tag) {
        if (!category.isAllowed(tag)){
            throw new PostException(ErrorCode.INVALID_TAG_FOR_CATEGORY);
        }
    }


    /// 이미지 리스트에 없는 썸네일 이미지를 넣었을때
    private String resolveThumbnail(String thumbnailImage, List<String> postImages) {
        if (thumbnailImage != null) {
            if (postImages == null || !postImages.contains(thumbnailImage)) {
                throw new PostException(ErrorCode.THUMBNAIL_NOT_IN_IMAGE_LIST);
            }
            return thumbnailImage;
        }

        if (postImages != null && !postImages.isEmpty()) {
            return postImages.get(0);
        }

        return null;
    }

    /// 수정, 삭제 권한이 없을때
    private void validatePostNotOwner(Post post, User user) {
        if (!post.getUser().equals(user)) {
            throw new PostException(ErrorCode.INVALID_POST_OWNER);
        }
    }
}
