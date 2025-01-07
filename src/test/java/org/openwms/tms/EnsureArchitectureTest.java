/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.tms;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;
import org.ameba.annotation.TxService;
import org.slf4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * A EnsureArchitectureTest.
 *
 * @author Heiko Scherrer
 */
@AnalyzeClasses(packages = "org.openwms.tms", cacheMode = CacheMode.PER_CLASS, importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class,
        ImportOption.DoNotIncludeArchives.class
})
class EnsureArchitectureTest {

    @ArchTest
    public static final ArchRule verify_logger_definition =
            fields().that().haveRawType(Logger.class)
                    .should().bePrivate()
                    .andShould().beStatic()
                    .andShould().beFinal()
                    .because("This a an agreed convention")
            ;

    @ArchTest
    public static final ArchRule verify_api_package =
            classes().that()
                    .resideInAPackage("..tms.api..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("..tms.api..",
                            "org.openwms.common..",
                            "org.openwms.core..",
                            "java..", "javax..", "jakarta..",
                            "org.springframework..",
                            "com.."
                    )
                    .because("The API package is separated and the only package accessible by the client")
            ;

    @ArchTest
    public static final ArchRule verify_services =
            classes().that()
                    .areAnnotatedWith(TxService.class)
                    .or()
                    .areAnnotatedWith(Service.class)
                    .should()
                    .bePackagePrivate()
                    .andShould()
                    .resideInAnyPackage("..impl..", "..commands..", "..events..")
                    .because("By convention Transactional Services should only reside in internal packages")
            ;

    @ArchTest
    public static final ArchRule verify_transactional_repository_access =
            classes().that()
                    .areAnnotatedWith(Repository.class)
                    .or()
                    .areAssignableTo(JpaRepository.class)
                    .should()
                    .onlyHaveDependentClassesThat()
                    .areAnnotatedWith(TxService.class)
                    .orShould()
                    .onlyHaveDependentClassesThat()
                    .areAnnotatedWith(Transactional.class)
                    .because("A Repository must only be accessed in a transaction context")
            ;

    @ArchTest
    public static final ArchRule verify_repositories_are_package_private =
            classes().that()
                    .areAnnotatedWith(Repository.class)
                    .or()
                    .areAssignableTo(JpaRepository.class)
                    .should()
                    .bePackagePrivate()
                    .because("A Repository must only be accessed by a service")
            ;

    @ArchTest
    public static final ArchRule verify_no_cycles =
            slices().matching("..(*)..")
                    .should().beFreeOfCycles()
                    .because("For maintainability reasons")
            ;
}
