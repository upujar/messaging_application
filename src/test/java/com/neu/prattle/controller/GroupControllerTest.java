package com.neu.prattle.controller;

import com.neu.prattle.exceptions.GroupAlreadyPresentException;
import com.neu.prattle.exceptions.GroupDoesNotExistException;
import com.neu.prattle.model.Group;
import com.neu.prattle.model.Member;
import com.neu.prattle.viewmodel.GroupMember;
import com.neu.prattle.model.Invite;
import com.neu.prattle.model.Request;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.InvitationService;
import com.neu.prattle.service.RequestService;
import com.neu.prattle.service.UserService;
import com.neu.prattle.viewmodel.ModeratorGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupControllerTest {

  @Mock
  private Group mockGroup;

  @Mock
  private User mockUser;

  @Mock
  private GroupAlreadyPresentException groupAlreadyPresentException;

  @Mock
  private GroupService groupService;

  @Mock
  private UserService userService;

  @Mock
  private RequestService requestService;

  @Mock
  private Request mockRequest;

  @Mock
  InvitationService invitationService;

  @InjectMocks
  private GroupController groupController = new GroupController();

  private Response actualResponse;

  private void callCreateGroup(Group group) {
    actualResponse = groupController.createGroupAccount(mockGroup);
  }

  @Test
  public void testCreateGroupSuccess() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testCreateExistingGroupFailure()  {
    when(userService.isModerators(any())).thenReturn(true);
    doThrow(groupAlreadyPresentException)
            .when(groupService)
            .addGroup(mockGroup);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testAddMemberToExistingGroupNonUser() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    when(groupService.findGroupById("group1")).thenReturn(Optional.of(mockGroup));
    when(groupService.findGroupById("member1")).thenReturn(Optional.empty());
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    GroupMember member = GroupMember.builder().memberId("member1").groupId("group1").build();
    actualResponse = groupController.addGroupUser(member);
    assertTrue(actualResponse.getStatus() == 407);
  }

  @Test
  public void testAddMemberToNonExistingGroup() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    when(groupService.findGroupById(any())).thenReturn(Optional.empty());
    GroupMember member = new GroupMember();
    member.setGroupId("group1");
    member.setMemberId("member1");
    actualResponse = groupController.addGroupUser(member);
    assertTrue(actualResponse.getStatus() == 408);
  }

  @Test
  public void testAddMemberValid() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    GroupMember member = GroupMember.builder().memberId("member1").groupId("group1").build();
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    when(groupService.addMemberToGroupDB(any(),any())).thenReturn(true);
    actualResponse = groupController.addGroupUser(member);
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testAddMemberSubGroupValid() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    GroupMember member = GroupMember.builder().memberId("member1").groupId("group1").build();
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    when(groupService.addMemberToGroupDB(any(),any())).thenReturn(true);
    actualResponse = groupController.addGroupUser(member);
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testDeleteGroupValid() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(groupService.deleteGroup(any())).thenReturn(true);
    actualResponse = groupController.deleteGroup("group1");
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testDeleteGroupIValid() {
    when(userService.isModerators(any())).thenReturn(true);
    callCreateGroup(mockGroup);
    verify(groupService, times(1)).addGroup(any(Group.class));
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(groupService.deleteGroup(any())).thenThrow(new GroupDoesNotExistException("not existing"));
    actualResponse = groupController.deleteGroup("group1");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidModerators(){
    when(userService.isModerators(any())).thenReturn(false);
    callCreateGroup(mockGroup);
    assertTrue(actualResponse.getStatus() == 408);
  }

  @Test
  public void testCreateRequest(){
    doNothing().when(requestService).createRequest(any());
    Response  response = groupController.evictRequest(mockRequest);
    assertTrue(response.getStatus() == 200);
  }

  @Test
  public void testApproveEvictRequest(){
    doNothing().when(requestService).approveRequest(any());
    when(mockRequest.getType()).thenReturn('E');
    Response  response = groupController.approveRequest(mockRequest);
    assertTrue(response.getStatus() == 200);
  }

  @Test
  public void testApproveNonEvictRequest(){
    doNothing().when(requestService).createRequest(any());
    when(mockRequest.getType()).thenReturn('X');
    Response  response = groupController.approveRequest(mockRequest);
    assertTrue(response.getStatus() == 200);
  }

  @Test
  public void testAcceptOrRejectInvitation() throws Exception {
    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    when(mockUser.getMemberId()).thenReturn("testUser3");
    when(mockGroup.getModeratorIds()).thenReturn("testUser3");
    Invite invitation = Invite.builder().inviteId("testUser1testUser2group1").groupId("group1").fromInvite("testUser1").toInvite("testUser2").approverId("testUser3").isAccepted(1)
        .build();
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>(
        Arrays.asList(invitation)));
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(1)).fetchGroupInvitationForMember(any());
    verify(invitationService, times(1)).persistInvitation(any(Invite.class));
    assertTrue(actualResponse.getStatus() == 200);
  }

  @Test
  public void testInvalidAcceptOrRejectInvitation() throws Exception {
    when(groupService.findGroupById(any())).thenReturn(Optional.empty());
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    Invite invitation = Invite.builder().inviteId("testUser1testUser2group1").fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>(
        Arrays.asList(invitation)));
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidAcceptOrRejectInvitation1() throws Exception {
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>());
    when(groupService.findGroupById(any())).thenReturn(Optional.empty());
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidAcceptOrRejectInvitation2() throws Exception {

    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.empty());
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").isAccepted(1)
        .build();
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>(Arrays.asList(invitation)));
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidAcceptOrRejectInvitation3() throws Exception {

    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    when(mockUser.getMemberId()).thenReturn("testUser3");
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").approverId("testUser3").isAccepted(0)
        .build();
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>(Arrays.asList(invitation)));
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testInvalidAcceptOrRejectInvitation4() throws Exception {

    when(groupService.findGroupById(any())).thenReturn(Optional.of(mockGroup));
    when(userService.findUserById(any())).thenReturn(Optional.of(mockUser));
    when(mockUser.getMemberId()).thenReturn("testUser3");
    when(mockGroup.getModeratorIds()).thenReturn("testUser4");
    Invite invitation = Invite.builder().fromInvite("testUser1").toInvite("testUser2").approverId("testUser3").isAccepted(1)
        .build();
    when(invitationService.fetchGroupInvitationForMember(anyString())).thenReturn(new ArrayList<>(Arrays.asList(invitation)));
    actualResponse = groupController.acceptOrRejectInvitation(invitation);
    verify(invitationService, times(0)).persistInvitation(any(Invite.class));
    verify(userService, times(0)).addUserTofriendList("testUser1", "testUser2");
    assertTrue(actualResponse.getStatus() == 409);
  }

  @Test
  public void testGetAllActiveRequestsForModerator(){
    List<Request> requestList = new ArrayList<>();
    when(requestService.fetchAllActiveEvictRequestsForUser(any())).thenReturn(requestList);
    Response response = groupController.getAllActiveRequestsForModerator("moderatorId");
    assertTrue(response.getEntity()==requestList);
  }

  @Test
  public void testRejectResponse(){
    doNothing().when(requestService).rejectRequest(any());
    groupController.rejectRequest(Request.builder().build());
    verify(requestService,times(1)).rejectRequest(any());
  }


  @Test
  public void testAddModeratorResponse(){
    when(groupService.addModeratorToGroup(anyString(),anyString(),anyString())).thenReturn(true);
    ModeratorGroup moderatorGroup = ModeratorGroup.builder().build();
    moderatorGroup.setGroupId("G1");
    moderatorGroup.setInitiatorId("I1");
    moderatorGroup.setModeratorId("M1");
    Response res = groupController.createGroupModerator(moderatorGroup);
    verify(groupService,times(1)).addModeratorToGroup(anyString(),anyString(),anyString());
    assertTrue(res.getStatus()==200);
  }

  @Test
  public void testAddModeratorExceptionResponse(){
    when(groupService.addModeratorToGroup(anyString(),anyString(),anyString())).thenThrow(new NotAuthorizedException("xyz"));
    ModeratorGroup moderatorGroup = new ModeratorGroup();
    moderatorGroup.setGroupId("G1");
    moderatorGroup.setInitiatorId("I1");
    moderatorGroup.setModeratorId("M1");
    Response res = groupController.createGroupModerator(moderatorGroup);
    verify(groupService,times(1)).addModeratorToGroup(anyString(),anyString(),anyString());
    assertTrue(res.getStatus()==409);
  }

  @Test
  public void testTransferModeratorResponse(){
    when(groupService.addModeratorToGroup(anyString(),anyString(),anyString())).thenReturn(true);
    ModeratorGroup moderatorGroup = new ModeratorGroup();
    moderatorGroup.setGroupId("G1");
    moderatorGroup.setInitiatorId("I1");
    moderatorGroup.setModeratorId("M1");
    Response res = groupController.transferModerator(moderatorGroup);
    verify(groupService,times(1)).transferModeratorResponsibility(anyString(),anyString(),anyString());
    assertTrue(res.getStatus()==200);
  }

  @Test
  public void testTransferModeratorExceptionResponse(){
    doThrow(new NotAuthorizedException("xyz")).when(groupService).transferModeratorResponsibility(anyString(),anyString(),anyString());
    ModeratorGroup moderatorGroup = new ModeratorGroup();
    moderatorGroup.setGroupId("G1");
    moderatorGroup.setInitiatorId("I1");
    moderatorGroup.setModeratorId("M1");
    Response res = groupController.transferModerator(moderatorGroup);
    verify(groupService,times(1)).transferModeratorResponsibility(anyString(),anyString(),anyString());
    assertTrue(res.getStatus()==409);
  }

  @Test
  public void testGetAllPendingInvitation() {

    List<Invite> invites = new ArrayList<>(
        Arrays.asList(Invite.builder().inviteId("i1").build(),
            Invite.builder().inviteId("i2").build()));
    when(invitationService.fetchGroupInvitationForMember("g1")).thenReturn(invites);
    when(userService.findUserById("Test")).thenReturn(Optional.of(mockUser));
    List<String> list = new ArrayList<>(Arrays.asList("g1"));
    when(groupService.getGroupsForUser("Test")).thenReturn(list);
    when(groupService.findGroupById("g1")).thenReturn(Optional.of(mockGroup));
    when(mockGroup.getModeratorIds()).thenReturn("t1,Test");
    Response response = groupController.getAllPendingInvitation("Test");
    verify(invitationService, times(1)).fetchGroupInvitationForMember(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals(invites.toString(), response.getEntity().toString());
  }


  @Test
  public void testGetAllPendingInvitation1() {


    when(userService.findUserById("Test")).thenReturn(Optional.empty());

    Response response = groupController.getAllPendingInvitation("Test");
    verify(invitationService, times(0)).fetchGroupInvitationForMember(any(String.class));
    assertTrue(response.getStatus() == 409);

  }

  @Test
  public void testGetAllPendingInvitation2() {

    List<Invite> invites = new ArrayList<>(
        Arrays.asList(Invite.builder().inviteId("i1").build(),
            Invite.builder().inviteId("i2").build()));
    when(invitationService.fetchGroupInvitationForMember("g1")).thenReturn(invites);
    when(userService.findUserById("Test")).thenReturn(Optional.of(mockUser));
    List<String> list = new ArrayList<>(Arrays.asList("g1"));
    when(groupService.getGroupsForUser("Test")).thenReturn(list);
    when(groupService.findGroupById("g1")).thenReturn(Optional.empty());
    when(mockGroup.getModeratorIds()).thenReturn("t1,Test");
    Response response = groupController.getAllPendingInvitation("Test");
    verify(invitationService, times(0)).fetchGroupInvitationForMember(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals("[]", response.getEntity().toString());
  }

  @Test
  public void testGetAllPendingInvitation3() {

    List<Invite> invites = new ArrayList<>(
        Arrays.asList(Invite.builder().inviteId("i1").build(),
            Invite.builder().inviteId("i2").build()));
    when(invitationService.fetchGroupInvitationForMember("g1")).thenReturn(invites);
    when(userService.findUserById("Test")).thenReturn(Optional.of(mockUser));
    List<String> list = new ArrayList<>(Arrays.asList("g1"));
    when(groupService.getGroupsForUser("Test")).thenReturn(list);
    when(groupService.findGroupById("g1")).thenReturn(Optional.of(mockGroup));
    when(mockGroup.getModeratorIds()).thenReturn("t1,t2");
    Response response = groupController.getAllPendingInvitation("Test");
    verify(invitationService, times(0)).fetchGroupInvitationForMember(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals("[]", response.getEntity().toString());
  }



  @Test
  public void testGetGroups() {
    List<String> list = new ArrayList<>(Arrays.asList("g1","g2"));
    when(groupService.getGroupsForUser("Test")).thenReturn(list);
    Response response = groupController.getGroups("Test");
    verify(groupService, times(1)).getGroupsForUser(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals(list,response.getEntity());

  }

  @Test
  public void testGetModerators() {
    when(groupService.findGroupById("Test")).thenReturn(Optional.of(mockGroup));
    when(mockGroup.getModeratorIds()).thenReturn("a1,a2");

    Response response = groupController.getAllModerators("Test");
    verify(groupService, times(1)).findGroupById(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals("a1,a2",response.getEntity());

  }

  @Test
  public void testGetModerators1() {
    when(groupService.findGroupById("Test")).thenReturn(Optional.empty());


    Response response = groupController.getAllModerators("Test");
    verify(groupService, times(1)).findGroupById(any(String.class));
    assertTrue(response.getStatus() == 409);


  }

  @Test
  public void testGetMembers() {
    when(groupService.findGroupById("Test")).thenReturn(Optional.of(mockGroup));
    User u1 = User.builder().name("u1").build();
    u1.setMemberId("u1");
    User u2 = User.builder().name("u2").build();
    u2.setMemberId("u2");
    Set<Member> set = new HashSet<>(Arrays.asList(u1,u2));
    when(mockGroup.getMembers()).thenReturn(set);

    Response response = groupController.getAllMembers("Test");
    verify(groupService, times(1)).findGroupById(any(String.class));
    assertTrue(response.getStatus() == 200);
    assertEquals(set.stream().map(e -> e.getMemberId()).collect(
        Collectors.toList()),response.getEntity());

  }

  @Test
  public void testGetMembers1() {
    when(groupService.findGroupById("Test")).thenReturn(Optional.empty());


    Response response = groupController.getAllMembers("Test");
    verify(groupService, times(1)).findGroupById(any(String.class));
    assertTrue(response.getStatus() == 409);

  }

  @Test
  public void testGetAllGroupsWhereCurrentUserIsModerator(){
    List<String> groupIdList = new ArrayList<>();
    when(groupService.getGroupIdsWhereUserIsModerator(any())).thenReturn(groupIdList);
    Response response = groupController.getGroupsWhereUserIsModerator("moderatorId");
    assertTrue(response.getEntity()==groupIdList);
  }
}