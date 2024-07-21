/*
 * Copyright 2024 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.python.resolve.provider;

import com.google.common.collect.ImmutableSet;
import com.google.idea.blaze.base.BlazeTestCase;
import com.google.idea.blaze.base.ideinfo.*;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.settings.BuildSystemName;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.google.idea.blaze.base.sync.workspace.MockArtifactLocationDecoder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class AbstractPyImportResolverStrategyTest extends BlazeTestCase {

  /**
   * It is possible to specify import paths for a <code>py_...</code> target so that the modules in
   * the Python side can be specified. This test checks what happens in this situation.
   */
  @Test
  public void testBuildSourcesIndexWithAnImportRoot() {
    AbstractPyImportResolverStrategy strategy = new TestPyImportResolverStrategy();
    TargetMap targetMap = assembleTestTargetMap(Set.of("river"));

    Project project = Mockito.mock(Project.class);
    BlazeProjectData projectData = Mockito.mock(BlazeProjectData.class);

    Mockito.when(projectData.getTargetMap()).thenReturn(targetMap);
    Mockito.when(projectData.getArtifactLocationDecoder()).thenReturn(
        new MockArtifactLocationDecoder(new File("/workspaceroot"), false));

    // code under test
    PySourcesIndex actualSourcesIndex = strategy.buildSourcesIndex(project, projectData);

    // Verify the short names capture the right mapping to possible modules.
    Set<QualifiedName> shortNamesImports = actualSourcesIndex.shortNames.get("bar");
    assertThat(shortNamesImports).containsExactly(
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar"),
        // ^ this one is the default which is the complete path in the Bazel repo to the Python file
        QualifiedName.fromComponents("lib", "bar")
        // ^ this one is created from the target's `imports`.
    );

    // Verify that the mappings from possible modules to actual files works.
    // Because the strategy class was initialized with a special class to provide the PsiElement, we know that the
    // `toString()` method will return specific information that we can relay on in this test.

    assertThat(actualSourcesIndex.sourceMap).hasSize(4);
    // ^ the mapping to the Python file, and also it's parent with the two possible paths
    // one from the imports and one being the default.

    PsiManager manager = Mockito.mock(PsiManager.class);

    QualifiedName[] expectedBarModules = new QualifiedName[]{
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar"),
        QualifiedName.fromComponents("lib", "bar")
    };

    for (QualifiedName expectedBarModule : expectedBarModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedBarModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("whistle/foo/river/lib/bar.py");
    }

    // the parent dir of the Python file is also included in the mapping.

    QualifiedName[] expectedLibModules = new QualifiedName[]{
        QualifiedName.fromComponents("whistle", "foo", "river", "lib"),
        QualifiedName.fromComponents("lib"),
    };

    for (QualifiedName expectedLibModule : expectedLibModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedLibModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("whistle/foo/river/lib");
    }
  }

  /**
   * This is the case where there is no import roots supplied. In this case, we expect that the only
   * allowed Python module path will be the one from the root of the Bazel repo.
   */
  @Test
  public void testBuildSourcesIndexWithNoImportRoot() {
    AbstractPyImportResolverStrategy strategy = new TestPyImportResolverStrategy();
    TargetMap targetMap = assembleTestTargetMap(Set.of()); // <-- note empty

    Project project = Mockito.mock(Project.class);
    BlazeProjectData projectData = Mockito.mock(BlazeProjectData.class);

    Mockito.when(projectData.getTargetMap()).thenReturn(targetMap);
    Mockito.when(projectData.getArtifactLocationDecoder()).thenReturn(
        new MockArtifactLocationDecoder(new File("/workspaceroot"), false));

    // code under test
    PySourcesIndex actualSourcesIndex = strategy.buildSourcesIndex(project, projectData);

    // Verify the short names capture the right mapping to possible modules.
    Set<QualifiedName> shortNamesImports = actualSourcesIndex.shortNames.get("bar");
    assertThat(shortNamesImports).containsExactly(
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar")
        // ^ this one is the default which is the complete path in the Bazel repo to the Python file
    );

    // Verify that the mappings from possible modules to actual files works.
    // Because the strategy class was initialized with a special class to provide the PsiElement, we know that the
    // `toString()` method will return specific information that we can relay on in this test.

    assertThat(actualSourcesIndex.sourceMap).hasSize(2);
    // ^ the mapping to the Python file, and also it's parent.

    PsiManager manager = Mockito.mock(PsiManager.class);

    QualifiedName[] expectedBarModules = new QualifiedName[]{
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar"),
    };

    for (QualifiedName expectedBarModule : expectedBarModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedBarModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("whistle/foo/river/lib/bar.py");
    }

    // Check that the parent dir of the Python file is also included in the mapping.

    QualifiedName[] expectedLibModules = new QualifiedName[]{
        QualifiedName.fromComponents("whistle", "foo", "river", "lib"),
    };

    for (QualifiedName expectedLibModule : expectedLibModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedLibModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("whistle/foo/river/lib");
    }
  }

  /**
   * This is similar to {@link #testBuildSourcesIndexWithAnImportRoot()} but covers the case where
   * the import path is `.` to check that this will root the import statements from the top of the
   * Bazel project.
   */
  @Test
  public void testBuildSourcesIndexWithDotImportRoot() {
    AbstractPyImportResolverStrategy strategy = new TestPyImportResolverStrategy();
    TargetMap targetMap = assembleTestTargetMap(Set.of("."));

    Project project = Mockito.mock(Project.class);
    BlazeProjectData projectData = Mockito.mock(BlazeProjectData.class);

    Mockito.when(projectData.getTargetMap()).thenReturn(targetMap);
    Mockito.when(projectData.getArtifactLocationDecoder()).thenReturn(
        new MockArtifactLocationDecoder(new File("/workspaceroot"), false));

    // code under test
    PySourcesIndex actualSourcesIndex = strategy.buildSourcesIndex(project, projectData);

    // Verify the short names capture the right mapping to possible modules.
    Set<QualifiedName> shortNamesImports = actualSourcesIndex.shortNames.get("bar");
    assertThat(shortNamesImports).containsExactly(
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar"),
        // ^ this one is the default which is the complete path in the Bazel repo to the Python file
        QualifiedName.fromComponents("river", "lib", "bar")
        // ^ this one is created from the target's `imports`.
    );

    // Verify that the mappings from possible modules to actual files works.
    // Because the strategy class was initialized with a special class to provide the PsiElement, we know that the
    // `toString()` method will return specific information that we can relay on in this test.

    // Checks for the imports to the directory which is parent to the Python file have
    // been done in other tests and won't be repeated here.

    PsiManager manager = Mockito.mock(PsiManager.class);

    QualifiedName[] expectedBarModules = new QualifiedName[]{
        QualifiedName.fromComponents("whistle", "foo", "river", "lib", "bar"),
        QualifiedName.fromComponents("river", "lib", "bar")
    };

    for (QualifiedName expectedBarModule : expectedBarModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedBarModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("whistle/foo/river/lib/bar.py");
    }
  }

  /**
   * This case covers a special situation where the `BUILD.bazel` file is at the top of the repo.
   * This happens for a Python wheel and because the `BUILD.bazel` file is `/` we have to be careful
   * that we don't make reference to the top of the file system.
   */
  @Test
  public void testBuildSourcesIndexWithBuildAtRootAndImport() {
    AbstractPyImportResolverStrategy strategy = new TestPyImportResolverStrategy();

    PyIdeInfo.Builder pyIdeInfoBuilder = PyIdeInfo.builder()
        .addSources(ImmutableSet.of(source("top_level/lib/tea.py")))
        .addImports(ImmutableSet.copyOf(Set.of("top_level")));

    TargetMap targetMap = TargetMapBuilder.builder()
        .addTarget(
            TargetIdeInfo.builder()
                .setLabel("//:tea")
                .setBuildFile(source("/BUILD.bazel")) // <- note
                .setPyInfo(pyIdeInfoBuilder)
        )
        .build();

    Project project = Mockito.mock(Project.class);
    BlazeProjectData projectData = Mockito.mock(BlazeProjectData.class);

    Mockito.when(projectData.getTargetMap()).thenReturn(targetMap);
    Mockito.when(projectData.getArtifactLocationDecoder()).thenReturn(
        new MockArtifactLocationDecoder(new File("/workspaceroot"), false));

    // code under test
    PySourcesIndex actualSourcesIndex = strategy.buildSourcesIndex(project, projectData);

    // Verify the short names capture the right mapping to possible modules.
    Set<QualifiedName> shortNamesImports = actualSourcesIndex.shortNames.get("tea");
    assertThat(shortNamesImports).containsExactly(
        QualifiedName.fromComponents("top_level", "lib", "tea"),
        // ^ this one is the default which is the complete path in the Bazel repo to the Python file
        QualifiedName.fromComponents("lib", "tea")
        // ^ this one is created from the target's `imports`.
    );

    // Verify that the mappings from possible modules to actual files works.
    // Because the strategy class was initialized with a special class to provide the PsiElement, we know that the
    // `toString()` method will return specific information that we can relay on in this test.

    // Checks for the imports to the directory which is parent to the Python file have
    // been done in other tests and won't be repeated here.

    PsiManager manager = Mockito.mock(PsiManager.class);

    QualifiedName[] expectedBarModules = new QualifiedName[]{
        QualifiedName.fromComponents("top_level", "lib", "tea"),
        QualifiedName.fromComponents("lib", "tea")
    };

    for (QualifiedName expectedBarModule : expectedBarModules) {
      PsiElement fullElement = actualSourcesIndex.sourceMap.get(expectedBarModule).get(manager);
      assertThat(fullElement).isNotNull();
      assertThat(fullElement.toString()).isEqualTo("top_level/lib/tea.py");
    }
  }

  private TargetMap assembleTestTargetMap(Set<String> importPaths) {
    PyIdeInfo.Builder pyIdeInfoBuilder = PyIdeInfo.builder()
        .addSources(ImmutableSet.of(source("whistle/foo/river/lib/bar.py")))
        .addImports(ImmutableSet.copyOf(importPaths));

    return TargetMapBuilder.builder()
        .addTarget(
            TargetIdeInfo.builder()
                .setLabel("//whistle/foo:foo")
                .setBuildFile(source("whistle/foo/BUILD.bazel"))
                .setPyInfo(pyIdeInfoBuilder)
        )
        .build();
  }

  private static ArtifactLocation source(String relativePath) {
    return ArtifactLocation.builder().setRelativePath(relativePath).setIsSource(true).build();
  }

  private static class MockArtifactSupplierToPsiElementProviderMapper
      implements AbstractPyImportResolverStrategy.ArtifactSupplierToPsiElementProviderMapper {

    @Override
    public PsiElementProvider map(
        Project project, ArtifactLocationDecoder decoder, ArtifactLocation source) {
      return (manager) -> new MockArtifactLocationPsiElement(source.getExecutionRootRelativePath());
    }
  }

  /**
   * This is a concrete implementation of {@link AbstractPyImportResolverStrategy} so there is
   * something to test.
   */
  private static class TestPyImportResolverStrategy extends AbstractPyImportResolverStrategy {

    /**
     * This constructor will install a fake mapper to {@link PsiElementProvider} so that the results
     * can be captured to assert on.
     */
    public TestPyImportResolverStrategy() {
      super(new MockArtifactSupplierToPsiElementProviderMapper());
    }

    @Nullable
    @Override
    QualifiedName toImportString(ArtifactLocation source) {
      return fromRelativePath(source.getRelativePath());
    }

    @Nullable
    @Override
    public PsiElement resolveToWorkspaceSource(QualifiedName name,
        PyQualifiedNameResolveContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean appliesToBuildSystem(BuildSystemName buildSystemName) {
      throw new UnsupportedOperationException();
    }
  }

}
