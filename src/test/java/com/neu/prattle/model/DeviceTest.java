package com.neu.prattle.model;

import java.sql.Timestamp;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;



public class DeviceTest {

  private Device device;
  private Timestamp timestamp;

  @Before
  public void setup(){
    timestamp = new Timestamp(System.currentTimeMillis());
    device = Device.builder().build();

  }

  @Test
  public void testDeviceId(){

    device.setDeviceId(2);
    assertEquals(2, device.getDeviceId());
  }

  @Test
  public void testDeviceType(){
    assertNull(device.getDeviceType());
    device.setDeviceType("mobile");
    assertEquals("mobile", device.getDeviceType());
  }

  @Test
  public void testLastLogin(){
    assertNull(device.getLastLogin());
    device.setLastLogin(timestamp);
    assertEquals(timestamp, device.getLastLogin());
  }

  @Test
  public void testAccountSetting(){
    assertNull(device.getUserAccountSetting());
    UserAccountSetting setting = UserAccountSetting.builder().build();
    device.setUserAccountSetting(setting);
    assertEquals(setting, device.getUserAccountSetting());
  }
}
