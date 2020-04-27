package com.neu.prattle.websocket;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neu.prattle.model.Group;
import com.neu.prattle.model.Member;
import com.neu.prattle.model.Message;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.MessageService;
import com.neu.prattle.service.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChatEndpointTest {

  @Mock
  UserService mockUserService;

  @Mock
  MessageService messageService;

  @Mock
  User mockUser1;

  @Mock
  User mockUser2;

  @Mock
  RemoteEndpoint.Basic basic;

  @Mock
  Session mockSession;

  @Mock
  GroupService groupService;

  @Mock
  Group mockGroup;

  @Mock
  Message mockMessage;

  @Mock
  IOException ioException;

  @InjectMocks
  ChatEndpoint chatEndpointToTest = new ChatEndpoint();

  private String testUser1;
  private String testUser2;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testUser1 = "testUser1";
    testUser2 = "testUser2";

    when(mockUserService.findUserById(testUser1))
        .thenReturn(java.util.Optional.ofNullable(mockUser1));
    when(mockUserService.findUserById(testUser2))
        .thenReturn(java.util.Optional.ofNullable(mockUser2));
    when(mockSession.getBasicRemote()).thenReturn(basic);
    doNothing().when(messageService).persistMessage(any(Message.class));
    when(mockUser1.getMemberId()).thenReturn(testUser1);
    when(mockUser2.getMemberId()).thenReturn(testUser2);
    //when()
    //User testUserObject = User.builder().name(testUser1).build();
    // testUserObject.setMemberId(testUser1);
    when(mockUser1.getMembers()).thenReturn(new HashSet<>(Arrays.asList(mockUser2)));
    when(mockGroup.getMembers()).thenReturn(new HashSet<>(Arrays.asList(mockUser1, mockUser2)));
    when(groupService.getMembers(anyString())).thenReturn(new HashSet<>(Arrays.asList(mockUser1)));

  }

  @Test
  public void testOnOpenWithExistingUser() throws IOException, EncodeException {
    chatEndpointToTest.onOpen(mockSession, testUser1);
    verify(basic, times(1)).sendObject(Matchers.any());
  }

  @Test
  public void testOnOpenWithNonExistingUser() throws IOException, EncodeException {
    when(mockUserService.findUserById(testUser1)).thenReturn(java.util.Optional.ofNullable(null));
    chatEndpointToTest.onOpen(mockSession, testUser1);
    verify(basic, times(1)).sendObject(Matchers.any());
  }

  @Test
  public void testBroadcastIOException() throws IOException, EncodeException {
    doThrow(ioException)
        .when(basic)
        .sendObject(any());
    chatEndpointToTest.onOpen(mockSession, testUser1);
    verify(ioException, times(4)).getMessage();
  }

  @Test
  public void testOnCloseWithOneUser() throws IOException, EncodeException {
    chatEndpointToTest.onOpen(mockSession, testUser1);
    verify(basic, times(1)).sendObject(Matchers.any());
//    doNothing().when(messageService).persistMessage(mockMessage);
    chatEndpointToTest.onClose(mockSession);
    verify(basic, times(1)).sendObject(Matchers.any());
  }

  @Test
  public void testOnMessage() throws IOException, EncodeException {
    when(mockSession.getId()).thenReturn(testUser2);

   /* when(mockUserService.findUserById(mockUser1.getMemberId()))
        .thenReturn(java.util.Optional.ofNullable(mockUser1));*/

    when(mockMessage.getFrom()).thenReturn(testUser2);
    chatEndpointToTest.onOpen(mockSession, testUser2);
    verify(basic, times(1)).sendObject(Matchers.any());
    when(mockMessage.getTo()).thenReturn(testUser1);
    chatEndpointToTest.onMessage(mockSession, mockMessage);
    when(mockMessage.getTo()).thenReturn("Wrong");
    when(mockUserService.findUserById("Wrong"))
        .thenReturn(java.util.Optional.ofNullable(null));
    when(groupService.findGroupById("Wrong"))
        .thenReturn(java.util.Optional.ofNullable(null));
    chatEndpointToTest.onMessage(mockSession, mockMessage);
    verify(basic, times(3)).sendObject(Matchers.any());
  }

  @Test
  public void testSendMessageIOException() throws IOException, EncodeException {
    when(mockSession.getId()).thenReturn(testUser2);
    when(mockMessage.getFrom()).thenReturn(testUser2);
    chatEndpointToTest.onOpen(mockSession, testUser2);
    when(mockMessage.getTo()).thenReturn(testUser1);
    doThrow(ioException)
        .when(basic)
        .sendObject(any());
    chatEndpointToTest.onMessage(mockSession, mockMessage);
    verify(ioException, times(1)).getMessage();
  }

  @Test
  public void onError() throws IOException, EncodeException {
    chatEndpointToTest.onError(mockSession, new IOException());
    verify(basic, times(1)).sendObject(Matchers.any());
  }

  @Test
  public void testUnreadMessages() throws IOException, EncodeException {

    when(messageService.fetchUnreadMessages(any(), any())).thenReturn(new ArrayList<>(
        Arrays.asList(mockMessage, mockMessage, mockMessage)));
    when(mockSession.getId()).thenReturn(testUser2);
    when(mockMessage.getFrom()).thenReturn(testUser2);
    when(mockMessage.getTo()).thenReturn(testUser1);
    chatEndpointToTest.onOpen(mockSession, testUser2);
    verify(basic, times(4)).sendObject(Matchers.any());

    chatEndpointToTest.onMessage(mockSession, mockMessage);

    verify(basic, times(5)).sendObject(Matchers.any());
  }

  @Test
  public void testGroupRecipient() throws IOException, EncodeException {
    when(mockSession.getId()).thenReturn(testUser1);
    when(mockMessage.getFrom()).thenReturn(testUser1);
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(mockMessage.getTo()).thenReturn("group1");
    when(mockUserService.findUserById("group1")).thenReturn(Optional.empty());
    when(groupService.findGroupById("group1")).thenReturn(Optional.of(mockGroup));
    doNothing().when(messageService).persistMessage(mockMessage);
    Set<Member> members = new HashSet<>();
   /* User user1 = User.builder().build();
    user1.setMemberId(testUser1);
    members.add(user1);*/
    //when(mockUserService.findUserById(testUser1)).thenReturn(Optional.of(mockUser1));
    chatEndpointToTest.onOpen(mockSession, testUser1);
    //when(groupService.getMembers(any())).thenReturn(members);

    chatEndpointToTest.onMessage(mockSession, mockMessage);
    verify(basic, times(2)).sendObject(Matchers.any());
  }


}
