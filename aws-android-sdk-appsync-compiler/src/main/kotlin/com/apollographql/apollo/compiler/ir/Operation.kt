/**
 * Copyright 2018-2019 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *     http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.apollographql.apollo.compiler.ir

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.compiler.SchemaTypeSpecBuilder
import com.apollographql.apollo.compiler.withBuilder
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

data class Operation(
    val operationName: String,
    val operationType: String,
    val variables: List<Variable>,
    val source: String,
    val fields: List<Field>,
    val filePath: String,
    val fragmentsReferenced: List<String>,
    val operationId: String

) : CodeGenerator {
  override fun toTypeSpec(context: CodeGenerationContext): TypeSpec =
      SchemaTypeSpecBuilder(
          typeName = DATA_TYPE_NAME,
          fields = fields,
          fragmentSpreads = emptyList(),
          inlineFragments = emptyList(),
          context = context
      )
          .build(Modifier.PUBLIC, Modifier.STATIC)
          .toBuilder()
          .addSuperinterface(Operation.Data::class.java)
          .build()
          .let {
            if (context.generateModelBuilder) {
              it.withBuilder()
            } else {
              it
            }
          }

    fun normalizedOperationName(useSemanticNaming: Boolean): String = when (operationType) {
        TYPE_MUTATION -> normalizedOperationName(useSemanticNaming, "Mutation")
        TYPE_QUERY -> normalizedOperationName(useSemanticNaming, "Query")
        TYPE_SUBSCRIPTION -> normalizedOperationName(useSemanticNaming, "Subscription")
        else -> throw IllegalArgumentException("Unknown operation type $operationType")
    }

    private fun normalizedOperationName(useSemanticNaming: Boolean, operationNameSuffix: String): String {
        return if (useSemanticNaming && !operationName.endsWith(operationNameSuffix)) {
            operationName.capitalize() + operationNameSuffix
        } else {
            operationName.capitalize()
        }
    }

    fun isMutation() = operationType == TYPE_MUTATION

    fun isQuery() = operationType == TYPE_QUERY

    fun isSubscription() = operationType == TYPE_SUBSCRIPTION

  companion object {
    val DATA_TYPE_NAME = "Data"
    val TYPE_MUTATION = "mutation"
    val TYPE_QUERY = "query"
    val TYPE_SUBSCRIPTION = "subscription"
  }
}
