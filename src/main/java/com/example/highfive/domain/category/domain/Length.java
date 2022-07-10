package com.example.highfive.domain.category.domain;

import com.example.highfive.domain.weather.dto.Weather1;
import com.example.highfive.domain.weather.dto.Weather2;
import com.example.highfive.domain.weather.dto.Weather3;
import lombok.Getter;

@Getter
public enum Length {
    짧음(Weather1.비와눈, null, Weather3.추움),
    보통(null, null, null),
    긺(Weather1.맑음, null, Weather3.더움);

    private Weather1 weather1;
    private Weather2 weather2;
    private Weather3 weather3;

    Length(Weather1 weather1, Weather2 weather2, Weather3 weather3){
        this.weather1 = weather1;
        this.weather2 = weather2;
        this.weather3 = weather3;
    }
}
