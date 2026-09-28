package com.example.platform.arch;

import com.example.platform.trace.Requirement;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.codeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

/** Build-time enforcement of the error and design standards (the same rules WS2/WS4 review). */
@AnalyzeClasses(packages = "com.example.platform", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** Throw System/Business/ProjectException, never raw Exception/RuntimeException/Throwable. */
    @ArchTest
    static final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    /** Only the global handler may catch Exception / Throwable / RuntimeException. */
    @ArchTest
    static final ArchRule no_catch_all = codeUnits()
            .that().areDeclaredInClassesThat().areNotAnnotatedWith(RestControllerAdvice.class)
            .should(notCatchGenericExceptions())
            .allowEmptyShould(true);

    /** Controllers go through services, never straight to repositories. */
    @ArchTest
    static final ArchRule controllers_do_not_use_repositories = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAssignableTo(org.springframework.data.repository.Repository.class)
            .allowEmptyShould(true);

    /** Every public domain service method is traceable to a Confluence requirement. */
    @ArchTest
    static final ArchRule domain_services_are_traceable = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
            .and().arePublic()
            .should().beAnnotatedWith(Requirement.class)
            .orShould().beDeclaredInClassesThat().areAnnotatedWith(Requirement.class)
            .allowEmptyShould(true);

    private static ArchCondition<JavaCodeUnit> notCatchGenericExceptions() {
        return new ArchCondition<>("not catch Exception, RuntimeException or Throwable") {
            @Override
            public void check(JavaCodeUnit unit, ConditionEvents events) {
                unit.getTryCatchBlocks().forEach(block ->
                        block.getCaughtThrowables().stream()
                                .map(JavaClass::getName)
                                .filter(n -> n.equals("java.lang.Exception")
                                        || n.equals("java.lang.RuntimeException")
                                        || n.equals("java.lang.Throwable"))
                                .forEach(n -> events.add(SimpleConditionEvent.violated(unit,
                                        unit.getFullName() + " catches " + n + " at " + block.getSourceCodeLocation()))));
            }
        };
    }
}
