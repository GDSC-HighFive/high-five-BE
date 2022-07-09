package com.example.highfive.domain.weather.service;

import com.example.highfive.domain.weather.dto.WeatherDto;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

public interface WeatherService {
    List<WeatherDto.WeatherResponse> getRegionWeatherInfo(WeatherDto.WeatherRequest weatherRequest) throws JSONException;

    WeatherDto.FilterResponse recommendDate(WeatherDto.FilterRequest filterRequest);
}
