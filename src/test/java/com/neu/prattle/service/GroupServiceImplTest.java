package com.neu.prattle.service;

import com.neu.prattle.exceptions.AlreadyExistingModeratorException;
import com.neu.prattle.exceptions.GroupAlreadyPresentException;
import com.neu.prattle.exceptions.GroupDoesNotExistException;
import com.neu.prattle.exceptions.MemberDoesNotExistInGroupException;
import com.neu.prattle.exceptions.NotExistingMember;
import com.neu.prattle.model.Group;
import com.neu.prattle.viewmodel.GroupMember;
import com.neu.prattle.model.Member;
import com.neu.prattle.model.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceImplTest {

  @Mock
  private Session session;

  @Mock
  private SessionFactory sessionFactory;

  @Mock
  private Query query;

  @Mock
  private Transaction transaction;

  @InjectMocks
  private GroupService as = GroupServiceImpl.getInstance();


  private List<Group> list;

  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);
    when(sessionFactory.openSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(null);
    when(session.getTransaction()).thenReturn(transaction);
    doNothing().when(transaction).commit();
    when(session.close()).thenReturn(null);

    list = new ArrayList<>();
    when(session.createQuery("FROM Group")).thenReturn(query);
    when(query.list()).thenReturn(list);
  }

  @Test
  @Order(1)
  public void addGroupTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    when(session.save(group)).thenReturn(null);
    as.addGroup(group);
    list.add(group);
    Optional<Group> group1 = as.findGroupById("group1");
    assertTrue(group1.isPresent());
  }


  @Test(expected = GroupAlreadyPresentException.class)
  @Order(2)
  public void addExistingGroupTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    list.add(group);
    Optional<Group> group1 = as.findGroupById("group1");


    assertTrue(group1.isPresent());
    when(session.save(group1)).thenReturn(null);

    as.addGroup(group);
  }

  @Test
  @Order(3)
  public void getNonExistentGroupTest() {
    Optional<Group> user = as.findGroupById("MikeX");
    assertFalse(user.isPresent());
  }

  @Test
  @Order(4)
  public void addGroupMemberTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    list.add(group);
    when(session.save(group)).thenReturn(null);
    Member user = User.builder().build();
    user.setMemberId("testUserId");
    doNothing().when(session).saveOrUpdate(group);
    assertTrue(as.addMemberToGroupDB(group.getMemberId(),user));
  }

  @Test
  @Order(4)
  public void addGroupMemberTest1() {
    Group group = Group.builder().members(new HashSet<>(Arrays.asList(User.builder().build()))).build();
    group.setMemberId("group1");
    list.add(group);
    when(session.save(group)).thenReturn(null);
    Member user = User.builder().build();
    user.setMemberId("testUserId");
    doNothing().when(session).saveOrUpdate(group);
    assertTrue(as.addMemberToGroupDB(group.getMemberId(),user));
  }

  @Test
  @Order(5)
  public void addGroupMemberToNonExistantGroupTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    when(session.save(group)).thenReturn(null);
    Member user = User.builder().build();
    user.setMemberId("testUserId");
    doNothing().when(session).saveOrUpdate(group);
    assertFalse(as.addMemberToGroupDB("group2",user));
  }

  @Test
  @Order(6)
  public void deleteExistingGroupTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    list.add(group);
    doNothing().when(session).delete(group);
    assertTrue(as.deleteGroup("group1"));
  }

  @Test(expected = GroupDoesNotExistException.class)
  @Order(7)
  public void deleteNonExistingGroupTest() {
    Group group = Group.builder().build();
    group.setMemberId("group1");
    when(session.save(group)).thenReturn(null);
    doNothing().when(session).delete(group);
    assertTrue(as.deleteGroup("group2"));
  }


  @Test
  @Order(8)
  public void getMembersTest(){
    Group group = Group.builder().build();
    group.setMemberId("sample");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    group.setMembers(members);
    as.addGroup(group);
    List<Member> ans = new ArrayList<>(as.getMembers(group.getMemberId()));
    assertEquals(2,ans.size());
    assertEquals(user1.getMemberId(), ans.get(0).getMemberId());
    assertEquals(user2.getMemberId(), ans.get(1).getMemberId());
  }

  @Test
  @Order(9)
  public void getMembersFromEmptyGroupTest(){
    Group group = Group.builder().build();
    group.setMemberId("sample");
    Set<Member> members = new HashSet<>();
    group.setMembers(members);
    assertEquals(0,as.getMembers("sample").size());
  }

  @Test
  @Order(10)
  public void getMembersFromNonExistentGroupTest(){
    assertEquals(0,as.getMembers("123").size());
  }


  @Test(expected = GroupDoesNotExistException.class)
  @Order(11)
  public void testRemoveMemberFromNonExistentGroup(){
    as.removeMemberFromGroup("m1","groupk");
  }

  @Test(expected = MemberDoesNotExistInGroupException.class)
  @Order(12)
  public void testRemoveNonExistentMember() {
    Group group = Group.builder().build();
    group.setMemberId("sample1");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    group.setMembers(members);
    as.addGroup(group);
    as.removeMemberFromGroup("two", group.getMemberId());
  }

  @Test
  @Order(13)
  public void removeExistingMember() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("sample2");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    group.setMembers(members);
    as.addGroup(group);
    as.removeMemberFromGroup(user2.getMemberId(),group.getMemberId());
    assertFalse(as.getMembers(group.getMemberId()).contains(user2));
  }


  @Test(expected = AlreadyExistingModeratorException.class)
  @Order(14)
  public void testAddExistingModerator() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("TestGroup");
    group.setModeratorIds("one,two");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Member user3 = User.builder().build();
    user3.setMemberId("three");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    members.add(user3);
    group.setMembers(members);
    as.addGroup(group);
    as.addModeratorToGroup("one","TestGroup","two");
  }

  @Test(expected = NotExistingMember.class)
  @Order(15)
  public void testAddModeratorNonMember() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("TestGroup2");
    group.setModeratorIds("one,two");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Member user3 = User.builder().build();
    user3.setMemberId("three");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    members.add(user3);
    group.setMembers(members);
    as.addGroup(group);
    as.addModeratorToGroup("one1","TestGroup2","two");
  }

  @Test(expected = GroupDoesNotExistException.class)
  @Order(16)
  public void testAddModeratorNonExistingGroup() {
    as.addModeratorToGroup("one","xyz","two");
  }

  @Test(expected = NotAuthorizedException.class)
  @Order(16)
  public void testNonAuthorized() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("TestGroup2");
    group.setModeratorIds("one,two");
    list.add(group);
    as.addGroup(group);


    as.addModeratorToGroup("one","TestGroup2","three");
  }

  @Test
  @Order(18)
  public void testAddModeratorValid() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("TestGroup3");
    group.setModeratorIds("one,two");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Member user3 = User.builder().build();
    user3.setMemberId("three");
    Member user4 = User.builder().build();
    user4.setMemberId("four");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    members.add(user3);
    members.add(user4);
    group.setMembers(members);
    as.addGroup(group);
    as.addModeratorToGroup("three","TestGroup3","two");
    assertTrue(group.getModeratorIds().contentEquals("one,two,three"));
  }

  @Test
  @Order(19)
  public void testTransferModeratorValid() {
    doNothing().when(session).saveOrUpdate(any());
    Group group = Group.builder().build();
    group.setMemberId("TestGroup4");
    group.setModeratorIds("one,two");
    Member user1 = User.builder().build();
    user1.setMemberId("one");
    Member user2 = User.builder().build();
    user2.setMemberId("two");
    Member user3 = User.builder().build();
    user3.setMemberId("three");
    Member user4 = User.builder().build();
    user4.setMemberId("four");
    Set<Member> members = new HashSet<>();
    members.add(user1);
    members.add(user2);
    members.add(user3);
    members.add(user4);
    group.setMembers(members);
    as.addGroup(group);
    System.out.println(group.getModeratorIds());
    as.transferModeratorResponsibility("four","TestGroup4","two");
    System.out.println(group.getModeratorIds());
    assertTrue(group.getModeratorIds().contentEquals("four,one"));
  }

  @Test
  // @Order(8)
  public void getGroupsForUserTest(){
    User u = User.builder().build();
    u.setMemberId("test123");
    Group g = Group.builder().members(new HashSet<>(Arrays.asList(u))).build();
    g.setMemberId("g1");
    list.add(g);

    List<String> res = as.getGroupsForUser("test123");
    assertEquals(Arrays.asList("g1"), res);

  }

  @Test
  public void testQueryRequest2() {
    Group group = Group.builder().build();
    group.setMemberId("TestGroup4");
    group.setModeratorIds("one,two");
    list.add(group);
    List<String> groupIds = new ArrayList<>();
    groupIds.add("TestGroup4");

    assertTrue(groupIds.containsAll(as.getGroupIdsWhereUserIsModerator("one")));
  }

}