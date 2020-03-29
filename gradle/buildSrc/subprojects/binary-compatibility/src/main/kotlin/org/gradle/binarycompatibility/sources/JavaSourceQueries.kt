/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.binarycompatibility.sources

import japicmp.model.JApiClass
import japicmp.model.JApiCompatibility
import japicmp.model.JApiConstructor
import japicmp.model.JApiField
import japicmp.model.JApiMethod

import com.github.javaparser.ast.body.AnnotationDeclaration
import com.github.javaparser.ast.body.AnnotationMemberDeclaration
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
import com.github.javaparser.ast.visitor.GenericVisitorAdapter

import org.gradle.binarycompatibility.jApiClass
import org.gradle.binarycompatibility.simpleName


internal
object JavaSourceQueries {

    fun isOverrideMethod(method: JApiMethod): JavaSourceQuery<Boolean> =
        JavaSourceQuery(
            false,
            object : GenericVisitorAdapter<Boolean, Unit?>() {
                override fun visit(declaration: MethodDeclaration, arg: Unit?): Boolean? {
                    if (declaration.name?.asString() == method.name && declaration.hasOverrideAnnotation) {
                        return true
                    }
                    return null
                }
            }
        )

    fun isSince(version: String, member: JApiCompatibility): JavaSourceQuery<Boolean> =
        member.jApiClass.simpleName.let { declaringClassSimpleName ->
            JavaSourceQuery(
                false,
                when (member) {
                    is JApiClass -> isSinceJavaClassVisitorFor(declaringClassSimpleName, version)
                    is JApiField -> isSinceJavaFieldVisitorFor(member, declaringClassSimpleName, version)
                    is JApiConstructor -> isSinceJavaConstructorVisitorFor(member, declaringClassSimpleName, version)
                    is JApiMethod -> isSinceJavaMethodVisitorFor(member, declaringClassSimpleName, version)
                    else -> throw IllegalStateException("Unsupported japicmp member type ${member::class}")
                }
            )
        }
}


private
val MethodDeclaration.hasOverrideAnnotation: Boolean
    get() = annotations?.any { it.name.asString() == Override::class.simpleName } == true


private
fun isSinceJavaClassVisitorFor(classSimpleName: String, version: String) =
    NameAndSinceMatchingVisitor(classSimpleName, version)


private
fun isSinceJavaFieldVisitorFor(field: JApiField, classSimpleName: String, version: String) =
    object : NameAndSinceMatchingVisitor(classSimpleName, version) {

        override fun visit(declaration: FieldDeclaration, arg: Unit?): Boolean? =
            if (matchesName(declaration.fieldName, field.name) && declaration.isSince(version)) true
            else null

        override fun visit(declaration: EnumConstantDeclaration, arg: Unit?): Boolean? =
            if (declaration.matchesNameAndIsSince(field.name, version)) true
            else null
    }


private
fun isSinceJavaConstructorVisitorFor(constructor: JApiConstructor, classSimpleName: String, version: String) =
    object : NameAndSinceMatchingVisitor(classSimpleName, version) {

        override fun visit(declaration: ConstructorDeclaration, arg: Unit?): Boolean? =
            if (declaration.matchesNameAndIsSince(classSimpleName, version)) true
            else super.visit(declaration, arg)

        override fun visit(declaration: FieldDeclaration, arg: Unit?): Boolean? =
            if (matchesName(declaration.fieldName, constructor.name) && declaration.isSince(version)) true
            else null

        override fun visit(declaration: EnumConstantDeclaration, arg: Unit?): Boolean? =
            if (declaration.matchesNameAndIsSince(constructor.name, version)) true
            else null
    }


private
fun isSinceJavaMethodVisitorFor(method: JApiMethod, classSimpleName: String, version: String) =
    object : NameAndSinceMatchingVisitor(classSimpleName, version) {

        override fun visit(declaration: AnnotationMemberDeclaration, arg: Unit?): Boolean? =
            if (declaration.matchesNameAndIsSince(method.name, version)) true
            else super.visit(declaration, arg)

        override fun visit(declaration: MethodDeclaration, arg: Unit?): Boolean? =
            if (declaration.matchesNameAndIsSince(method.name, version)) true
            else null
    }


private
open class NameAndSinceMatchingVisitor(
    val classSimpleName: String,
    val version: String
) : GenericVisitorAdapter<Boolean, Unit?>() {

    override fun visit(declaration: ClassOrInterfaceDeclaration, arg: Unit?): Boolean? =
        if (declaration.matchesNameAndIsSince(classSimpleName, version)) true
        else super.visit(declaration, arg)

    override fun visit(declaration: AnnotationDeclaration, arg: Unit?): Boolean? =
        if (declaration.matchesNameAndIsSince(classSimpleName, version)) true
        else super.visit(declaration, arg)

    override fun visit(declaration: EnumDeclaration, arg: Unit?): Boolean? =
        if (declaration.matchesNameAndIsSince(classSimpleName, version)) true
        else super.visit(declaration, arg)
}


private
fun <T> T.matchesNameAndIsSince(candidateName: String, version: String): Boolean where T : BodyDeclaration<*>, T : NodeWithSimpleName<*> =
    takeIf { matchesName(it.name.asString(), candidateName) }?.isSince(version) == true


private
fun matchesName(name: String, candidateName: String) =
    name == candidateName.replace(".*\\$".toRegex(), "") // strip outer class names


private
fun BodyDeclaration<*>.isSince(version: String): Boolean =
    comment?.orElse(null)?.content?.contains("@since $version") == true


private
val FieldDeclaration.fieldName: String
    get() = variables.first().name.asString()
