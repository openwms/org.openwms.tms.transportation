/*
 * Copyright 2005-2019 the original author or authors.
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
import com.tngtech.archunit.lang.ArchRule;
import org.ameba.annotation.TxService;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * A EnsureArchitectureIT.
 *
 * @author Heiko Scherrer
 */
@AnalyzeClasses(packages = "org.openwms.tms", importOptions = {ImportOption.DoNotIncludeTests.class})
class EnsureArchitectureIT {

    @ArchTest
    public static final ArchRule verify_api_package =
            classes().that()
                    .resideInAPackage("..tms.api..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("..tms.api..", "org.openwms.core..", "org.openwms.common..", "java..", "org.springframework..")
            ;

    @ArchTest
    public static final ArchRule rule1 =
            noClasses().that()
                    .resideInAPackage("..tms.")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..tms.app..", "..tms.commands..", "..tms.events..", "..tms.impl..", "..tms.inmem..")
            ;

    @ArchTest
    public static final ArchRule verify_services =
            classes().that()
                    .areAnnotatedWith(TxService.class)
                    .or()
                    .areAnnotatedWith(Service.class)
                    .should()
                    .bePackagePrivate()
                    .andShould().resideInAnyPackage("..impl..", "..commands..", "..events..")
            ;

    @ArchTest
    public static final ArchRule cycles =
            slices().matching("org.openwms.(*)..").should().beFreeOfCycles();
}
