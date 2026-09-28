package com.example.platform.trace;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links code to a Confluence requirement, e.g. {@code @Requirement("REQ-ORD-012")}.
 * Used by the design-verifier agent (WS2) and enforced by ArchitectureTest.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Requirement {
    String[] value();
}
