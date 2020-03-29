/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl.accessors

import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.ExtensionAware

import org.gradle.kotlin.dsl.support.unsafeLazy

import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens


internal
data class AccessorScope(
    private val targetTypesByName: HashMap<AccessorNameSpec, HashSet<TypeAccessibility.Accessible>> = hashMapOf()
) {
    fun uniqueAccessorsFor(entries: Iterable<ProjectSchemaEntry<TypeAccessibility>>): Sequence<TypedAccessorSpec> =
        uniqueAccessorsFrom(entries.asSequence().mapNotNull(::typedAccessorSpec))

    fun uniqueAccessorsFrom(accessorSpecs: Sequence<TypedAccessorSpec>): Sequence<TypedAccessorSpec> =
        accessorSpecs.filter(::add)

    private
    fun add(accessorSpec: TypedAccessorSpec) =
        targetTypesOf(accessorSpec.name).add(accessorSpec.receiver)

    private
    fun targetTypesOf(accessorNameSpec: AccessorNameSpec): HashSet<TypeAccessibility.Accessible> =
        targetTypesByName.computeIfAbsent(accessorNameSpec) { hashSetOf() }
}


internal
fun extensionAccessor(spec: TypedAccessorSpec): String = spec.run {
    when (type) {
        is TypeAccessibility.Accessible -> accessibleExtensionAccessorFor(receiver.type.kotlinString, name, type.type.kotlinString)
        is TypeAccessibility.Inaccessible -> inaccessibleExtensionAccessorFor(receiver.type.kotlinString, name, type)
    }
}


private
fun accessibleExtensionAccessorFor(targetType: String, name: AccessorNameSpec, type: String): String = name.run {
    """
        /**
         * Retrieves the [$original][$type] extension.
         */
        val $targetType.`$kotlinIdentifier`: $type get() =
            $thisExtensions.getByName("$stringLiteral") as $type

        /**
         * Configures the [$original][$type] extension.
         */
        fun $targetType.`$kotlinIdentifier`(configure: $type.() -> Unit): Unit =
            $thisExtensions.configure("$stringLiteral", configure)
    """
}


private
fun inaccessibleExtensionAccessorFor(targetType: String, name: AccessorNameSpec, typeAccess: TypeAccessibility.Inaccessible): String = name.run {
    """
        /**
         * Retrieves the `$original` extension.
         *
         * ${documentInaccessibilityReasons(name, typeAccess)}
         */
        val $targetType.`$kotlinIdentifier`: Any get() =
            $thisExtensions.getByName("$stringLiteral")

        /**
         * Configures the `$original` extension.
         *
         * ${documentInaccessibilityReasons(name, typeAccess)}
         */
        fun $targetType.`$kotlinIdentifier`(configure: Any.() -> Unit): Unit =
            $thisExtensions.configure("$stringLiteral", configure)

    """
}


internal
fun conventionAccessor(spec: TypedAccessorSpec): String = spec.run {
    when (type) {
        is TypeAccessibility.Accessible -> accessibleConventionAccessorFor(receiver.type.kotlinString, name, type.type.kotlinString)
        is TypeAccessibility.Inaccessible -> inaccessibleConventionAccessorFor(receiver.type.kotlinString, name, type)
    }
}


private
fun accessibleConventionAccessorFor(targetType: String, name: AccessorNameSpec, type: String): String = name.run {
    """
        /**
         * Retrieves the [$original][$type] convention.
         */
        val $targetType.`$kotlinIdentifier`: $type get() =
            $thisConvention.getPluginByName<$type>("$stringLiteral")

        /**
         * Configures the [$original][$type] convention.
         */
        fun $targetType.`$kotlinIdentifier`(configure: $type.() -> Unit): Unit =
            configure(`$stringLiteral`)

    """
}


private
fun inaccessibleConventionAccessorFor(targetType: String, name: AccessorNameSpec, typeAccess: TypeAccessibility.Inaccessible): String = name.run {
    """
        /**
         * Retrieves the `$original` convention.
         *
         * ${documentInaccessibilityReasons(name, typeAccess)}
         */
        val $targetType.`$kotlinIdentifier`: Any get() =
            $thisConvention.getPluginByName<Any>("$stringLiteral")

        /**
         * Configures the `$original` convention.
         *
         * ${documentInaccessibilityReasons(name, typeAccess)}
         */
        fun $targetType.`$kotlinIdentifier`(configure: Any.() -> Unit): Unit =
            configure(`$stringLiteral`)

    """
}


internal
fun existingTaskAccessor(spec: TypedAccessorSpec): String = spec.run {
    when (type) {
        is TypeAccessibility.Accessible -> accessibleExistingTaskAccessorFor(name, type.type.kotlinString)
        is TypeAccessibility.Inaccessible -> inaccessibleExistingTaskAccessorFor(name, type)
    }
}


private
fun accessibleExistingTaskAccessorFor(name: AccessorNameSpec, type: String): String = name.run {
    """
        /**
         * Provides the existing [$original][$type] task.
         */
        val TaskContainer.`$kotlinIdentifier`: TaskProvider<$type>
            get() = named<$type>("$stringLiteral")

    """
}


private
fun inaccessibleExistingTaskAccessorFor(name: AccessorNameSpec, typeAccess: TypeAccessibility.Inaccessible): String = name.run {
    """
        /**
         * Provides the existing `$original` task.
         *
         * ${documentInaccessibilityReasons(name, typeAccess)}
         */
        val TaskContainer.`$kotlinIdentifier`: TaskProvider<Task>
            get() = named("$stringLiteral")

    """
}


internal
fun existingContainerElementAccessor(spec: TypedAccessorSpec): String = spec.run {
    when (type) {
        is TypeAccessibility.Accessible -> accessibleExistingContainerElementAccessorFor(receiver.type.kotlinString, name, type.type.kotlinString)
        is TypeAccessibility.Inaccessible -> inaccessibleExistingContainerElementAccessorFor(receiver.type.kotlinString, name, type)
    }
}


private
fun accessibleExistingContainerElementAccessorFor(targetType: String, name: AccessorNameSpec, type: String): String = name.run {
    """
        /**
         * Provides the existing [$original][$type] element.
         */
        val $targetType.`$kotlinIdentifier`: NamedDomainObjectProvider<$type>
            get() = named<$type>("$stringLiteral")

    """
}


private
fun inaccessibleExistingContainerElementAccessorFor(containerType: String, name: AccessorNameSpec, elementType: TypeAccessibility.Inaccessible): String = name.run {
    """
        /**
         * Provides the existing `$original` element.
         *
         * ${documentInaccessibilityReasons(name, elementType)}
         */
        val $containerType.`$kotlinIdentifier`: NamedDomainObjectProvider<Any>
            get() = named("$stringLiteral")

    """
}


private
val thisExtensions =
    "(this as ${ExtensionAware::class.java.name}).extensions"


private
val thisConvention =
    "((this as? Project)?.convention ?: (this as ${HasConvention::class.java.name}).convention)"


internal
data class AccessorNameSpec(val original: String) {

    val kotlinIdentifier
        get() = original

    val stringLiteral by unsafeLazy {
        stringLiteralFor(original)
    }
}


internal
data class TypedAccessorSpec(
    val receiver: TypeAccessibility.Accessible,
    val name: AccessorNameSpec,
    val type: TypeAccessibility
)


private
fun stringLiteralFor(original: String) =
    escapeStringTemplateDollarSign(original)


private
fun escapeStringTemplateDollarSign(string: String) =
    string.replace("${'$'}", "${'$'}{'${'$'}'}")


private
fun accessorNameSpec(originalName: String) =
    AccessorNameSpec(originalName)


internal
fun typedAccessorSpec(schemaEntry: ProjectSchemaEntry<TypeAccessibility>) =
    schemaEntry.takeIf { isLegalAccessorName(it.name) }?.target?.run {
        when (this) {
            is TypeAccessibility.Accessible ->
                TypedAccessorSpec(this, accessorNameSpec(schemaEntry.name), schemaEntry.type)
            is TypeAccessibility.Inaccessible ->
                null
        }
    }


private
fun documentInaccessibilityReasons(name: AccessorNameSpec, typeAccess: TypeAccessibility.Inaccessible): String =
    "`${name.kotlinIdentifier}` is not accessible in a type safe way because:\n${typeAccess.reasons.joinToString("\n") { reason ->
        "         * - ${reason.explanation}"
    }}"


internal
fun isLegalAccessorName(name: String): Boolean =
    isKotlinIdentifier("`$name`")
        && name.indexOfAny(invalidNameChars) < 0


private
val invalidNameChars = charArrayOf('.', '/', '\\')


private
fun isKotlinIdentifier(candidate: String): Boolean =
    KotlinLexer().run {
        start(candidate)
        tokenStart == 0
            && tokenEnd == candidate.length
            && tokenType == KtTokens.IDENTIFIER
    }
