package com.neu.prattle.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neu.prattle.model.Message;
import com.neu.prattle.model.User;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceImplTest {

  @Mock
  private Session session;

  @Mock
  private SessionFactory sessionFactory;

  @Mock
  private Query query;

  @Mock
  private Transaction transaction;

  @InjectMocks
  private MessageService messageService = MessageServiceImpl.getInstance();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(sessionFactory.openSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(null);
    when(session.getTransaction()).thenReturn(transaction);
    doNothing().when(transaction).commit();
    when(session.close()).thenReturn(null);
  }

  @Test
  public void persistMessage() {
    Message message = Message.builder().to("toPerson").from("fromPerson").content("content")
        .build();
    doNothing().when(session).saveOrUpdate(message);
    messageService.persistMessage(message);
    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

    verify(session, times(1)).beginTransaction();
    verify(session, times(1)).close();
    verify(session, times(1)).getTransaction();
    verify(sessionFactory, times(1)).openSession();
    verify(session, times(1)).saveOrUpdate(captor.capture());
    assertEquals(captor.getValue(), message);
  }

  @Test
  public void fetchUnreadMessage() {
    Timestamp  ts = new Timestamp(System.currentTimeMillis());
    when(session.createQuery("FROM Message where timestamp > :lastLogin  and is_read = 0 "
        + "and to_user = \'testUser\'")).thenReturn(query);
    query.setTimestamp("lastLogin", ts);
    List<Message> expList = new ArrayList(Arrays.asList(Message.builder().content("test").build()));
    when(query.list()).thenReturn(expList);
    User user = User.builder().build();
    user.setMemberId("testUser");
    List<Message> list = messageService.fetchUnreadMessages(user, ts);
    assertEquals(expList, list);

  }

  @Test
  public void fetchAllMessage() {
    Timestamp  ts = new Timestamp(System.currentTimeMillis());
    when(session.createQuery("FROM Message")).thenReturn(query);
    query.setTimestamp("lastLogin", ts);
    List<Message> expList = new ArrayList(Arrays.asList(Message.builder().content("test").build()));
    when(query.list()).thenReturn(expList);
    User user = User.builder().build();
    user.setMemberId("testUser");
    List<Message> list = messageService.fetchAllUnreadMessages(ts);
    assertEquals(expList, list);

  }

  @Test
  public void fetchTimeRange() {
    Timestamp  ts = new Timestamp(System.currentTimeMillis());
    when(session.createQuery(
            "FROM Message where timestamp > :from  and timestamp<= :to and from_user = \'testUser\'"+
            " or to_user = \'testUser\'")).thenReturn(query);
    query.setTimestamp("lastLogin", ts);
    List<Message> expList = new ArrayList(Arrays.asList(Message.builder().content("test").build()));
    when(query.list()).thenReturn(expList);
    User user = User.builder().build();
    user.setMemberId("testUser");
    List<Message> list = messageService.fetchUserTimeRange(ts,ts,user.getMemberId());
    assertEquals(expList, list);

  }


}