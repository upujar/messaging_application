package com.neu.prattle.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestTest {

  private Request request;

  @Before
  public void setUp() {
    request = Request.builder().build();
  }

  @Test
  public void testGetSetRequestId() {
    assertTrue(request.getRequestId()==null);
    request.setRequestId(1);
    assertTrue(request.getRequestId()==1);
  }

  @Test
  public void testGetSetInitiator() {
    assertTrue(request.getInitiator()==null);
    request.setInitiator("initiator");
    assertTrue(request.getInitiator().contentEquals("initiator"));

  }

  @Test
  public void testGetSetRegarding() {
    assertTrue(request.getRegarding()==null);
    request.setRegarding("regarding");
    assertTrue(request.getRegarding().contentEquals("regarding"));
  }

  @Test
  public void testGetSetApprover() {
    assertTrue(request.getApprover()==null);
    request.setApprover("approver");
    assertTrue(request.getApprover().contentEquals("approver"));
  }

  @Test
  public void getPlace() {
    assertTrue(request.getPlace()==null);
    request.setPlace("group1");
    assertTrue(request.getPlace().contentEquals("group1"));
  }

  @Test
  public void testIsApproved() {
    assertTrue(request.isApproved()==false);
    request.setApproved(true);
    assertTrue(request.isApproved());
  }

  @Test
  public void testIsRejected() {
    assertTrue(request.isApproved()==false);
    request.setRejected(true);
    assertTrue(request.isRejected());
  }

  @Test
  public void testGetSetType() {
    assertFalse(request.getType()=='E');
    request.setType('E');
    assertTrue(request.getType()=='E');
  }

}