package com.example.highfive.domain.category.domain;

import com.example.highfive.domain.weather.dto.Weather1;
import com.example.highfive.domain.weather.dto.Weather2;
import com.example.highfive.domain.weather.dto.Weather3;
import lombok.Getter;


@Getter
public enum Fabric {
    면(null, null, null),
    린넨(null, null, Weather3.추움),
    울(Weather1.비와눈, null, Weather3.더움),
    폴리에스테르(null, Weather2.습함, null),
    레이온(null, null, Weather3.더움),
    나일론(null, Weather2.습함, Weather3.추움),
    아크릴(null, Weather2.습함, Weather3.더움);

    private Weather1 weather1;
    private Weather2 weather2;
    private Weather3 weather3;

    Fabric(Weather1 weather1, Weather2 weather2, Weather3 weather3){
        this.weather1 = weather1;
        this.weather2 = weather2;
        this.weather3 = weather3;
    }
}
