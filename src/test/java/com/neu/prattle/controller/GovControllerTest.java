package com.neu.prattle.controller;

import com.neu.prattle.exceptions.GroupAlreadyPresentException;
import com.neu.prattle.model.Message;
import com.neu.prattle.model.Request;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.InvitationService;
import com.neu.prattle.service.MessageService;
import com.neu.prattle.service.RequestService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GovControllerTest {

  @Mock
  InvitationService invitationService;
  @Mock
  private Message mockMessage;
  @Mock
  private User mockUser;
  @Mock
  private GroupAlreadyPresentException groupAlreadyPresentException;
  @Mock
  private GroupService groupService;
  @Mock
  private MessageService messageService;
  @Mock
  private RequestService requestService;
  @Mock
  private Request mockRequest;
  @InjectMocks
  private GovController govController = new GovController();

  private Response actualResponse;


  @Test
  public void testGovFilter() throws ParseException {
    List<Message> messages = new ArrayList<>();
    messages.add(mockMessage);
    when(messageService.fetchUserTimeRange(any(), any(), any())).thenReturn(messages);
    actualResponse = govController.findMessages("01/01/2020", "01/09/2020", "test");
    assertTrue(actualResponse.getStatus() == 200);

  }

  @Test
  public void testNoMessages() throws ParseException {
    when(messageService.fetchUserTimeRange(any(), any(), any())).thenReturn(null);
    actualResponse = govController.findMessages("01/01/2020", "01/09/2020", "test");
    assertTrue(actualResponse.getStatus() == 409);

  }
}
