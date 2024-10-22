/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lpvs.util.LPVSFileUtil.copyFiles;
import static org.junit.jupiter.api.Assertions.*;

public class LPVSFileUtilTest {
    private LPVSQueue webhookConfig = null;
    private File sourceDir;
    private File destinationDir;

    @BeforeEach
    public void setUp() {
        webhookConfig = new LPVSQueue();
        webhookConfig.setId(1L);
        webhookConfig.setRepositoryUrl("http://test.com/test/test");
        webhookConfig.setPullRequestUrl("http://test.com/test/test/pull/123");
    }

    @Test
    public void testSaveGithubDiffs() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedProjectsPathWithCommitSHA(1);

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
        String expected = getExpectedProjectsPathWithCommitSHA(1);

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
        String expected = getExpectedProjectsPathWithCommitSHA(1);

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
        String expected = getExpectedProjectsPathWithCommitSHA(1);

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetLocalDirectoryPathWithHeadCommitSHAEmpty() {
        webhookConfig.setHeadCommitSHA("");
        String expected = getExpectedProjectsPathWithPullRequestId(1);

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetLocalDirectoryPathWithoutHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA(null);
        String expected = getExpectedProjectsPathWithPullRequestId(1);

        assertEquals(expected, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA("aaaa");
        String expected = getExpectedJsonFilePathWithCommitSHA(1);

        assertEquals(expected, LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHAEmpty() {
        webhookConfig.setHeadCommitSHA("");
        String expected = getExpectedJsonFilePathWithPullRequestId(1);

        assertEquals(expected, LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
    }

    @Test
    public void testGetScanResultsJsonFilePathWithoutHeadCommitSHA() {
        webhookConfig.setHeadCommitSHA(null);
        String expected = getExpectedJsonFilePathWithPullRequestId(1);

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

        assertFalse(LPVSFileUtil.saveFile(fileName, directoryPath, patchedLines));
        Boolean result1 = Files.exists(Paths.get(directoryPath, fileName));
        assert (result1.equals(false));

        assertFalse(LPVSFileUtil.saveFile(fileName, directoryPath, null));
        Boolean result2 = Files.exists(Paths.get(directoryPath, fileName));
        assert (result2.equals(false));
    }

    private static String getExpectedProjectsPathWithCommitSHA(long id) {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Projects"
                + File.separator
                + "test"
                + File.separator
                + id
                + "-aaaa";
    }

    private static String getExpectedProjectsPathWithPullRequestId(long id) {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Projects"
                + File.separator
                + "test"
                + File.separator
                + id
                + "-123";
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

    private static String getExpectedJsonFilePathWithPullRequestId(long id) {
        return getExpectedResultsPath() + File.separator + id + "-123.json";
    }

    private static String getExpectedJsonFilePathWithCommitSHA(long id) {
        return getExpectedResultsPath() + File.separator + id + "-aaaa.json";
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

    @Test
    void testConstructorThrowsException_N() {
        try {
            Constructor<LPVSFileUtil> constructor = LPVSFileUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (InvocationTargetException e) {
            assertInstanceOf(
                    UnsupportedOperationException.class,
                    e.getCause(),
                    "UnsupportedOperationException expected");
        } catch (Exception e) {
            fail("Unexpected exception type thrown: " + e.getCause());
        }
    }

    @Test
    public void saveFileToDiskTest() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        String content = "Hello, World!";
        String fileName = "file.txt";
        LPVSFileUtil.saveFileToDisk(sourceDir.getAbsolutePath(), fileName, content);
        assertTrue(new File(sourceDir.getAbsolutePath() + File.separator + fileName).exists());
        deleteDirectory(sourceDir);
    }

    @Test
    public void saveFileToDiskTest_N() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        String content = "Hello, World!";
        String fileName =
                "zxcvbnmasdfghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfsdsdsdhjhjghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfghjklqwertyuiopoiuytrewqasdfg/file.txt";
        assertThrows(
                IOException.class,
                () -> LPVSFileUtil.saveFileToDisk(sourceDir.getAbsolutePath(), fileName, content));
        deleteDirectory(sourceDir);
    }

    @Test
    public void saveFileTest_N() throws IOException {
        sourceDir = Files.createTempDirectory("source").toFile();
        String content = "+Hello, World!";
        String fileName =
                "zxcvbnmasdfghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfsdsdsdhjhjghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfghjklqwertyuiopoiuytrewqasdfghjklmnbvcxzaqwsxcderfvbgtyhnmjuikzxcvbnmasdfghjklqwertyuiopoiuytrewqasdfg/file.txt";
        assertFalse(
                LPVSFileUtil.saveFile(
                        fileName, sourceDir.getAbsolutePath(), Collections.singletonList(content)));
        deleteDirectory(sourceDir);
    }
}
