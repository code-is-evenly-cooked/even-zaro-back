package com.even.zaro.service;

import com.even.zaro.dto.map.MarkerInfoResponse;
import com.even.zaro.dto.map.PlaceResponse;
import com.even.zaro.entity.Favorite;
import com.even.zaro.entity.Place;
import com.even.zaro.global.ErrorCode;
import com.even.zaro.global.exception.map.MapException;
import com.even.zaro.mapper.MapMapper;
import com.even.zaro.repository.FavoriteRepository;
import com.even.zaro.repository.MapQueryRepository;
import com.even.zaro.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MapService {
    private final PlaceRepository placeRepository;
    private final FavoriteRepository favoriteRepository;
    private final MapQueryRepository mapQueryRepository;
    private final MapMapper mapMapper;

    public MarkerInfoResponse getPlaceInfo(long placeId) {

        Place selectPlace = placeRepository.findById(placeId)
                .orElseThrow(() -> new MapException(ErrorCode.PLACE_NOT_FOUND));

        // 해당 지역에 메모를 남긴 사용자들 리스트를 가져와야 함.
        List<Favorite> allByPlace = favoriteRepository.findAllByPlace(selectPlace);

        // 유저 메모 리스트 객체
        List<MarkerInfoResponse.UserSimpleResponse> userSimpleResponseList = mapMapper.toUserSimpleResponseList(allByPlace);

        // 마커 정보
        MarkerInfoResponse markerInfo = mapMapper.toMarkerInfoResponse(selectPlace, userSimpleResponseList);

        return markerInfo;
    }

    public PlaceResponse getPlacesByCoordinate(double lat, double lng, double distanceKm) {

        List<Place> placeByCoordinate = mapQueryRepository.findPlaceByCoordinate(lat, lng, distanceKm);

        List<PlaceResponse.PlaceInfo> placeInfos =  placeByCoordinate.stream()
                .sorted(Comparator.comparingInt(Place::getFavoriteCount).reversed()) // 내림차순 정렬
                .map(place -> PlaceResponse.PlaceInfo.builder()
                        .placeId(place.getId())
                        .kakaoPlaceId(place.getKakaoPlaceId())
                        .name(place.getName())
                        .address(place.getAddress())
                        .lat(place.getLat())
                        .lng(place.getLng())
                        .category(place.getCategory())
                        .favoriteCount(place.getFavoriteCount())
                        .build()
                ).toList();

        int totalCount = placeByCoordinate.size();

        PlaceResponse placeResponse = PlaceResponse.builder()
                .totalCount(totalCount)
                .placeInfos(placeInfos)
                .build();

        return placeResponse;
    }
}
