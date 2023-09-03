package com.github.vssavin.umlib.config.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that method must be executed using
 * {@link com.github.vssavin.umlib.config.aspect.UmRoutingDatasourceAspect} aspect.
 *
 * @author vssavin on 02.09.2023
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmRouteDatasource {
}
