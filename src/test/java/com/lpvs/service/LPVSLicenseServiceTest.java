/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSLicenseConflict;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseConflictRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.util.LPVSExitHandler;
import lombok.extern.slf4j.Slf4j;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SystemStubsExtension.class)
public class LPVSLicenseServiceTest {

    @SystemStub private EnvironmentVariables environmentVars;

    private LPVSExitHandler exitHandler;

    @Test
    public void testFindConflicts() {
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl("http://github.com/Samsung/LPVS/pull/1");
        webhookConfig.setRepositoryLicense("Apache-2.0");
        webhookConfig.setPullRequestAPIUrl("http://url.com");
        LPVSFile file = new LPVSFile();
        LPVSLicense license =
                new LPVSLicense() {
                    {
                        setChecklistUrl("");
                        setAccess("unrviewed");
                        setSpdxId("LPGL-2.1-or-later");
                    }
                };
        file.setLicenses(
                new HashSet<LPVSLicense>() {
                    {
                        add(license);
                    }
                });
        file.setFilePath("");
        file.setComponentFilePath("");
        file.setComponentName("");
        file.setComponentLines("");
        file.setComponentUrl("");
        file.setComponentVersion("");
        file.setComponentVendor("");
        file.setSnippetMatch("");
        file.setMatchedLines("");
        file.setSnippetType("");

        LPVSFile file1 = new LPVSFile();
        LPVSLicense license1 =
                new LPVSLicense() {
                    {
                        setChecklistUrl("");
                        setAccess("unrviewed");
                        setSpdxId("Apache-2.0");
                    }
                };
        file1.setLicenses(
                new HashSet<LPVSLicense>() {
                    {
                        add(license1);
                    }
                });
        file1.setFilePath("");
        file1.setComponentFilePath("");
        file1.setComponentName("");
        file1.setComponentLines("");
        file1.setComponentUrl("");
        file1.setComponentVersion("");
        file1.setComponentVendor("");
        file1.setSnippetMatch("");
        file1.setMatchedLines("");
        file1.setSnippetType("");

        LPVSFile file2 = new LPVSFile();
        LPVSLicense license2 =
                new LPVSLicense() {
                    {
                        setChecklistUrl("");
                        setAccess("unrviewed");
                        setSpdxId("MIT");
                    }
                };
        file2.setLicenses(
                new HashSet<LPVSLicense>() {
                    {
                        add(license2);
                    }
                });
        file2.setFilePath("");
        file2.setComponentFilePath("");
        file2.setComponentName("");
        file2.setComponentLines("");
        file2.setComponentUrl("");
        file2.setComponentVersion("");
        file2.setComponentVendor("");
        file2.setSnippetMatch("");
        file2.setMatchedLines("");
        file2.setSnippetType("");

        List<LPVSFile> fileList =
                new ArrayList<LPVSFile>() {
                    {
                        add(file);
                        add(file1);
                        add(file2);
                    }
                };
        LPVSLicenseService licenseService = new LPVSLicenseService("", exitHandler);
        ReflectionTestUtils.setField(
                licenseService,
                "licenseConflicts",
                new ArrayList<LPVSLicenseService.Conflict<String, String>>() {
                    {
                        add(new LPVSLicenseService.Conflict<>("", ""));
                    }
                });
        Assertions.assertNotNull(licenseService.findConflicts(webhookConfig, fileList));
    }

    @Nested
    class TestInit {
        final LPVSLicenseService licenseService = new LPVSLicenseService(null, exitHandler);

        final LPVSLicenseRepository lpvsLicenseRepository =
                Mockito.mock(LPVSLicenseRepository.class);

        final LPVSLicenseConflictRepository lpvsLicenseConflictRepository =
                Mockito.mock(LPVSLicenseConflictRepository.class);

        @Test
        public void testInit() {
            environmentVars.set("LPVS_LICENSE_CONFLICT", "scanner");
            try {
                Method init_method = licenseService.getClass().getDeclaredMethod("init");
                init_method.setAccessible(true);
                init_method.invoke(licenseService);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("LPVSLicenseServiceTest::TestInit exception: " + e);
                fail();
            }
        }

        @Test
        public void testInitDB() {
            environmentVars.set("LPVS_LICENSE_CONFLICT", "db");
            try {
                LPVSLicense license1 =
                        new LPVSLicense() {
                            {
                                setChecklistUrl("");
                                setAccess("unrviewed");
                                setSpdxId("Apache-2.0");
                            }
                        };
                LPVSLicense license2 =
                        new LPVSLicense() {
                            {
                                setChecklistUrl("");
                                setAccess("unrviewed");
                                setSpdxId("MIT");
                            }
                        };
                List<LPVSLicense> licenseList = new ArrayList<>();
                licenseList.add(license1);
                licenseList.add(license2);
                Mockito.when(lpvsLicenseRepository.takeAllLicenses()).thenReturn(licenseList);

                LPVSLicenseConflict licenseConflict = new LPVSLicenseConflict();
                licenseConflict.setConflictId(1L);
                licenseConflict.setConflictLicense(license1);
                licenseConflict.setRepositoryLicense(license2);
                List<LPVSLicenseConflict> licenseConflictList = new ArrayList<>();
                licenseConflictList.add(licenseConflict);
                Mockito.when(lpvsLicenseConflictRepository.takeAllLicenseConflicts())
                        .thenReturn(licenseConflictList);

                Field lpvsLicenseRepositoryField =
                        LPVSLicenseService.class.getDeclaredField("lpvsLicenseRepository");
                lpvsLicenseRepositoryField.setAccessible(true);
                lpvsLicenseRepositoryField.set(licenseService, lpvsLicenseRepository);

                Field lpvsLicenseConflictRepositoryField =
                        LPVSLicenseService.class.getDeclaredField("lpvsLicenseConflictRepository");
                lpvsLicenseConflictRepositoryField.setAccessible(true);
                lpvsLicenseConflictRepositoryField.set(
                        licenseService, lpvsLicenseConflictRepository);

                Method init_method = licenseService.getClass().getDeclaredMethod("init");
                init_method.setAccessible(true);
                init_method.invoke(licenseService);
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchFieldException e) {
                log.error("LPVSLicenseServiceTest::TestInit exception: " + e);
                fail();
            }
        }
    }

    @Nested
    class TestFindLicenseBySPDXFindLicenseByName {
        LPVSLicenseService licenseService;

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";

        // `lpvs_license_2`
        LPVSLicense lpvs_license_2;
        final String license_name_2 = "Apache-2.0 License";
        final String spdx_id_2 = "Apache-2.0";

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);

            lpvs_license_1 = new LPVSLicense(1L, license_name_1, spdx_id_1, null, null, null);
            lpvs_license_2 = new LPVSLicense(2L, license_name_2, spdx_id_2, null, null, null);

            licenseService.addLicenseToList(lpvs_license_1);
            licenseService.addLicenseToList(lpvs_license_2);
        }

        @Test
        public void testFindLicenseBySPDXFindLicenseByName() {
            assertEquals(lpvs_license_1, licenseService.findLicenseBySPDX(spdx_id_1));
            assertEquals(lpvs_license_2, licenseService.findLicenseBySPDX(spdx_id_2));
            assertNull(licenseService.findLicenseBySPDX("Apache-1.1"));

            assertEquals(lpvs_license_1, licenseService.findLicenseByName(license_name_1));
            assertEquals(lpvs_license_2, licenseService.findLicenseByName(license_name_2));
            assertNull(licenseService.findLicenseByName("Apache-1.1 License"));
        }
    }

    @Nested
    class TestAddLicenseConflict {
        LPVSLicenseService licenseService;
        LPVSLicenseService.Conflict<String, String> conflict_1 =
                new LPVSLicenseService.Conflict<>("MIT", "Apache-1.1");
        LPVSLicenseService.Conflict<String, String> conflict_2 =
                new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);
            Field conflicts_field = licenseService.getClass().getDeclaredField("licenseConflicts");
            conflicts_field.setAccessible(true);
            conflicts_field.set(licenseService, new ArrayList<>());
        }

        @Test
        public void testAddLicenseConflict() throws NoSuchFieldException, IllegalAccessException {
            licenseService.addLicenseConflict("MIT", "Apache-1.1");
            licenseService.addLicenseConflict("Apache-1.1", "MIT");
            licenseService.addLicenseConflict("MIT", "Apache-2.0");

            Field conflicts_field = licenseService.getClass().getDeclaredField("licenseConflicts");
            conflicts_field.setAccessible(true);
            List<?> conflicts_list = (List<?>) conflicts_field.get(licenseService);

            assertEquals(List.of(conflict_1, conflict_2), conflicts_list);
        }
    }

    @Nested
    class TestCheckLicense {
        LPVSLicenseService licenseService;

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String spdx_id_1 = "MIT";
        // `lpvs_license_2`
        LPVSLicense lpvs_license_2;
        final String spdx_id_2 = "Apache-1.1-or-later";
        // `lpvs_license_3`
        LPVSLicense lpvs_license_3;
        final String spdx_id_3 = "Apache-2.0-only";

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);

            lpvs_license_1 = new LPVSLicense(1L, null, spdx_id_1, null, null, null);
            lpvs_license_2 = new LPVSLicense(1L, null, spdx_id_2, null, null, null);
            lpvs_license_3 = new LPVSLicense(1L, null, spdx_id_3, null, null, null);

            Field conflicts_field = licenseService.getClass().getDeclaredField("licenses");
            conflicts_field.setAccessible(true);
            conflicts_field.set(
                    licenseService, List.of(lpvs_license_1, lpvs_license_2, lpvs_license_3));
        }

        @Test
        public void testCheckLicense() {
            assertEquals(lpvs_license_1, licenseService.checkLicense("MIT"));
            assertEquals(lpvs_license_2, licenseService.checkLicense("Apache-1.1+"));
            assertEquals(lpvs_license_3, licenseService.checkLicense("Apache-2.0+"));
            assertNull(licenseService.checkLicense("non-MIT"));
        }
    }

    @Nested
    class TestFindConflicts_fullExecution {
        LPVSLicenseService licenseService;
        LPVSLicenseService.Conflict<String, String> conflict_1 =
                new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        LPVSLicense lpvs_license_1;
        final String spdx_id_1 = "MIT";

        // `lpvs_file_2`
        LPVSFile lpvs_file_2;
        LPVSLicense lpvs_license_2;
        final String spdx_id_2 = "Apache-2.0";

        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);
            Field conflicts_field = licenseService.getClass().getDeclaredField("licenseConflicts");
            conflicts_field.setAccessible(true);
            conflicts_field.set(licenseService, List.of(conflict_1));

            lpvs_license_1 = new LPVSLicense(1L, null, spdx_id_1, null, null, null);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            null,
                            null,
                            null,
                            null,
                            Set.of(lpvs_license_1),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            lpvs_license_2 = new LPVSLicense(2L, null, spdx_id_2, null, null, null);
            lpvs_file_2 =
                    new LPVSFile(
                            2L,
                            null,
                            null,
                            null,
                            null,
                            Set.of(lpvs_license_2),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            webhookConfig = new LPVSQueue();
            webhookConfig.setRepositoryLicense("MIT");
        }

        @Test
        public void testFindConflicts_fullExecution() {
            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);
            List<LPVSLicenseService.Conflict<String, String>> actual =
                    licenseService.findConflicts(webhookConfig, List.of(lpvs_file_1, lpvs_file_2));

            assertEquals(expected, actual);
        }
    }

    final String license1 = "license1";
    final String license2 = "license2";
    final LPVSLicenseService.Conflict<String, String> base_conf_11 =
            new LPVSLicenseService.Conflict<>(license1, license1);
    final LPVSLicenseService.Conflict<String, String> base_conf_12 =
            new LPVSLicenseService.Conflict<>(license1, license2);
    final LPVSLicenseService.Conflict<String, String> base_conf_21 =
            new LPVSLicenseService.Conflict<>(license2, license1);
    final LPVSLicenseService.Conflict<String, String> base_conf_22 =
            new LPVSLicenseService.Conflict<>(license2, license2);

    @Test
    public void findConflictsEmptyScanResults() {
        List<LPVSFile> scanResults = new ArrayList<>();
        if (scanResults.isEmpty()) {
            System.out.println("is empty");
        }
        LPVSLicenseService mockLicenseService = new LPVSLicenseService(null, exitHandler);
        LPVSQueue webhookConfig = new LPVSQueue(); // licenseConflicts = new ArrayList<>();
        assertEquals(
                new ArrayList<>(), mockLicenseService.findConflicts(webhookConfig, scanResults));
    }

    @Test
    public void conflictEquals() {
        String license1 = "license1";
        String license2 = "license2";

        LPVSLicenseService.Conflict<String, String> conf =
                new LPVSLicenseService.Conflict<>(license1, license2);
        assertNotEquals(conf.l1, conf.l2);
    }

    @Test
    public void conflictEqualNotEqualsTest() {
        final String license1 = "license1";
        final String license2 = "license2";
        final LPVSLicenseService.Conflict<String, String> conf_1_2 =
                new LPVSLicenseService.Conflict<>(license1, license2);

        assertTrue(conf_1_2.equals(conf_1_2));
        assertTrue(conf_1_2.equals(base_conf_12));
        assertTrue(conf_1_2.equals(base_conf_21));

        assertFalse(conf_1_2.equals(base_conf_22));
        assertFalse(conf_1_2.equals(base_conf_11));
        assertFalse(conf_1_2.equals(license1));
        assertFalse(conf_1_2.equals(null));
    }

    @Test
    public void hashTest() {
        String license1 = "license1";
        String license2 = "license2";
        assertEquals(Objects.hash(license1, license2), base_conf_12.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_11.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_21.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_22.hashCode());
    }
}
