package org.devsmart.miniweb.handlers.controller;

import org.devsmart.miniweb.utils.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface RequestMapping {
    public String[] value() default {};
    public RequestMethod method() default RequestMethod.GET;
}
