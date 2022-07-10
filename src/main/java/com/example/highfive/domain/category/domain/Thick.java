package com.example.highfive.domain.category.domain;

import com.example.highfive.domain.weather.dto.Weather1;
import com.example.highfive.domain.weather.dto.Weather2;
import com.example.highfive.domain.weather.dto.Weather3;
import lombok.Getter;


@Getter
public enum Thick {

    매우_얇음(Weather1.비와눈, Weather2.습함, Weather3.추움),
    얇음(Weather1.맑음, null, Weather3.추움),
    두꺼움(Weather1.맑음, null, Weather3.더움),
    매우_두꺼움(Weather1.맑음, Weather2.건조, Weather3.더움);

    private Weather1 weather1;
    private Weather2 weather2;
    private Weather3 weather3;

    Thick(Weather1 weather1, Weather2 weather2, Weather3 weather3){
        this.weather1 = weather1;
        this.weather2 = weather2;
        this.weather3 = weather3;
    }
}
