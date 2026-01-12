package com.jelvin.neopivot;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * 架构约束测试（ArchUnit）。
 *
 * <p>用于守护包结构与依赖方向，避免随着功能扩展出现不可控的耦合。
 *
 * @author Jelvin
 */
public class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.jelvin.neopivot";

    private static JavaClasses importAll() {
        return new ClassFileImporter().importPackages(BASE_PACKAGE);
    }

    @Test
    void apiShouldNotDependOnPersistence() {
        JavaClasses classes = importAll();
        noClasses().that().resideInAPackage("..api..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..persistence..")
                .check(classes);
    }

    @Test
    void persistenceShouldNotDependOnApi() {
        JavaClasses classes = importAll();
        noClasses().that().resideInAPackage("..persistence..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..api..")
                .check(classes);
    }

    @Test
    void commonShouldNotDependOnBusinessModules() {
        JavaClasses classes = importAll();
        noClasses().that().resideInAPackage("..common..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..auth..",
                        "..storage..",
                        "..document..",
                        "..chat..",
                        "..ai..",
                        "..search..",
                        "..platform..")
                .check(classes);
    }
}

