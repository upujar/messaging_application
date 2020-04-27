package com.neu.prattle.controller;

import com.neu.prattle.exceptions.GroupAlreadyPresentException;
import com.neu.prattle.exceptions.GroupDoesNotExistException;
import com.neu.prattle.model.Group;
import com.neu.prattle.model.Invite;
import com.neu.prattle.model.Member;
import com.neu.prattle.model.Request;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.GroupServiceImpl;
import com.neu.prattle.service.InvitationService;
import com.neu.prattle.service.InvitationServiceImpl;
import com.neu.prattle.service.RequestService;
import com.neu.prattle.service.RequestServiceImpl;
import com.neu.prattle.service.UserService;
import com.neu.prattle.service.UserServiceImpl;
import com.neu.prattle.viewmodel.GroupMember;
import com.neu.prattle.viewmodel.ModeratorGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Path(value = "/group")
public class GroupController {

    private GroupService groupService = GroupServiceImpl.getInstance();
    private UserService userService = UserServiceImpl.getInstance();
    private RequestService requestService = RequestServiceImpl.getInstance();
    private InvitationService invitationService = InvitationServiceImpl.getInstance();

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGroupAccount(Group group) {
        try {
            if (userService.isModerators(group.getModeratorIds())) {
                groupService.addGroup(group);
            } else {
                return Response.status(408).build();
            }
        } catch (GroupAlreadyPresentException e) {
            return Response.status(409).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/addUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addGroupUser(GroupMember groupMember) {

        Optional<Group> optionalGroup = groupService.findGroupById(groupMember.getGroupId());
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            Optional<User> user = userService.findUserById(groupMember.getMemberId());
            Optional<Group> subgroup = groupService.findGroupById(groupMember.getMemberId());
            if (user.isPresent()) {
                groupService.addMemberToGroupDB(group.getMemberId(),  user.get());
            } else if (subgroup.isPresent()) {
                groupService.addMemberToGroupDB(group.getMemberId(), subgroup.get());
            } else {
                return Response.status(407).build();
            }
        } else {
            return Response.status(408).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @JsonIgnore
    public Response deleteGroup(String groupId) {
        try {
            groupService.deleteGroup(groupId);
        } catch (GroupDoesNotExistException e) {
            return Response.status(409).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/createEvictRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response evictRequest(Request request) {
        request.setType('E');
        request.setApproved(false);
        requestService.createRequest(request);
        return Response.ok().build();
    }

    @POST
    @Path("/approveRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response approveRequest(Request request) {
        requestService.approveRequest(request);
        char typeOfRequest = request.getType();
        if (typeOfRequest == 'E') {
            groupService.removeMemberFromGroup(request.getRegarding(), request.getPlace());
        }
        return Response.ok().build();
    }

    @POST
    @Path("/invitation_respond")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response acceptOrRejectInvitation(Invite invitation) {

        List<Invite> invites = invitationService.fetchGroupInvitationForMember(invitation.getGroupId());

        Optional<Group> found = groupService.findGroupById(invitation.getGroupId());
        Optional<User> moderator = userService.findUserById(invitation.getApproverId());
        if (invites.contains(invitation) && found.isPresent() && moderator.isPresent()
                && invitation.getIsAccepted() == 1 && found.get().getModeratorIds()
                .contains(moderator.get().getMemberId())) {
            invitationService.persistInvitation(invitation);
            return addGroupUser(
                    GroupMember.builder().groupId(invitation.getGroupId()).memberId(invitation.getToInvite())
                            .build());

        } else {
            return Response.status(409).build();
        }
    }

    @POST
    @Path("/rejectRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rejectRequest(Request request) {
        requestService.rejectRequest(request);
        return Response.ok().build();
    }

    /***
     *
     */
    @GET
    @Path("/requests")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    public Response getAllActiveRequestsForModerator(@QueryParam("memberId") String moderatorId) {
        List<Request> requestList = requestService.fetchAllActiveEvictRequestsForUser(moderatorId);
        return Response.ok().entity(requestList)
                .type(MediaType.APPLICATION_JSON).build();

    }

    @POST
    @Path("/addModerator")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGroupModerator(ModeratorGroup moderatorGroup) {
        try {
            groupService.addModeratorToGroup(moderatorGroup.getModeratorId(), moderatorGroup.getGroupId(),
                    moderatorGroup.getInitiatorId());
        } catch (Exception ex) {
            return Response.status(409).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/transferModerator")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transferModerator(ModeratorGroup moderatorGroup) {
        try {
            groupService.transferModeratorResponsibility(moderatorGroup.getModeratorId(),
                    moderatorGroup.getGroupId(), moderatorGroup.getInitiatorId());
        } catch (Exception ex) {
            return Response.status(409).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/get_groups")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    public Response getGroups(@QueryParam("userid") String user) {

        return Response.ok().entity(groupService.getGroupsForUser(user))
                .type(MediaType.APPLICATION_JSON).build();


    }

    @GET
    @Path("/get_all_invitation")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    public Response getAllPendingInvitation(@QueryParam("userid") String user) {
        Optional<User> userOptional = userService.findUserById(user);
        if (userOptional.isPresent()) {
            List<String> groups = groupService.getGroupsForUser(user);
            List<Invite> invites = new ArrayList<>();
            for (String group : groups) {
                Optional<Group> groupOptional = groupService.findGroupById(group);
                if (groupOptional.isPresent() && groupOptional.get().getModeratorIds().contains(user)) {
                    invites.addAll(invitationService.fetchGroupInvitationForMember(group));
                }
            }
            return Response.ok().entity(invites)
                    .type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(409).build();

    }

    @GET
    @Path("/get_moderators")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    public Response getAllModerators(@QueryParam("groupid") String groupid) {
        Optional<Group> group = groupService.findGroupById(groupid);
        if (group.isPresent()) {
            return Response.ok().entity(group.get().getModeratorIds())
                    .type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(409).build();
    }

    @GET
    @Path("/get_all_members")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    public Response getAllMembers(@QueryParam("groupid") String groupid) {
        Optional<Group> group = groupService.findGroupById(groupid);
        if (group.isPresent()) {
            List<String> allMemberIds = group.get().getMembers().stream().map(Member::getMemberId).collect(
                    Collectors.toList());
            return Response.ok()
                    .entity(allMemberIds).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(409).build();
    }

    /***
     * gets all groups where current user is moderator.
     */
    @GET
    @Path("/getGroupsWhereUserIsModerator")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonIgnore
    public Response getGroupsWhereUserIsModerator(@QueryParam("memberId") String memberId) {
        List<String> requestList = groupService.getGroupIdsWhereUserIsModerator(memberId);
        return Response.ok().entity(requestList)
                .type(MediaType.APPLICATION_JSON).build();

    }

}
