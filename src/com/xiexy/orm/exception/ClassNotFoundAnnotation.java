package com.xiexy.orm.exception;

public class ClassNotFoundAnnotation extends Exception {
    public ClassNotFoundAnnotation() {
        super("class's annotation isn't found");
    }
}
