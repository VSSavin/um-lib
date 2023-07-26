package com.github.vssavin.umlib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to ignore field.
 *
 * @author vssavin on 17.12.21
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface IgnoreField {
}
