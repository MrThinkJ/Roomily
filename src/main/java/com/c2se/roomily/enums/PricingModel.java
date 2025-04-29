package com.c2se.roomily.enums;

public enum PricingModel {
    CPC("CPC", "Cost Per Click"),
    CPM("CPM", "Cost Per Mille");

    private final String code;
    private final String description;

    PricingModel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
