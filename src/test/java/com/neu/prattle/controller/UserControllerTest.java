package com.neu.prattle.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neu.prattle.exceptions.UserAlreadyPresentException;
import com.neu.prattle.main.PrattleApplication;
import com.neu.prattle.model.Group;
import com.neu.prattle.model.Invite;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.InvitationService;
import com.neu.prattle.service.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

  @Mock
  private User mockUser;

  @Mock
  private Group mockGroup;

  @Mock
  private UserAlreadyPresentException userAlreadyPresentException;

  @Mock
  private UserService userService;

  @Mock
  private GroupService groupService;

  @Mock
  private InvitationService invitationService;

  @InjectMocks
  private UserController userController = new UserController();

  private Response actualResponse;

  private void callCreateUser(User user) {
    actualResponse = userController.createUserAccount(mockUser);
  }

  private void callFindUser() {
    actualResponse = userController.findUser("Test");
  }

  @Test
  public void testCreateUserSuccess() throws Exception {
    callCreateUser(mockUser);
    verify(userService, times(1)).addUser(any(User.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testCreateExistingUserFailure() throws Exception {
    doThrow(userAlreadyPresentException)
        .when(userService)
        .addUser(mockUser);
    callCreateUser(mockUser);
    verify(userService, times(1)).addUser(any(User.class));
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testFindUser() {
    Optional<User> user = Optional.of(mockUser);
    doReturn(user).when(userService).findUserById("Test");
    callFindUser();
    verify(userService, times(1)).findUserById(any(String.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testFindUserNotFound() {
    Optional<User> user = Optional.empty();
    doReturn(user).when(userService).findUserById("Test");
    callFindUser();
    verify(userService, times(1)).findUserById(any(String.class));
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testApplication() {
    PrattleApplication pa = new PrattleApplication();
    assertEquals(3, pa.getClasses().size());
  }

  @Test
  public void testsendInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").build();
    actualResponse = userController.sendInvitation(invitation);
    verify(invitationService, times(1)).persistInvitation(any(Invite.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testsendInvitation1() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    when(groupService.findGroupById(anyString())).thenReturn(Optional.of(mockGroup));
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testGroup").build();
    actualResponse = userController.sendInvitation(invitation);
    verify(invitationService, times(1)).persistInvitation(any(Invite.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testsendInvalidInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    when(groupService.findGroupById(anyString())).thenReturn(Optional.empty());
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").build();
    actualResponse = userController.sendInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    assertTrue(actualResponse.getStatus() == 409);
  }



  @Test
  public void testacceptOrRejectInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));

    Invite invitation = Invite.builder().inviteId("testUser1testUser2").fromInvite("testUser1")
        .toInvite("testUser2").isAccepted(1)
        .build();
    when(invitationService.fetchUserInvitationForMember(anyString())).thenReturn(new ArrayList<>(
        Arrays.asList(invitation)));
    actualResponse = userController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(1)).fetchUserInvitationForMember(any());
    verify(invitationService, times(1)).persistInvitation(any(Invite.class));
    verify(userService, times(1)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testInvlidAcceptOrRejectInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    when(invitationService.fetchUserInvitationForMember(anyString())).thenReturn(new ArrayList<>(
        Arrays.asList(invitation)));
    actualResponse = userController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testRejectInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(0)
        .build();
    when(invitationService.fetchUserInvitationForMember(anyString())).thenReturn(new ArrayList<>(
        Arrays.asList(invitation)));
    actualResponse = userController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidInvitation() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    when(invitationService.fetchUserInvitationForMember(anyString())).thenReturn(new ArrayList<>());
    actualResponse = userController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }


  @Test
  public void testGetFriends() {
    Optional<User> user = Optional.of(mockUser);
    User u1 = User.builder().name("u1").build();
    u1.setMemberId("u1");
    User u2 = User.builder().name("u2").build();
    u2.setMemberId("u1");
    Set<User> friends = new HashSet<>(
        Arrays.asList(u1, u2));
    when(mockUser.getMembers()).thenReturn(friends);
    doReturn(user).when(userService).findUserById("Test");
    Response response = userController.getFriends("Test");
    verify(userService, times(1)).findUserById(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals(friends.stream().map(e->e.getName()).collect(Collectors.toList()), response.getEntity());
  }

  @Test
  public void testGetFriends1() {
    Optional<User> user = Optional.empty();

    doReturn(user).when(userService).findUserById("Test");
    Response response = userController.getFriends("Test");
    verify(userService, times(1)).findUserById(any(String.class));
    assertTrue(response.getStatus() == 409);

  }


  @Test
  public void testGetAllPendingInvitation() {

    List<Invite> invites = new ArrayList<>(
        Arrays.asList(Invite.builder().inviteId("i1").build(),
            Invite.builder().inviteId("i2").build()));
    when(invitationService.fetchUserInvitationForMember("Test")).thenReturn(invites);

    Response response = userController.getAllPendingInvitation("Test");
    verify(invitationService, times(1)).fetchUserInvitationForMember(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals(invites, response.getEntity());
  }

}