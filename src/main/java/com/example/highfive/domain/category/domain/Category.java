package com.example.highfive.domain.category.domain;

import lombok.Getter;

@Getter
public enum Category {

    상의1(1, 1.0), 상의2(2,0.6), 상의3(3,0.45), 하의(0,0.0);

    private final int count;
    private final double weight;

    Category(int count, double weight){
        this.count = count;
        this.weight = weight;
    }
}
