package com.example.highfive.domain.weather.controller;

import com.example.highfive.domain.weather.dto.WeatherDto;
import com.example.highfive.domain.weather.service.WeatherServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/weather")
@Slf4j
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherServiceImpl weatherServiceImpl;

    @PostMapping
    public List<WeatherDto.WeatherResponse> getWeatherInfo(@RequestBody WeatherDto.WeatherRequest weatherRequest) throws JSONException {
        return weatherServiceImpl.getRegionWeatherInfo(weatherRequest);
    }

    public WeatherDto.FilterResponse getRecommendedDate(@RequestBody WeatherDto.FilterRequest filterRequest){
        return weatherServiceImpl.recommendDate(filterRequest);
    }

}
