// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.execution.sam

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.testFramework.MapDataContext
import com.intellij.testFramework.runInEdtAndWait
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import software.aws.toolkits.jetbrains.testutils.rules.JavaCodeInsightTestFixtureRule
import software.aws.toolkits.jetbrains.testutils.rules.openClass

class LambdaSamRunConfigurationProducerTest {
    @Rule
    @JvmField
    val projectRule = JavaCodeInsightTestFixtureRule()

    @Test
    fun validRunConfigurationIsCreated() {
        val psiClass = projectRule.fixture.openClass(
            """
            package com.example;

            public class LambdaHandler {
                public String handleRequest(String request) {
                    return request.toUpperCase();
                }
            }
            """
        )

        val lambdaMethod = psiClass.findMethodsByName("handleRequest", false).first()
        runInEdtAndWait {
            val runConfiguration = createRunConfiguration(lambdaMethod)
            assertThat(runConfiguration).isNotNull
            val configuration = runConfiguration?.configuration as SamRunConfiguration
            assertThat(configuration.getHandler()).isEqualTo("com.example.LambdaHandler::handleRequest")
            assertThat(configuration.name).isEqualTo("com.example.LambdaHandler::handleRequest")
        }
    }

    @Test
    fun invalidLambdaIsNotCreated() {
        val psiClass = projectRule.fixture.openClass(
            """
            package com.example;

            public class LambdaHandler {
                public void handleRequest() {
                }
            }
            """
        )

        val lambdaMethod = psiClass.findMethodsByName("handleRequest", false).first()
        runInEdtAndWait {
            val runConfiguration = createRunConfiguration(lambdaMethod)
            assertThat(runConfiguration).isNull()
        }
    }

    private fun createRunConfiguration(psiElement: PsiElement): ConfigurationFromContext? {
        val dataContext = MapDataContext()
        val context = createContext(psiElement, dataContext)
        val producer = RunConfigurationProducer.getInstance(LambdaSamRunConfigurationProducer::class.java)
        return producer.createConfigurationFromContext(context)
    }

    private fun createContext(psiClass: PsiElement, dataContext: MapDataContext): ConfigurationContext {
        dataContext.put(CommonDataKeys.PROJECT, projectRule.project)
        if (LangDataKeys.MODULE.getData(dataContext) == null) {
            dataContext.put(LangDataKeys.MODULE, ModuleUtilCore.findModuleForPsiElement(psiClass))
        }
        dataContext.put(Location.DATA_KEY, PsiLocation.fromPsiElement(psiClass))
        return ConfigurationContext.getFromContext(dataContext)
    }
}