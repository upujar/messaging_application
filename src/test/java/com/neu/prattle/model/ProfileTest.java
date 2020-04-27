package com.neu.prattle.model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Blob;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class ProfileTest {

  private Profile testProfile;

  @Before
  public void setUp() throws Exception {
    testProfile = Profile.builder().build();
  }

  @Test
  public void testProfileId() {
    testProfile.setProfileId(1);
    assertTrue(testProfile.getProfileId() == 1);
  }

  @Test
  public void testDisplayName() {
    testProfile.setDisplayName("testName");
    assertTrue(testProfile.getDisplayName() == "testName");
  }

  @Test
  public void testDisplayPicture() throws SQLException {
    byte[] bytes = "A byte array".getBytes();
    Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
    testProfile.setDisplayPicture(blob);
    assertTrue(testProfile.getDisplayPicture() == blob);
  }

  @Test
  public void testIsOnline() {
    testProfile.setOnline(true);
    assertTrue(testProfile.isOnline());
  }

  @Test
  public void testIsDNDAccount() {
    testProfile.setDNDAccount(true);
    assertTrue(testProfile.isDNDAccount());
  }


}