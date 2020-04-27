package com.neu.prattle.model;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GroupTest {

  private Group testGroup;

  @Before
  public void setUp() {
    testGroup = Group.builder().build();
    testGroup.setMemberId("testGroup");
  }

  @Test
  @Order(1)
  public void isLeaf() {
    assertFalse(testGroup.isLeaf());
  }

  @Test
  @Order(2)
  public void testSetGetModeratorIds() {
    assertNull(testGroup.getModeratorIds());
    testGroup.setModeratorIds("User1,User2");
    assertTrue(testGroup.getModeratorIds().contentEquals("User1,User2"));
  }

  @Test
  @Order(3)
  public void testSetGetMembers() {
    assertNull(testGroup.getMembers());
    Set<Member> members = new HashSet<>();
    testGroup.setMembers(members);
    assertTrue(testGroup.getMembers() == members);
  }

  @Test
  @Order(4)
  public void testGroupConstructor() {
    Member member = new Group();
    member.setMemberId("test");
    assertEquals("test", member.getMemberId());
    assertTrue(member.getParents().size()==0);
  }

}
