package com.example.highfive.domain.category.domain;

public enum Category {

    상의1(1.0), 상의2(0.6), 상의3(0.45), 하의(0.0),
    신발(0.0), 모자(0.0);

    private final double weight;

    Category(double weight){
        this.weight = weight;
    }
}
