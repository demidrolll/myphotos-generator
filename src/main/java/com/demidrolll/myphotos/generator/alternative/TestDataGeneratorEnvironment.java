package com.demidrolll.myphotos.generator.alternative;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Alternative
@Stereotype
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TestDataGeneratorEnvironment {
}
