package com.example.highfive.domain.category.domain;

import lombok.Getter;

@Getter
public enum Thick {

    매우_얇음(1.2, 0.0, 0.0),
    얇음(1.1,0.5, 1.0),  // 옷, 신발, 모자
    보통(1.0, 0.0, 0.0),
    두꺼움(0.9, 1.0, 2.0),
    매우_두꺼움(0.8, 0.0 ,0.0);

    private final double percent;
    private final double second;

    private final double third;

    Thick(double percent, double second, double third){
        this.percent = percent;
        this.second = second;
        this.third = third;
    }
}
