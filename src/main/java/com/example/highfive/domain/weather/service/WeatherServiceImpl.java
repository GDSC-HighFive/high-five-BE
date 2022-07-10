package com.example.highfive.domain.weather.service;

import com.example.highfive.domain.category.domain.Category;
import com.example.highfive.domain.category.domain.Fabric;
import com.example.highfive.domain.category.domain.Length;
import com.example.highfive.domain.category.domain.Thick;
import com.example.highfive.domain.weather.dto.Weather1;
import com.example.highfive.domain.weather.dto.Weather2;
import com.example.highfive.domain.weather.dto.Weather3;
import com.example.highfive.domain.weather.dto.WeatherDto;
import com.example.highfive.domain.weather.dto.WeatherDto.FilterRequest.SetData;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.Convert;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;
import static java.lang.Math.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final double LAT = 37.5665;
    private final double LON = 126.9780;

    private final int GIVEN_POINT_1 = 1;

    private final int GIVEN_POINT_2 = 2;

    private final int GIVEN_POINT_3 = 4;

    private final int NO_POINT = 0;

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
                    .rain(Double.parseDouble(result.get("pop").toString()))
                    .humidity(Integer.parseInt(main.get("humidity").toString()))
                    .temp_min(Double.parseDouble(main.get("temp_min").toString()))
                    .temp_max(Double.parseDouble(main.get("temp_max").toString()))
                    .date(result.get("dt_txt").toString().substring(0, 10))
                    .build());
        }
        return getDailyTemperature(resultList);
    }

    @Override
    public WeatherDto.FilterResponse recommendDate(WeatherDto.FilterRequest filterRequest) throws JSONException {
        // 1) 날씨 정보 모두 변환해서 저장
        Map<Integer, Double> totalMap = new HashMap<>();
        Map<String, List<String>> totalWeatherInfo = new HashMap<>();
        List<WeatherDto.WeatherResponse> regionWeatherInfo = getRegionWeatherInfo(WeatherDto.WeatherRequest.builder().lat(LAT).lon(LON).build());
        for(WeatherDto.WeatherResponse weather : regionWeatherInfo) {
            Weather1 weather1; Weather2 weather2; Weather3 weather3;

            Double rain = weather.getRain();
            rain = rain * 100;

            //1)날씨 1
            if (rain < 10 && weather.getDescription().equals("CLEAR")) {
                weather1 = Weather1.맑음;
            } else if (rain >= 10 && rain < 40 && weather.getDescription().equals("CLEAR")) {
                weather1 = Weather1.흐림;
            } else {
                weather1 = Weather1.비와눈;
            }

            //2)날씨 2
            int humidity = weather.getHumidity();
            if (humidity < 40) {
                weather2 = Weather2.건조;
            } else if (humidity < 60) {
                weather2 = Weather2.적정;
            } else {
                weather2 = Weather2.습함;
            }

            // 3)날씨 3
            //화씨 => 섭씨 온도 변환
            double min_temp = (32 * (weather.getTemp_min()) - 32) * 5/9;
            double max_temp = (32 * (weather.getTemp_max()) - 32) * 5/9;

            double average_temp = (max_temp - min_temp) / 2.0;
            if (average_temp < 6) {
                weather3 = Weather3.추움;
            } else if (average_temp >= 6 && average_temp < 20) {
                weather3 = Weather3.보통;
            } else {
                weather3 = Weather3.더움;
            }

            List<String> array = new ArrayList<>();
            array.add(weather1.name());
            array.add(weather2.name());
            array.add(weather3.name());
            array.add(weather.getTemp_max().toString());
            array.add(weather.getTemp_min().toString());
            array.add(weather.getDate());
            totalWeatherInfo.put(weather.getDate(), array);
        }

        //2)
        // 날씨 - 옷 비교
        List<SetData> dataSet = filterRequest.getSet();
        for(SetData data : dataSet){   //3개
            double result = 0.0;
            Category category = Category.valueOf(data.getCategory());
            Fabric fabric = Fabric.valueOf(data.getFabric());
            Length length = Length.valueOf(data.getLength());
            Thick thick = Thick.valueOf(data.getThick());

            for(List<String> arr : totalWeatherInfo.values()) {  //5개
                int point1 = NO_POINT;
                int point2 = NO_POINT;
                int point3 = NO_POINT;

                //1) 옷 소재 적합 여부 판단
                if (fabric.name().equals(Fabric.면.name())) {
                    point1 = GIVEN_POINT_1;
                    point2 = GIVEN_POINT_2;
                    point3 = GIVEN_POINT_3;
                } else if (fabric.name().equals(Fabric.린넨.name())) {
                    point1 = GIVEN_POINT_1;
                    point2 = GIVEN_POINT_2;
                    if (!arr.get(2).equals(Fabric.린넨.getWeather3().name())) {
                        point3 = GIVEN_POINT_3;
                    }
                } else if (fabric.name().equals(Fabric.울.name())) {
                    if (!arr.get(0).equals(Fabric.울.getWeather1().name())) {
                        point1 = GIVEN_POINT_1;
                    }
                    if (!arr.get(2).equals(Fabric.울.getWeather3().name())) {
                        point3 = GIVEN_POINT_3;
                    }
                    point2 = GIVEN_POINT_2;
                } else if (fabric.name().equals(Fabric.폴리에스테르.name())) {
                    if (!arr.get(1).equals(Fabric.폴리에스테르.getWeather2().name())) {
                        point2 = GIVEN_POINT_2;
                    }
                    point1 = GIVEN_POINT_1;
                    point3 = GIVEN_POINT_2;
                } else if (fabric.name().equals(Fabric.레이온.name())) {
                    if (!arr.get(2).equals(Fabric.레이온.getWeather3().name())) {
                        point3 = GIVEN_POINT_3;
                    }
                    point1 = GIVEN_POINT_1;
                    point2 = GIVEN_POINT_2;
                } else if (fabric.name().equals(Fabric.아크릴.name())) {
                    if (!arr.get(1).equals(Fabric.아크릴.getWeather2().name())) {
                        point2 = GIVEN_POINT_2;
                    }
                    if (!arr.get(2).equals(Fabric.아크릴.getWeather3().name())) {
                        point3 = GIVEN_POINT_3;
                    }
                    point1 = GIVEN_POINT_1;
                }

                // 얇기
                if(thick.name().equals(Thick.매우_얇음.name())){
                    if(!arr.get(0).equals(Thick.얇음.getWeather3().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(1).equals(Thick.얇음.getWeather3().name())){
                        point2 += GIVEN_POINT_2;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather1().name())){
                        point2 += GIVEN_POINT_3;
                    }
                }else if(thick.name().equals(Thick.얇음.name())){
                    if(!arr.get(0).equals(Thick.얇음.getWeather1().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather1().name())){
                        point3 += GIVEN_POINT_3;
                    }
                    point2 += GIVEN_POINT_2;
                }else if(thick.name().equals(Thick.두꺼움.name())){
                    if(!arr.get(0).equals(Thick.얇음.getWeather1().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(1).equals(Thick.얇음.getWeather1().name())){
                        point2 += GIVEN_POINT_2;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather3().name())){
                        point3 += GIVEN_POINT_3;
                    }
                }else if(thick.name().equals(Thick.매우_두꺼움.name())){
                    if(!arr.get(0).equals(Thick.얇음.getWeather1().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather3().name())){
                        point3 += GIVEN_POINT_3;
                    }
                    point2 += point2;
                }else{
                    point1 += GIVEN_POINT_1;
                    point2 += GIVEN_POINT_2;
                    point3 += GIVEN_POINT_3;
                }

                //옷 길이
                if(length.name().equals(Length.짧음.name())) {
                    if(!arr.get(0).equals(Length.짧음.getWeather3().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather1().name())){
                        point3 += GIVEN_POINT_3;
                    }
                    point2 += GIVEN_POINT_2;
                }else if(length.name().equals(Length.긺.name())){
                    if(!arr.get(0).equals(Length.짧음.getWeather1().name())){
                        point1 += GIVEN_POINT_1;
                    }
                    if(!arr.get(2).equals(Thick.얇음.getWeather3().name())){
                        point3 += GIVEN_POINT_3;
                    }
                    point2 += GIVEN_POINT_2;
                }else{
                    point1 += GIVEN_POINT_1;
                    point2 += GIVEN_POINT_2;
                    point3 += GIVEN_POINT_3;
                }

                String s = arr.get(5).replace("-", "");
                int newKey = Integer.parseInt(s);
                double total = (point1 + point2 + point3) / (double)(dataSet.size());
                totalMap.put(newKey, total);
            }
        }

        Object[] keyArray = totalMap.keySet().toArray();
        Arrays.sort(keyArray);
        log.info("keyArrayList = {}", keyArray.length);

        Map<Integer, Double> resultTempMap = new HashMap<>();

        for(int i = 0; i< keyArray.length/5 ; i++){
            double temp = 0.0;
            for(int j = i; j< keyArray.length; j+=5){

                temp += totalMap.get((totalMap.keySet().toArray())[i]);
            }
            Object object = (totalMap.keySet().toArray())[i];
            resultTempMap.put((int)object, temp);
            log.info("object={}", (int)object);
        }




        // value 값이 최대인데 같음 = 다 좋음  value 값이 최소인데 같음 = 다 나쁨
        // 나머지는 다 보통 8 7 6 5 4 3 3

//
//        totalWeatherInfo.keySet()
//        WeatherDto.FilterResponse.builder() //날짜별 데이터
//                .min_temp()
//                .max_temp()
//                .weather() //cloud
//                .status()   //총 결과
//                .date().build();   //현재 날짜
        return null;
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
                .rain(dataList.get(count-1).getRain())
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
                    .rain(dataList.get(i).getRain())
                    .temp_max(temp_max)
                    .temp_min(temp_min)
                    .humidity(humidity)
                    .date(dataList.get(i).getDate()).build());
            if (i >= dataList.size()) break;
        }
        return arrayList;
    }
}
