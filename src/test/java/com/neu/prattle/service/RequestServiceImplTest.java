package com.neu.prattle.service;

import com.neu.prattle.model.Request;

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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestServiceImplTest {

  @Mock
  private Session session;

  @Mock
  private SessionFactory mockSessionFactory;

  @Mock
  private Transaction transaction;

  @Mock
  private Request mockRequest;

  @Mock
  private Query query;

  @InjectMocks
  private RequestService as = RequestServiceImpl.getInstance();

  private List<Request> list;

  @Before
  public void setUp() {
    when(mockSessionFactory.openSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(null);
    when(session.getTransaction()).thenReturn(transaction);
    doNothing().when(transaction).commit();
    when(session.close()).thenReturn(null);

    list = new ArrayList<>();
    when(session.createQuery(any())).thenReturn(query);
    when(query.list()).thenReturn(list);
  }

  @Test
  @Order(1)
  public void testCreateRequest() {
    when(session.save(mockRequest)).thenReturn(null);
    as.createRequest(mockRequest);
    verify(session, times(1)).save(mockRequest);
  }

  @Test
  @Order(2)
  public void testApproveRequest() {
    doNothing().when(session).saveOrUpdate(any());
    Request request = Request.builder().build();
    assertFalse(request.isApproved());
    as.approveRequest(request);
    assertTrue(request.isApproved());
    verify(session, times(1)).saveOrUpdate(request);
  }

  @Test
  @Order(3)
  public void testRejectRequest() {
    doNothing().when(session).saveOrUpdate(any());
    Request request = Request.builder().build();
    assertFalse(request.isRejected());
    as.rejectRequest(request);
    assertTrue(request.isRejected());
    verify(session, times(1)).saveOrUpdate(request);
  }

  @Test
  @Order(4)
  public void testQueryRequest() {
    assertTrue(list==as.fetchAllActiveEvictRequestsForUser(""));
  }

}