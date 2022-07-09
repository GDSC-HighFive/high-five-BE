package com.example.highfive.domain.weather.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class WeatherDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class WeatherRequest{
        private Float lat;    // 위도
        private Float lon;    // 경도
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class WeatherResponse{
        private String description;
        private Float temp;
        private Float temp_min;
        private Float temp_max;
        private Long humidity;
        private String date;
    }

    public static class FilterRequest{

    }
}
