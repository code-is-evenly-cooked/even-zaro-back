package com.even.zaro.global.event.listener;

import com.even.zaro.entity.PostLike;
import com.even.zaro.global.event.event.PostLikeCreatedEvent;
import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeListener {

    private final ApplicationEventPublisher eventPublisher;

    @PostPersist
    public void onPostLikePersist(PostLike postLike) {
        eventPublisher.publishEvent(new PostLikeCreatedEvent(postLike));
    }
}
