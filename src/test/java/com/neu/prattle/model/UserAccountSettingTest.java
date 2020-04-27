package com.neu.prattle.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class UserAccountSettingTest {

  private UserAccountSetting userAccountSetting;

  @Before
  public void setup() {
    userAccountSetting = UserAccountSetting.builder().build();
  }

  @Test
  public void testId() {

    userAccountSetting.setUserAccountSettingId(3);
    assertEquals(3, userAccountSetting.getUserAccountSettingId());
  }

  @Test
  public void testDevices() {
    assertNull(userAccountSetting.getDevices());
    List<Device> devices = new ArrayList<>(Arrays.asList(Device.builder().build()));
    userAccountSetting.setDevices(devices);
    assertEquals(devices, userAccountSetting.getDevices());
  }

  @Test
  public void testIsPrivateAccount() {

    userAccountSetting.setPrivateAccount(false);
    assertFalse(userAccountSetting.isPrivateAccount());
  }

}
