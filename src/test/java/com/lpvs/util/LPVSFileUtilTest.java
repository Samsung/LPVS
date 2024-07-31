/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.lpvs.util.LPVSFileUtil.copyFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LPVSFileUtilTest {
    private LPVSQueue webhookConfig = null;
    private File sourceDir;
    private File destinationDir;

    @BeforeEach
    public void setUp() {
        webhookConfig = new LPVSQueue();
        webhookConfig.setRepositoryUrl("http://test.com/test/test");
        webhookConfig.setPullRequestUrl("http://test.com/test/test/pull/123");
    }

    @Test
    public void testSaveGithubDiffs() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedProjectsPathWithCommitSHA();

        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        ReflectionTestUtils.setField(detail, "filename", "I_am_a_file");
        ReflectionTestUtils.setField(detail, "patch", "+ a\n- b\n@@ -8,7 +8,6 @@\n c");
        assertEquals(
                expected,
                LPVSFileUtil.saveGithubDiffs(
                        new ArrayList<GHPullRequestFileDetail>() {
                            {
                                add(detail);
                            }
                        },
                        webhookConfig));
    }

    @Test
    public void testSaveGithubDiffsFileNameWithSlash() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedProjectsPathWithCommitSHA();

        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        ReflectionTestUtils.setField(detail, "filename", "dir/I_am_a_file");
        ReflectionTestUtils.setField(detail, "patch", "+ a\n- b\n@@ -8,7 +8,6 @@\n c");
        assertEquals(
                expected,
                LPVSFileUtil.saveGithubDiffs(
                        new ArrayList<GHPullRequestFileDetail>() {
                            {
                                add(detail);
                            }
                        },
                        webhookConfig));
    }

    @Test
    public void testSaveGithubDiffsEmptyPatchLines() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedProjectsPathWithCommitSHA();

        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        ReflectionTestUtils.setField(detail, "filename", "I_am_a_file");
        ReflectionTestUtils.setField(detail, "patch", "");
        assertEquals(
                expected,
                LPVSFileUtil.saveGithubDiffs(
                        new ArrayList<GHPullRequestFileDetail>() {
                            {
                                add(detail);
                            }
                        },
                        webhookConfig));
    }

    @Test
    public void testCopyFilesDirectory() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        destinationDir = Files.createTempDirectory("destination").toFile();

        File sourceFile1 = new File(sourceDir, "file1.txt");
        File sourceFile2 = new File(sourceDir, "file2.txt");
        File sourceSubdir = new File(sourceDir, "subdir");
        File sourceSubfile = new File(sourceSubdir, "subfile.txt");

        sourceFile1.createNewFile();
        sourceFile2.createNewFile();
        sourceSubdir.mkdirs();
        sourceSubfile.createNewFile();

        copyFiles(sourceDir.getAbsolutePath(), destinationDir.getAbsolutePath());

        assertTrue(new File(destinationDir, "file1.txt").exists());
        assertTrue(new File(destinationDir, "file2.txt").exists());
        assertTrue(new File(destinationDir, "subdir").exists());
        assertTrue(new File(destinationDir, "subdir/subfile.txt").exists());

        deleteDirectory(sourceDir);
        deleteDirectory(destinationDir);
    }

    @Test
    public void testCopyFilesFile() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        destinationDir = Files.createTempDirectory("destination").toFile();

        File sourceFile = new File(sourceDir, "file.txt");
        sourceFile.createNewFile();

        copyFiles(sourceFile.getAbsolutePath(), destinationDir.getAbsolutePath());

        assertTrue(new File(destinationDir, "file.txt").exists());

        deleteDirectory(sourceDir);
        deleteDirectory(destinationDir);
    }

    @Test
    public void testCopyFilesDirectoryWithNullFiles() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        destinationDir = Files.createTempDirectory("destination").toFile();

        File sourceSubdir = new File(sourceDir, "subdir");
        sourceSubdir.mkdirs();

        copyFiles(sourceDir.getAbsolutePath(), destinationDir.getAbsolutePath());

        assertTrue(new File(destinationDir, "subdir").exists());

        deleteDirectory(sourceDir);
        deleteDirectory(destinationDir);
    }

    @Test
    public void testGetLocalDirectoryPathWithHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedProjectsPathWithCommitSHA();

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetLocalDirectoryPathWithHeadCommitSHAEmpty() {
        webhookConfig.setHeadCommitSHA("");
        String expected = getExpectedProjectsPathWithPullRequestId();

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetLocalDirectoryPathWithoutHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA(null);
        String expected = getExpectedProjectsPathWithPullRequestId();

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedJsonFilePathWithCommitSHA();

        assertEquals(expected, LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHAEmpty() {
        webhookConfig.setHeadCommitSHA("");
        String expected = getExpectedJsonFilePathWithPullRequestId();

        assertEquals(expected, LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithoutHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA(null);
        String expected = getExpectedJsonFilePathWithPullRequestId();

        assertEquals(expected, LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
    }

    @Test
    public void testGetScanResultsDirectoryPath() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedResultsPath();

        assertEquals(expected, LPVSFileUtil.getScanResultsDirectoryPath(webhookConfig));
    }

    @Test
    public void testSaveFileWithEmptyPatchedLines() {
        String fileName = "testFile.txt";
        String directoryPath = "testDirectory";
        List<String> patchedLines = new ArrayList<>();

        LPVSFileUtil.saveFile(fileName, directoryPath, patchedLines);
        Boolean result1 = Files.exists(Paths.get(directoryPath, fileName));
        assert (result1.equals(false));

        LPVSFileUtil.saveFile(fileName, directoryPath, null);
        Boolean result2 = Files.exists(Paths.get(directoryPath, fileName));
        assert (result2.equals(false));
    }

    private static String getExpectedProjectsPathWithCommitSHA() {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Projects"
                + File.separator
                + "test"
                + File.separator
                + "aaaa";
    }

    private static String getExpectedProjectsPathWithPullRequestId() {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Projects"
                + File.separator
                + "test"
                + File.separator
                + "123";
    }

    private static String getExpectedResultsPath() {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Results"
                + File.separator
                + "test";
    }

    private static String getExpectedJsonFilePathWithPullRequestId() {
        return getExpectedResultsPath() + File.separator + "123.json";
    }

    private static String getExpectedJsonFilePathWithCommitSHA() {
        return getExpectedResultsPath() + File.separator + "aaaa.json";
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
