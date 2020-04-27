package com.neu.prattle.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;

public class UserTest {

  private User testUser;
  private Profile prof;

  @Before
  public void setUp() throws Exception {
    prof = Profile.builder().build();
    testUser = User.builder().profile(prof).name("testName").password("test").build();
    testUser.setMemberId("testId");
  }

  @Test
  @Order(1)
  public void isLeaf() {
    assertTrue(testUser.isLeaf());
  }


  @Test
  @Order(2)
  public void testSetName() {
    testUser.setName("testUser");
    assertTrue(testUser.getName() == "testUser");
  }

  @Test
  @Order(3)
  public void testEquals() {
    assertFalse(testUser.equals("testString"));
  }

  @Test
  @Order(4)
  public void testProfile() {
    assertTrue(testUser.getProfile() == prof);
  }

  @Test
  @Order(5)
  public void testSetProfile() {
    Profile profile = Profile.builder().build();
    testUser.setProfile(profile);
    assertTrue(testUser.getProfile() == profile);
  }

  @Test
  @Order(6)
  public void testUserId() {
    testUser.setMemberId("testIdChanged");
    assertTrue(testUser.getMemberId() == "testIdChanged");

  }

  @Test
  @Order(7)
  public void testEqualsValid() {
    User anotherUser = User.builder().build();
    anotherUser.setMemberId("testId");
    assertTrue(testUser.equals(anotherUser));
  }

  @Test
  @Order(8)
  public void testEqualsInvalid() {
    User anotherUser = User.builder().build();
    anotherUser.setMemberId("testId2");
    assertFalse(testUser.equals(anotherUser));
  }

  @Test
  @Order(8)
  public void testHash() {
    User anotherUser = User.builder().build();
    anotherUser.setMemberId("testId");
    assertTrue(testUser.hashCode() == anotherUser.hashCode());
  }

  @Test
  @Order(9)
  public void testSetUserId() {
    testUser.setMemberId("testChangedId");
    assertTrue(testUser.getMemberId() == "testChangedId");
  }

  @Test
  @Order(10)
  public void testUserCreationBuilderNoProfile() {
    User user = User.builder().name("testName").build();
    user.setMemberId("testId");
    assertTrue(user.getProfile() == null);
    assertTrue(user.getMemberId() == "testId");
    assertTrue(user.getName() == "testName");
  }

  @Test
  @Order(11)
  public void testUserCreationBuilder() {
    User user = User.builder().profile(prof).build();
    user.setMemberId("testId");
    assertTrue(user.getProfile() == prof);
    assertTrue(user.getMemberId() == "testId");
    assertTrue(user.getName() == null);
  }

  @Test
  @Order(12)
  public void testUserAccountSetting() {
    User user = User.builder().name("testName").build();
    UserAccountSetting ua = UserAccountSetting.builder().userAccountSettingId(2).build();
    user.setUserAccountSetting(ua);
    assertTrue(user.getUserAccountSetting().equals(ua));

  }

  @Test
  @Order(13)
  public void testPassWord() {
    testUser.setPassword("Test");
    assertEquals("Test", testUser.getPassword());

  }

  @Test
  public void testUser() {
    User user = new User();
    user.setName("test");
    assertEquals("test", user.getName());
  }

  @Test

  public void testMembers() {
    User user = User.builder().name("testName").build();
    Set<User> friends = new HashSet<>(Arrays.asList(testUser));
    user.setMembers(friends);
    assertTrue(user.getMembers().equals(friends));

  }
}