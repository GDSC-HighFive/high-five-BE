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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;

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
                    .description(weather1.get("main").toString())
                    .humidity(Integer.parseInt(main.get("humidity").toString()))
                    .temp_min(Double.parseDouble(main.get("temp_min").toString()))
                    .temp_max(Double.parseDouble(main.get("temp_max").toString()))
                    .date(result.get("dt_txt").toString().substring(0, 10))
                    .build());
        }
        return getDailyTemperature(resultList);
    }

    @Override
    public WeatherDto.FilterResponse recommendDate(WeatherDto.FilterRequest filterRequest) {
        return null;
        //하루의 최저 / 최고 온도 구하기
    }

    private List<WeatherDto.WeatherResponse> getDailyTemperature(List<WeatherDto.WeatherResponse> dataList){
        List<WeatherDto.WeatherResponse> arrayList = new ArrayList<>();
        int humidity = 0; int count = 0;
        double temp_min= Double.POSITIVE_INFINITY ; double temp_max = 0.0;

        String nextDate = LocalDate.now().plusDays(1).toString();
        for (WeatherDto.WeatherResponse weatherResponse : dataList) {
            if(Integer.parseInt(nextDate.substring(8,10)) !=
                    Integer.parseInt(weatherResponse.getDate().substring(8,10))){
                count++;
                temp_max = max(temp_max, weatherResponse.getTemp_max());
                temp_min = min(temp_min, weatherResponse.getTemp_min());
                humidity += weatherResponse.getHumidity();
            }else{
                break;
            }
        }

        humidity /= count;
        arrayList.add(WeatherDto.WeatherResponse.builder()    // 당일 데이터
                .description(dataList.get(0).getDescription())
                .temp_max(temp_max)
                .temp_min(temp_min)
                .humidity(humidity)
                .date(dataList.get(count-1).getDate()).build());


        for(int i = count; i < dataList.size() ; i+=7){
            humidity = 0; temp_min = Double.POSITIVE_INFINITY; temp_max= 0.0;
            for(int j = i ; j <= i+7 ; j++){
                if (j >= dataList.size()) break;
                temp_max = max(temp_max, dataList.get(j).getTemp_max());
                temp_min = min(temp_min, dataList.get(j).getTemp_min());
                humidity += dataList.get(j).getHumidity();
            }
            arrayList.add(WeatherDto.WeatherResponse.builder()    // 당일 데이터
                    .description(dataList.get(i).getDescription())
                    .temp_max(temp_max)
                    .temp_min(temp_min)
                    .humidity(humidity)
                    .date(dataList.get(i).getDate()).build());
            if (i >= dataList.size()) break;
        }
        return arrayList;
    }

    private void getMaxAndMinTemperatureInDaily(List<WeatherDto.WeatherResponse> dataList){

    }

}
