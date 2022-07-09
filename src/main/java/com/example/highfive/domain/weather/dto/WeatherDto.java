package com.example.highfive.domain.weather.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WeatherDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class WeatherRequest{
        private Double lat;    // 위도
        private Double lon;    // 경도
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class WeatherResponse{
        private String description;
        private Double rain;
        private Double temp_min;
        private Double temp_max;
        private int humidity;
        private String date;
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FilterRequest{
        private final List<SetData> set = new ArrayList<>();

        @Getter
        @Setter
        public static class SetData{
            private String category;
            private String length;
            private String fabric;
            private String thick;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FilterResponse{
        private String date;
        private int recommendPercent;
    }
}
