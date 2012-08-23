package com.heymoose.infrastructure.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Cacheable {

  /**
   * Period in ISO format, e.g. "P1D" or "P1W3D".
   * @return period in ISO format
   */
  String period() default "P1D";
}
