/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.base.lang.buildfile;

import com.google.common.base.Joiner;
import com.google.idea.blaze.base.BlazeIntegrationTestCase;
import com.google.idea.blaze.base.EditorTestHelper;
import com.google.idea.blaze.base.MockProjectViewManager;
import com.google.idea.blaze.base.ExternalWorkspaceFixture;
import com.google.idea.blaze.base.lang.buildfile.psi.BuildFile;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.ExternalWorkspaceData;
import com.google.idea.blaze.base.model.MockBlazeProjectDataBuilder;
import com.google.idea.blaze.base.model.MockBlazeProjectDataManager;
import com.google.idea.blaze.base.model.primitives.ExternalWorkspace;
import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.projectview.ProjectViewManager;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.google.idea.blaze.base.sync.workspace.WorkspaceHelper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.junit.Before;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** BUILD file specific integration test base */
public abstract class BuildFileIntegrationTestCase extends BlazeIntegrationTestCase {
  protected EditorTestHelper editorTest;

  @Before
  public final void doSetup() {
    BlazeProjectDataManager mockProjectDataManager =
        new MockBlazeProjectDataManager(
            MockBlazeProjectDataBuilder.builder(workspaceRoot)
                .setOutputBase(fileSystem.getRootDir() + "/output_base")
                .setExternalWorkspaceData(mockExternalWorkspaceData())
                .build());
    registerProjectService(BlazeProjectDataManager.class, mockProjectDataManager);
    registerProjectService(ProjectViewManager.class, new MockProjectViewManager());
    editorTest = new EditorTestHelper(getProject(), testFixture);
  }

  protected ExternalWorkspaceData mockExternalWorkspaceData() {
    return ExternalWorkspaceData.EMPTY;
  }

  /**
   * Creates a file with the specified contents and file path in the test project, and asserts that
   * it's parsed as a BuildFile
   */
  protected BuildFile createBuildFile(WorkspacePath workspacePath, String... contentLines) {
    PsiFile file = workspace.createPsiFile(workspacePath, contentLines);
    assertThat(file).isInstanceOf(BuildFile.class);
    return (BuildFile) file;
  }

  protected BuildFile createBuildFileWithCaret(WorkspacePath workspacePath, String... contentLines) {
    PsiFile file = workspace.createPsiFile(workspacePath, contentLines);
    assertThat(file).isInstanceOf(BuildFile.class);
    testFixture.configureFromExistingVirtualFile(file.getVirtualFile());
    return (BuildFile) file;
  }

  protected void assertFileContents(VirtualFile file, String... contentLines) {
    assertFileContents(fileSystem.getPsiFile(file), contentLines);
  }

  protected void assertFileContents(PsiFile file, String... contentLines) {
    String contents = Joiner.on('\n').join(contentLines);
    assertThat(file.getText()).isEqualTo(contents);
  }

  protected void assertFileContents(PsiFile file, List<String> contentLines) {
    String contents = Joiner.on('\n').join(contentLines);
    assertThat(file.getText()).isEqualTo(contents);
  }

  protected PsiFile createFileInExternalWorkspace(
      String workspaceName, WorkspacePath path, String... contents) {
    String filePath =
        Paths.get(getExternalSourceRoot().getPath(), workspaceName, path.relativePath()).toString();
    return fileSystem.createPsiFile(filePath, contents);
  }

  protected File getExternalSourceRoot() {
    BlazeProjectData blazeProjectData = BlazeProjectDataManager.getInstance(getProject()).getBlazeProjectData();
    assertThat(blazeProjectData).isNotNull();

    return WorkspaceHelper.getExternalSourceRoot(blazeProjectData).toFile();
  }

  protected ExternalWorkspaceFixture createExternalWorkspaceFixture(ExternalWorkspace workspace) {
    return new ExternalWorkspaceFixture(workspace, fileSystem, testFixture);
  }
}
