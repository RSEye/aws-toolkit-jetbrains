// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import software.aws.toolkits.jetbrains.core.explorer.SingleResourceNodeAction
import software.aws.toolkits.jetbrains.services.lambda.LambdaFunctionNode
import software.aws.toolkits.jetbrains.services.lambda.LambdaPackager
import software.aws.toolkits.jetbrains.services.lambda.runtimeGroup
import software.aws.toolkits.jetbrains.services.lambda.toDataClass
import software.aws.toolkits.jetbrains.services.lambda.upload.EditFunctionDialog
import software.aws.toolkits.jetbrains.services.lambda.upload.EditFunctionMode
import software.aws.toolkits.resources.message

abstract class UpdateFunctionAction(private val mode: EditFunctionMode, title: String) : SingleResourceNodeAction<LambdaFunctionNode>(title) {
    override fun actionPerformed(selected: LambdaFunctionNode, e: AnActionEvent) {
        val project = e.getRequiredData(PlatformDataKeys.PROJECT)

        // Fetch latest version just in case
        val functionConfiguration = selected.client.getFunction { it.functionName(selected.functionName()) }.configuration()

        val lambdaFunction = functionConfiguration.toDataClass(
            selected.function.credentialProviderId,
            selected.function.region
        )

        EditFunctionDialog(project, lambdaFunction, mode = mode).show()
    }
}

class UpdateFunctionConfigurationAction : UpdateFunctionAction(EditFunctionMode.UPDATE_CONFIGURATION, message("lambda.function.updateConfiguration.action"))

class UpdateFunctionCodeAction : UpdateFunctionAction(EditFunctionMode.UPDATE_CODE, message("lambda.function.updateCode.action")) {
    override fun update(selected: LambdaFunctionNode, e: AnActionEvent) {
        if (selected.function.runtime.runtimeGroup?.let { LambdaPackager.getInstance(it) } != null) {
            return
        }
        e.presentation.isVisible = false
    }
}