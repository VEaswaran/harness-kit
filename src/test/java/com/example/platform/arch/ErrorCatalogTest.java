package com.example.platform.arch;

import com.example.platform.error.ErrorCode;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Keeps Java error enums and error-codes.yaml identical (the wiki catalog is generated from the YAML). */
class ErrorCatalogTest {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{3}-[SBP]-\\d{4}$");

    @Test
    @SuppressWarnings("unchecked")
    void java_codes_and_catalog_match() throws Exception {
        Map<String, Map<String, Object>> catalog;
        try (InputStream in = getClass().getResourceAsStream("/error-codes.yaml")) {
            assertThat(in).as("error-codes.yaml on classpath").isNotNull();
            catalog = (Map<String, Map<String, Object>>) ((Map<String, Object>) new Yaml().load(in)).get("codes");
        }

        Map<String, ErrorCode> javaCodes = new HashMap<>();
        List<String> problems = new ArrayList<>();

        for (JavaClass c : new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.platform")) {
            if (!c.isEnum() || !c.isAssignableTo(ErrorCode.class)) continue;
            for (Object constant : Class.forName(c.getName()).getEnumConstants()) {
                ErrorCode ec = (ErrorCode) constant;
                if (javaCodes.put(ec.code(), ec) != null) problems.add("Duplicate code in Java: " + ec.code());
            }
        }

        javaCodes.forEach((code, ec) -> {
            if (!FORMAT.matcher(code).matches()) problems.add("Bad format: " + code);
            Map<String, Object> entry = catalog.get(code);
            if (entry == null) {
                problems.add("Missing from error-codes.yaml: " + code);
                return;
            }
            if (!ec.category().name().equals(entry.get("category")))
                problems.add(code + " category " + entry.get("category") + " != " + ec.category());
            if (ec.status().value() != ((Number) entry.get("http")).intValue())
                problems.add(code + " http " + entry.get("http") + " != " + ec.status().value());
        });

        catalog.keySet().stream()
                .filter(code -> !javaCodes.containsKey(code))
                .forEach(code -> problems.add("In YAML but no Java constant: " + code));

        assertThat(problems).isEmpty();
    }
}
