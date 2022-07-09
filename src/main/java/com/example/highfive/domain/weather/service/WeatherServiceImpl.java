package com.example.highfive.domain.weather.service;

import com.example.highfive.domain.weather.dto.WeatherDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Value("${secret.api-key}")
    private String SECRET_API_KEY;

    @Value("${secret.url}")
    private String REQUEST_URI;

    @Override
    public List<WeatherDto.WeatherResponse> getRegionWeatherInfo(WeatherDto.WeatherRequest weatherRequest) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);

        UriComponents uri = UriComponentsBuilder.fromHttpUrl(REQUEST_URI)
                .queryParam("lat", weatherRequest.getLat())
                .queryParam("lon", weatherRequest.getLon())
                .queryParam("appid", SECRET_API_KEY).build(false);

        String response= restTemplate.exchange(uri.toString(), HttpMethod.GET, httpEntity, String.class).getBody();
        JSONObject object = new JSONObject(response);
        JSONArray total_forecast = (JSONArray) object.get("list");

        List<WeatherDto.WeatherResponse> resultList = new ArrayList<>();
        for(int i = 0 ; i < total_forecast.length() ; i++){
            JSONObject result = (JSONObject) total_forecast.get(i);
            JSONArray weather = (JSONArray) result.get("weather");
            JSONObject weather1 = (JSONObject) weather.get(0);
            JSONObject main = (JSONObject) result.get("main");

            resultList.add(WeatherDto.WeatherResponse.builder()
                    .description(weather1.get("description").toString())
                    .humidity(Long.parseLong(main.get("humidity").toString()))
                    .temp(Float.parseFloat(main.get("temp").toString()))
                    .temp_min(Float.parseFloat(main.get("temp_min").toString()))
                    .temp_max(Float.parseFloat(main.get("temp_max").toString()))
                    .date(result.get("dt_txt").toString())
                    .build());
        }
        return resultList;
    }

}
