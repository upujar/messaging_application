package com.neu.prattle.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.neu.prattle.exceptions.UserAlreadyPresentException;
import com.neu.prattle.model.Device;
import com.neu.prattle.model.Profile;
import com.neu.prattle.model.User;
import com.neu.prattle.model.UserAccountSetting;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

  @Mock
  private Session session;

  @Mock
  private SessionFactory sessionFactory;

  @Mock
  private Query query;

  @Mock
  private Transaction transaction;

  @InjectMocks
  private UserService as = UserServiceImpl.getInstance();

  private List<User> list;


  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);
    as = UserServiceImpl.getInstance();
    when(sessionFactory.openSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(null);
    when(session.getTransaction()).thenReturn(transaction);
    doNothing().when(transaction).commit();
    when(session.close()).thenReturn(null);

    list = new ArrayList<>();
    when(session.createQuery("FROM User")).thenReturn(query);
    when(query.list()).thenReturn(list);
  }


  //
//  // This method just tries to add
  @Test
  @Order(1)
  public void addUserTest() {
    User user = User.builder().name("Mike").build();
    user.setMemberId("mike");

    when(session.save(user)).thenReturn(null);

    user.setProfile(
        Profile.builder().displayName("Mike").isDNDAccount(true).isOnline(false).build());
    as.addUser(user);
    list.add(user);

    Optional<User> user1 = as.findUserById("mike");
    assertTrue(user1.isPresent());
  }


  @Test(expected = UserAlreadyPresentException.class)
  @Order(2)
  public void addExistingUserTest() {
    Optional<User> user = as.findUserById("mike");
    when(session.save(user)).thenReturn(null);
    User user1 = User.builder().name("Mike").build();
    user1.setMemberId("mike");
    as.addUser(user1);
  }


  // This method just tries to add
  @Test
  @Order(3)
  public void getNonExistentUserTest() {
    Optional<User> user = as.findUserById("MikeX");
    assertFalse(user.isPresent());
  }

  @Test
  public void fetchLastLoginTest() {
    User user = User.builder().name("Mike1").build();
    user.setMemberId("mike1");

    when(session.save(user)).thenReturn(null);

    Timestamp ts1 = new Timestamp(System.currentTimeMillis());
    Timestamp ts2 = new Timestamp(System.currentTimeMillis() + 20);
    user.setProfile(
        Profile.builder().displayName("Mike1").isDNDAccount(true).isOnline(false).build());
    user.setUserAccountSetting(UserAccountSetting.builder().devices(
        new ArrayList<Device>(Arrays.asList(Device.builder().lastLogin(ts1).build(),
            Device.builder().lastLogin(ts2).build()))).build());

    list.add(user);

    Timestamp res = as.fetchLatestLogin("mike1");
    assertEquals(ts2, res);


  }

  @Test
  public void fetchLastLoginTest2() {

    Timestamp res = as.fetchLatestLogin("utkarsh");

    assertEquals(null, res);


  }

  @Test
  @Order(4)
  public void validModeratorsTest() {
    Optional<User> user1 = as.findUserById("mike3");
    when(session.save(user1)).thenReturn(null);
    User user2 = User.builder().name("Mike3").build();
    user2.setMemberId("mike3");
    as.addUser(user2);
    Optional<User> user3 = as.findUserById("mike2");
    when(session.save(user3)).thenReturn(null);
    User user4 = User.builder().name("Mike2").build();
    user4.setMemberId("mike2");
    as.addUser(user4);
    assertTrue(as.isModerators("mike2,mike3"));

  }

  @Test
  @Order(5)
  public void invalidModeratorsTest() {
    assertFalse(as.isModerators("xyz,mike2,mike3"));

  }

  @Test

  public void addUserToFriendTest1() {
   // list = new ArrayList<>();
    User user2 = User.builder().name("Mike2").members(new HashSet<>()).build();
    user2.setMemberId("Mike2");
    list.add(user2);
    assertFalse(as.addUserTofriendList("Mike1","Mike2"));
    assertFalse(as.addUserTofriendList("Mike2","Mike1"));


  }



  @Test

  public void addUserToFriendTest3() {
    User user1 = User.builder().name("Mike1").members(new HashSet<>()).build();
    user1.setMemberId("Mike1");
    User user2 = User.builder().name("Mike2").members(new HashSet<>()).build();
    user2.setMemberId("Mike2");
    list.add(user1);
    list.add(user2);


    doNothing().when(session).saveOrUpdate(user1);
    doNothing().when(session).saveOrUpdate(user2);

    assertTrue(as.addUserTofriendList("Mike1","Mike2"));


  }







}
