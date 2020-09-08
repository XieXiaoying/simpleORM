package com.xiexy.orm.define;

public class TextVal {
    String filed;
    String value;
    public static TextVal create() {
        return new TextVal();
    }

    public TextVal setText(String filed) {
        this.filed = filed;
        return this;
    }

    public TextVal setValue(String value) {
        if (value == null) {
            this.value = "null";
        } else {
            this.value = value;
        }
        return this;
    }

    public TextVal setValue(int value) {
        this.value = String.valueOf(value);
        return this;
    }

    public TextVal setValue(long value) {
        this.value = String.valueOf(value);
        return this;
    }

    public TextVal setValue(Double value) {
        this.value = String.valueOf(value);
        return this;
    }

    public TextVal setValue(Boolean value) {
        this.value = String.valueOf(value);
        return this;
    }

    public String getFiled() {
        return filed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TextVal [filed=" + filed + ", value=" + value + "]";
    }
}
