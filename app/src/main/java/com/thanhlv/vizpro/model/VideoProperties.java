package com.thanhlv.vizpro.model;

public class VideoProperties {
    private String value;
    private Boolean check;

    public VideoProperties(String value, Boolean check) {
        this.value = value;
        this.check = check;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }
}
