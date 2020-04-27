package com.neu.prattle.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neu.prattle.model.Invite;
import com.neu.prattle.model.Message;
import java.util.ArrayList;
import java.util.Arrays;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvitationServiceImplTest {

  @Mock
  private Session session;

  @Mock
  private SessionFactory sessionFactory;

  @Mock
  private Query query;

  @Mock
  private Transaction transaction;

  @InjectMocks
  private InvitationService invitationService = InvitationServiceImpl.getInstance();

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
  public void persistInvite() {
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    doNothing().when(session).saveOrUpdate(invitation);
    invitationService.persistInvitation(invitation);
    ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);

    verify(session, times(1)).beginTransaction();
    verify(session, times(1)).close();
    verify(session, times(1)).getTransaction();
    verify(sessionFactory, times(1)).openSession();
    verify(session, times(1)).saveOrUpdate(captor.capture());
    assertEquals(captor.getValue(), invitation);
  }

  @Test
  public void fetchInvitationTest(){
    when(session.createQuery(any())).thenReturn(query);
    when(query.list()).thenReturn(new ArrayList(Arrays.asList(Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build())));
    invitationService.fetchUserInvitationForMember("testUser");
    verify(sessionFactory, times(1)).openSession();

  }

  @Test
  public void fetchInvitationGroupTest(){
    when(session.createQuery(any())).thenReturn(query);
    when(query.list()).thenReturn(new ArrayList(Arrays.asList(Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build())));
    invitationService.fetchGroupInvitationForMember("testGroup");
    verify(sessionFactory, times(1)).openSession();

  }

}
