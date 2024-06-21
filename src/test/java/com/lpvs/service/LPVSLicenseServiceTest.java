/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

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

        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        ReflectionTestUtils.setField(
                licenseService, "lpvsLicenseRepository", lpvsLicenseRepository);
        when(lpvsLicenseRepository.findFirstBySpdxIdOrderByIdDesc(anyString())).thenReturn(null);
        when(lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString())).thenReturn(null);

        Assertions.assertNotNull(licenseService.findConflicts(webhookConfig, fileList));
    }

    @Nested
    class TestInit {
        final LPVSLicenseService licenseService = new LPVSLicenseService(null, exitHandler);

        final LPVSLicenseService licenseServiceWithSource =
                new LPVSLicenseService("db", exitHandler);

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
                Mockito.when(lpvsLicenseRepository.findAll()).thenReturn(licenseList);

                LPVSLicenseConflict licenseConflict = new LPVSLicenseConflict();
                licenseConflict.setConflictId(1L);
                licenseConflict.setConflictLicense(license1);
                licenseConflict.setRepositoryLicense(license2);
                List<LPVSLicenseConflict> licenseConflictList = new ArrayList<>();
                licenseConflictList.add(licenseConflict);
                Mockito.when(lpvsLicenseConflictRepository.findAll())
                        .thenReturn(licenseConflictList);

                Field lpvsLicenseRepositoryField =
                        LPVSLicenseService.class.getDeclaredField("lpvsLicenseRepository");
                lpvsLicenseRepositoryField.setAccessible(true);
                lpvsLicenseRepositoryField.set(licenseServiceWithSource, lpvsLicenseRepository);

                Field lpvsLicenseConflictRepositoryField =
                        LPVSLicenseService.class.getDeclaredField("lpvsLicenseConflictRepository");
                lpvsLicenseConflictRepositoryField.setAccessible(true);
                lpvsLicenseConflictRepositoryField.set(
                        licenseServiceWithSource, lpvsLicenseConflictRepository);

                Method init_method = licenseServiceWithSource.getClass().getDeclaredMethod("init");
                init_method.setAccessible(true);
                init_method.invoke(licenseServiceWithSource);
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchFieldException e) {
                log.error("LPVSLicenseServiceTest::TestInit exception: " + e);
                fail();
            }
        }

        @Test
        public void testReloadFromTables()
                throws NoSuchFieldException,
                        SecurityException,
                        IllegalArgumentException,
                        IllegalAccessException {

            List<LPVSLicense> licenseList = new ArrayList<>();

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

            licenseList.add(license1);
            licenseList.add(license2);

            when(lpvsLicenseRepository.findAll()).thenReturn(licenseList);

            LPVSLicenseConflict licenseConflict = new LPVSLicenseConflict();
            licenseConflict.setConflictId(1L);
            licenseConflict.setConflictLicense(license1);
            licenseConflict.setRepositoryLicense(license2);
            List<LPVSLicenseConflict> licenseConflictList = new ArrayList<>();
            licenseConflictList.add(licenseConflict);

            when(lpvsLicenseConflictRepository.findAll()).thenReturn(licenseConflictList);

            Field lpvsLicenseRepositoryField =
                    LPVSLicenseService.class.getDeclaredField("lpvsLicenseRepository");
            lpvsLicenseRepositoryField.setAccessible(true);
            lpvsLicenseRepositoryField.set(licenseService, lpvsLicenseRepository);

            Field lpvsLicenseConflictRepositoryField =
                    LPVSLicenseService.class.getDeclaredField("lpvsLicenseConflictRepository");
            lpvsLicenseConflictRepositoryField.setAccessible(true);
            lpvsLicenseConflictRepositoryField.set(licenseService, lpvsLicenseConflictRepository);

            try {
                licenseService.reloadFromTables();
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception occurred: " + e.getMessage());
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
        final String license_name_aleternative_2 = "Apache License 2.0";

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);

            lpvs_license_1 = new LPVSLicense(1L, license_name_1, spdx_id_1, null, null, null);
            lpvs_license_2 =
                    new LPVSLicense(
                            2L, license_name_2, spdx_id_2, null, license_name_aleternative_2, null);

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
            assertEquals(
                    lpvs_license_2, licenseService.findLicenseByName(license_name_aleternative_2));
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

            LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
            Field license_repository =
                    licenseService.getClass().getDeclaredField("lpvsLicenseRepository");
            license_repository.setAccessible(true);
            license_repository.set(licenseService, lpvsLicenseRepository);

            lpvs_license_1 = new LPVSLicense(1L, null, spdx_id_1, null, null, null);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            null,
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
                            null,
                            Set.of(lpvs_license_2),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

            when(lpvsLicenseRepository.findFirstBySpdxIdOrderByIdDesc(anyString()))
                    .thenReturn(null);
            when(lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString()))
                    .thenReturn(lpvs_license_1);

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

    @Nested
    class TestGetLicenseBySpdxIdAndNameTest {

        LPVSLicenseService licenseService;
        LPVSLicense lpvs_license_1, lpvs_license_2;
        LPVSLicenseRepository licenseRepository;

        @BeforeEach
        void init() throws NoSuchFieldException, IllegalAccessException {
            licenseService = new LPVSLicenseService(null, exitHandler);
            licenseRepository = Mockito.mock(LPVSLicenseRepository.class);
            Field license_repository =
                    licenseService.getClass().getDeclaredField("lpvsLicenseRepository");
            license_repository.setAccessible(true);
            license_repository.set(licenseService, licenseRepository);

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            "OpenSSL License",
                            "OpenSSL",
                            "PERMITTED",
                            "OPENSSL_LICENSE,SSLeay license and OpenSSL License",
                            null);
            lpvs_license_2 =
                    new LPVSLicense(
                            2L,
                            "GNU General Public License v3.0 only",
                            "GPL-3.0-only",
                            "PROHIBITED",
                            null,
                            null);
            licenseService.addLicenseToList(lpvs_license_1);
            licenseService.addLicenseToList(lpvs_license_2);
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_findLicenseBySPDX() {
            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName("GPL-3.0-only", Optional.empty());
            assertEquals("GPL-3.0-only", result.getSpdxId());
            assertEquals("GNU General Public License v3.0 only", result.getLicenseName());
            assertEquals("PROHIBITED", result.getAccess());
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_findLicenseByName() {
            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName("OpenSSL License", Optional.empty());
            assertEquals("OpenSSL", result.getSpdxId());
            assertEquals("OpenSSL License", result.getLicenseName());
            assertEquals("PERMITTED", result.getAccess());
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_connectToOSORI()
                throws NoSuchFieldException,
                        IllegalAccessException,
                        IOException,
                        URISyntaxException {
            Field osoriDbUrl = licenseService.getClass().getDeclaredField("osoriDbUrl");
            osoriDbUrl.setAccessible(true);
            osoriDbUrl.set(licenseService, "http://127.0.0.1:8080");

            HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(200);

            LPVSLicenseService.OsoriConnection mockOsoriConnection =
                    Mockito.mock(LPVSLicenseService.OsoriConnection.class);
            Field osoriConnection = licenseService.getClass().getDeclaredField("osoriConnection");
            osoriConnection.setAccessible(true);
            osoriConnection.set(licenseService, mockOsoriConnection);

            when(mockOsoriConnection.createConnection(anyString(), anyString()))
                    .thenReturn(mockConnection);

            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass()
                                                    .getClassLoader()
                                                    .getResource("osori_db_response1.json"))
                                    .toURI());

            when(mockConnection.getInputStream()).thenReturn(Files.newInputStream(path));
            when(licenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName("Apache-2.0", Optional.empty());
            assertEquals("Apache-2.0", result.getSpdxId());
            assertEquals("Apache License 2.0", result.getLicenseName());
            assertEquals("UNREVIEWED", result.getAccess());
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_createNewLicense()
                throws NoSuchFieldException,
                        IllegalAccessException,
                        IOException,
                        URISyntaxException {
            Field osoriDbUrl = licenseService.getClass().getDeclaredField("osoriDbUrl");
            osoriDbUrl.setAccessible(true);
            osoriDbUrl.set(licenseService, "http://127.0.0.1:8080");

            HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(200);

            LPVSLicenseService.OsoriConnection mockOsoriConnection =
                    Mockito.mock(LPVSLicenseService.OsoriConnection.class);
            Field osoriConnection = licenseService.getClass().getDeclaredField("osoriConnection");
            osoriConnection.setAccessible(true);
            osoriConnection.set(licenseService, mockOsoriConnection);

            when(mockOsoriConnection.createConnection(anyString(), anyString()))
                    .thenReturn(mockConnection);

            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass()
                                                    .getClassLoader()
                                                    .getResource("osori_db_response2.json"))
                                    .toURI());

            when(mockConnection.getInputStream()).thenReturn(Files.newInputStream(path));
            when(licenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName(
                            "Apache-2.0", Optional.of("Apache License 2.0"));
            assertEquals("Apache-2.0", result.getSpdxId());
            assertEquals("Apache License 2.0", result.getLicenseName());
            assertEquals("UNREVIEWED", result.getAccess());
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_emptyOsoriUrl_N() {
            when(licenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);
            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName(
                            "Apache-2.0", Optional.of("Apache License 2.0"));
            assertEquals("Apache-2.0", result.getSpdxId());
            assertEquals("Apache License 2.0", result.getLicenseName());
            assertEquals("UNREVIEWED", result.getAccess());
        }

        @Test
        public void getLicenseBySpdxIdAndNameTest_errorCodeInOSORI_N()
                throws NoSuchFieldException,
                        IllegalAccessException,
                        IOException,
                        URISyntaxException {
            Field osoriDbUrl = licenseService.getClass().getDeclaredField("osoriDbUrl");
            osoriDbUrl.setAccessible(true);
            osoriDbUrl.set(licenseService, "http://127.0.0.1:8080");

            HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(400);

            LPVSLicenseService.OsoriConnection mockOsoriConnection =
                    Mockito.mock(LPVSLicenseService.OsoriConnection.class);
            Field osoriConnection = licenseService.getClass().getDeclaredField("osoriConnection");
            osoriConnection.setAccessible(true);
            osoriConnection.set(licenseService, mockOsoriConnection);

            when(mockOsoriConnection.createConnection(anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(licenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            LPVSLicense result =
                    licenseService.getLicenseBySpdxIdAndName(
                            "Apache-2.0", Optional.of("Apache License 2.0"));
            assertEquals("Apache-2.0", result.getSpdxId());
            assertEquals("Apache License 2.0", result.getLicenseName());
            assertEquals("UNREVIEWED", result.getAccess());
        }

        @Test
        public void findLicenseInOsoriDBTest()
                throws IOException,
                        IllegalAccessException,
                        NoSuchFieldException,
                        URISyntaxException {
            Field osoriDbUrl = licenseService.getClass().getDeclaredField("osoriDbUrl");
            osoriDbUrl.setAccessible(true);
            osoriDbUrl.set(licenseService, "http://127.0.0.1:8080");

            HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(200);

            LPVSLicenseService.OsoriConnection mockOsoriConnection =
                    Mockito.mock(LPVSLicenseService.OsoriConnection.class);
            Field osoriConnection = licenseService.getClass().getDeclaredField("osoriConnection");
            osoriConnection.setAccessible(true);
            osoriConnection.set(licenseService, mockOsoriConnection);

            when(mockOsoriConnection.createConnection(anyString(), anyString()))
                    .thenReturn(mockConnection);

            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass()
                                                    .getClassLoader()
                                                    .getResource("osori_db_response1.json"))
                                    .toURI());

            when(mockConnection.getInputStream()).thenReturn(Files.newInputStream(path));
            when(licenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            LPVSLicense result = licenseService.findLicenseInOsoriDB("Apache-2.0");
            assertEquals("Apache-2.0", result.getSpdxId());
            assertEquals("Apache License 2.0", result.getLicenseName());
            assertEquals("UNREVIEWED", result.getAccess());
        }

        @Test
        public void findLicenseTest() {
            LPVSLicense result = licenseService.findLicense("GPL-3.0-only", "GPL-3.0-only");
            assertEquals("GPL-3.0-only", result.getSpdxId());
            assertEquals("GNU General Public License v3.0 only", result.getLicenseName());
            assertEquals("PROHIBITED", result.getAccess());
        }
    }

    @Nested
    class TestOsoriConnection {

        @Test
        public void testCreateConnection() throws IOException {
            String osoriDbUrl = "https://ossori.com";
            String licenseSpdxId = "Apache-2.0";
            LPVSLicenseService.OsoriConnection connection =
                    new LPVSLicenseService.OsoriConnection();
            HttpURLConnection httpURLConnection =
                    connection.createConnection(osoriDbUrl, licenseSpdxId);
            assertEquals(
                    osoriDbUrl + "/api/v1/user/licenses/spdx_identifier?searchWord=Apache-2.0",
                    httpURLConnection.getURL().toString());
        }

        @Test
        public void testCreateConnectionThrowsIOException_N() {
            String licenseSpdxId = "Apache-2.0";
            LPVSLicenseService.OsoriConnection connection =
                    new LPVSLicenseService.OsoriConnection();
            assertThrows(IOException.class, () -> connection.createConnection(null, licenseSpdxId));
        }
    }
}
