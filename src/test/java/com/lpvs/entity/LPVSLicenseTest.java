package com.lpvs.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class LPVSLicenseTest {
    LPVSLicense lpvsLicense;
    Long baseLicenseId = 1234567890L;
    String baseLicenseName = "licenseName";
    String baseSpdxId = "spdxId";
    String baseAccess = "access";
    String baseChecklistUrl = "checklistUrl";

    List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");


    @BeforeEach
    void setUp(){
        lpvsLicense = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseChecklistUrl,
                baseIncompatibleWith);
    }

    @Test
    public void constructorTest(){
        LPVSLicense lpvsLicenseBase = new LPVSLicense(lpvsLicense);
        assertEquals(lpvsLicenseBase.getLicenseId(), baseLicenseId);
        assertEquals(lpvsLicenseBase.getLicenseName(), baseLicenseName);
        assertEquals(lpvsLicenseBase.getSpdxId(), baseSpdxId);
        assertEquals(lpvsLicenseBase.getAccess(), baseAccess);
//        assertIterableEquals(lpvsLicenseBase.getIncompatibleWith(), baseIncompatibleWith);
    }

    @Test
    public void getLicenseIdTest() {
        assertEquals(lpvsLicense.getLicenseId(), baseLicenseId);
    }

    @Test
    public void getLicenseNameTest() {
        assertEquals(lpvsLicense.getLicenseName(), baseLicenseName);
    }
    @Test
    public void getSpdxIdTest() {
        assertEquals(lpvsLicense.getSpdxId(), baseSpdxId);
    }
    @Test
    public void getAccessTest() {
        assertEquals(lpvsLicense.getAccess(), baseAccess);
    }
    @Test
    public void getChecklistUrlTest() {
        assertEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
    }
    @Test
    public void getIncompatibleWithTest() {
        assertIterableEquals(lpvsLicense.getIncompatibleWith(), baseIncompatibleWith);
    }

    @Test
    public void setLicenseIdTest() {
        Long newActualValue = 0L;
        assertEquals(lpvsLicense.getLicenseId(), baseLicenseId);
        lpvsLicense.setLicenseId(newActualValue);
        assertNotEquals(lpvsLicense.getLicenseId(), baseLicenseId);
        assertEquals(lpvsLicense.getLicenseId(), newActualValue);
    }

    @Test
    public void setLicenseNameTest() {
        String newActualName = "NewName";
        assertEquals(lpvsLicense.getLicenseName(), baseLicenseName);
        lpvsLicense.setLicenseName(newActualName);
        assertNotEquals(lpvsLicense.getLicenseName(), baseLicenseName);
        assertEquals(lpvsLicense.getLicenseName(), newActualName);
    }

    @Test
    public void setSpdxIdTest() {
        String newActualSpdxId = "NewSpdxId";
        assertEquals(lpvsLicense.getSpdxId(), baseSpdxId);
        lpvsLicense.setSpdxId(newActualSpdxId);
        assertNotEquals(lpvsLicense.getSpdxId(), baseSpdxId);
        assertEquals(lpvsLicense.getSpdxId(), newActualSpdxId);
    }

    @Test
    public void setAccessTest() {
        String newActualAccess = "newAccess";
        assertEquals(lpvsLicense.getAccess(), baseAccess);
        lpvsLicense.setAccess(newActualAccess);
        assertNotEquals(lpvsLicense.getAccess(), baseAccess);
        assertEquals(lpvsLicense.getAccess(), newActualAccess);
    }

    @Test
    public void setChecklistUrlTest() {
        String newActualCheckUrl = "NewCheckUrl";
        assertEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
        lpvsLicense.setChecklistUrl(newActualCheckUrl);
        assertNotEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
        assertEquals(lpvsLicense.getChecklistUrl(), newActualCheckUrl);
    }

    @Test
    public void setIncompatibleWithTest() {
        List<String> newIncompatibleWith = Arrays.asList("newIncompatibleWith1", "newIncompatibleWith2", "newIncompatibleWith3");
        assertIterableEquals(lpvsLicense.getIncompatibleWith(), baseIncompatibleWith);
        lpvsLicense.setIncompatibleWith(newIncompatibleWith);
        for (String base: baseIncompatibleWith) {
            for (String actual: lpvsLicense.getIncompatibleWith()){
                assertNotEquals(base, actual);
            }
        }
        assertIterableEquals(lpvsLicense.getIncompatibleWith(), newIncompatibleWith);
    }
}
