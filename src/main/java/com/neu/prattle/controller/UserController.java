package com.neu.prattle.controller;

import com.neu.prattle.exceptions.UserAlreadyPresentException;
import com.neu.prattle.model.Group;
import com.neu.prattle.model.Invite;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.GroupServiceImpl;
import com.neu.prattle.service.InvitationService;
import com.neu.prattle.service.InvitationServiceImpl;
import com.neu.prattle.service.UserService;
import com.neu.prattle.service.UserServiceImpl;
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

/***
 * A Resource class responsible for handling CRUD operations
 * on User objects.
 *
 * @author CS5500 Fall 2019 Teaching staff
 * @version dated 2019-10-06
 */
@Path(value = "/user")
public class UserController {

  // Usually Dependency injection will be used to inject the service at run-time
  private UserService accountService = UserServiceImpl.getInstance();

  private GroupService groupService = GroupServiceImpl.getInstance();

  private InvitationService invitationService = InvitationServiceImpl.getInstance();


  /***
   * Handles a HTTP POST request for user creation
   *
   * @param user -> The User object decoded from the payload of POST request.
   * @return -> A Response indicating the outcome of the requested operation.
   */
  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUserAccount(User user) {
    try {
      accountService.addUser(user);
    } catch (UserAlreadyPresentException e) {
      return Response.status(409).build();
    }

    return Response.ok().build();
  }

  /***
   *
   */
  @GET
  @Path("/query")
  @Produces(MediaType.TEXT_PLAIN)
  @JsonIgnore
  @Fetch(FetchMode.SUBSELECT)
  public Response findUser(@QueryParam("userid") String user) {
    Optional<User> found = accountService.findUserById(user);
    if (found.isPresent()) {
      return Response.ok().entity(found.get().getPassword())
          .type(MediaType.TEXT_PLAIN).build();

    } else {
      return Response.status(409).build();
    }
  }

  @GET
  @Path("/get_friends")
  @Produces(MediaType.APPLICATION_JSON)
  @JsonIgnore
  @Fetch(FetchMode.SUBSELECT)
  public Response getFriends(@QueryParam("userid") String user) {
    Optional<User> found = accountService.findUserById(user);
    if (found.isPresent()) {
      List<String> members = found.get().getMembers().stream().map(User::getMemberId).collect(
          Collectors.toList());
      return Response.ok().entity(members)
          .type(MediaType.APPLICATION_JSON).build();

    } else {
      return Response.status(409).build();
    }


  }




  @POST
  @Path("/invite")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendInvitation(Invite invitation) {

    Optional<User> foundUser = accountService.findUserById(invitation.getToInvite());
    Optional<Group> foundGroup = groupService.findGroupById(invitation.getToInvite());
    if (foundUser.isPresent() || foundGroup.isPresent()) {

      invitationService.persistInvitation(invitation);
      return Response.ok().type(MediaType.APPLICATION_JSON).build();

    } else {
      return Response.status(409).build();
    }
  }

  @POST
  @Path("/invitation_respond")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response acceptOrRejectInvitation(Invite invitation) {

    List<Invite> invites = invitationService.fetchUserInvitationForMember(invitation.getToInvite());

    Optional<User> found = accountService.findUserById(invitation.getToInvite());

    if (invites.contains(invitation) && found.isPresent() && invitation.getIsAccepted() == 1) {

      invitationService.persistInvitation(invitation);
      accountService.addUserTofriendList(invitation.getFromInvite(), invitation.getToInvite());
      return Response.ok().type(MediaType.APPLICATION_JSON).build();

    } else {
      return Response.status(409).build();
    }
  }

  @GET
  @Path("/get_user_invitation")
  @Produces(MediaType.APPLICATION_JSON)
  @JsonIgnore
  @Fetch(FetchMode.SUBSELECT)
  public Response getAllPendingInvitation(@QueryParam("userid") String user) {

    return Response.ok().entity(invitationService.fetchUserInvitationForMember(user))
        .type(MediaType.APPLICATION_JSON).build();


  }


}

