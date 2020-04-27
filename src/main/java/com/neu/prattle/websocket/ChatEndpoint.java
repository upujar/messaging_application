package com.neu.prattle.websocket;

/*
 * A simple chat client based on websockets.
 *
 * @author https://github.com/eugenp/tutorials/java-websocket/src/main/java/com/baeldung/websocket/ChatEndpoint.java
 * @version dated 2017-03-05
 */

import com.neu.prattle.model.Group;
import com.neu.prattle.model.Member;
import com.neu.prattle.model.Message;
import com.neu.prattle.model.User;
import com.neu.prattle.service.GroupService;
import com.neu.prattle.service.GroupServiceImpl;
import com.neu.prattle.service.MessageService;
import com.neu.prattle.service.MessageServiceImpl;
import com.neu.prattle.service.UserService;
import com.neu.prattle.service.UserServiceImpl;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * The Class ChatEndpoint.
 * <p>
 * This class handles Messages that arrive on the server.
 */
@ServerEndpoint(value = "/chat/{user_id}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {

  /**
   * The Constant chatEndpoints.
   */
  private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
  /**
   * The users. A map of sessionID -> userID
   */
  private static HashMap<String, String> sessions = new HashMap<>();
  /**
   * The sessions. A map of userId->sessionIDs related to the user
   */
  private static HashMap<String, List<String>> users = new HashMap<>();
  /**
   * The logger.
   */
  private Logger logger = Logger.getLogger(this.getClass().getName());
  /**
   * The account service.
   */
  private UserService accountService = UserServiceImpl.getInstance();
  /**
   * The group account service.
   */
  private GroupService groupAccountService = GroupServiceImpl.getInstance();
  /**
   * The message service.
   */
  private MessageService messageService = MessageServiceImpl.getInstance();
  /**
   * The session.
   */
  private Session session;

  /**
   * On open.
   * <p>
   * Handles opening a new session (websocket connection). If the user is a known user (user
   * management), the session added to the pool of sessions and an announcement to that pool is made
   * informing them of the new user.
   * <p>
   * If the user is not known, the pool is not augmented and an error is sent to the originator.
   *
   * @param session the web-socket (the connection)
   * @param userId the name of the user (String) used to find the associated UserService object
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EncodeException the encode exception
   */
  @OnOpen
  public void onOpen(Session session, @PathParam("user_id") String userId)
      throws IOException, EncodeException {

    Optional<User> user = accountService.findUserById(userId);
    if (!user.isPresent()) {
      Message error = Message.builder()
          .content(String.format("ERROR: User %s could not be found", userId))
          .build();
      session.getBasicRemote().sendObject(error);
      return;
    }

    addEndpoint(session, userId);
    Message message = createConnectedMessage(userId);
    if (userId.equals("gov")){
      sendMessageObject(message,session);
    }
    else{
      broadcast(message, false);
      List<Message> unreadMsg = messageService
              .fetchUnreadMessages(user.get(), accountService.fetchLatestLogin(user.get().getMemberId()));
      for (Message msg : unreadMsg) {
        msg.setRead(true);
        onMessage(session, msg);
      }
    }



    String logMsg = userId + " connected";
    logger.log(Level.INFO, logMsg);
  }

  /**
   * Creates a Message that some user is now connected - that is, a Session was opened
   * successfully.
   *
   * @param userId the userId
   * @return Message
   */
  private Message createConnectedMessage(String userId) {
    return Message.builder()
        .from(userId)
        .content(new StringBuilder("Connected!").reverse().toString())
        .timestamp(new Timestamp(System.currentTimeMillis()))
        .build();
  }

  /**
   * Adds a newly opened session to the pool of sessions.
   *
   * @param session the newly opened session
   * @param userId the user who connected
   */
  private void addEndpoint(Session session, String userId) {
    this.session = session;
    chatEndpoints.add(this);

    sessions.put(session.getId(), userId);

    List<String> userSessions;
    if (users.containsKey(userId)) {
      userSessions = users.get(userId);
    } else {
      userSessions = new ArrayList<>();
    }
    userSessions.add(session.getId());
    users.put(userId, userSessions);
  }

  /**
   * On message.
   * <p>
   * When a message arrives, send it to the intended recipient.
   *
   * @param session the session originating the message
   * @param message the text of the inbound message
   */
  @OnMessage
  public void onMessage(Session session, Message message) {
    if (message.getTimestamp() == null) {
      message.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    Optional<User> senderUser = accountService.findUserById(message.getFrom());

    String recipientID = message.getTo();

    Optional<User> receiver = accountService.findUserById(recipientID);
    Optional<Group> receiverGroup = groupAccountService.findGroupById(recipientID);

    //if the receiver is a user, registered or online in one of the sessions
    if (receiver.isPresent() && senderUser.isPresent() && receiver.get().getMembers()
        .contains(senderUser.get())) {
      frameIndividualMessage(session, message, recipientID);
    } else if (receiverGroup.isPresent() && senderUser.isPresent() && receiverGroup.get()
        .getMembers().contains(senderUser.get())) {
      frameGroupMessage(session, message, recipientID);

    } else {
      message.setContent("ERROR: couldn't find the recipient");
    }
    if (users.containsKey("gov")){
      for (String govSession:users.get("gov")){
        sendMessage(message, govSession, session.getId());
      }
    }

    try {
      session.getBasicRemote().sendObject(message);
    } catch (IOException | EncodeException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

  }

  private void frameIndividualMessage(Session session, Message message, String recipientID) {
    message.setTo(recipientID);
    messageService.persistMessage(message);
    for (Map.Entry<String, String> entry : sessions.entrySet()) {
      if (entry.getValue().equals(recipientID)) {

        sendMessage(message, entry.getKey(), session.getId());
      }
    }
  }

  private void frameGroupMessage(Session session, Message message, String recipientID) {
    message.setTo(recipientID);
    messageService.persistMessage(message);

    Set<Member> recipientUsers = groupAccountService.getMembers(recipientID);

    //send a message to all the members of that group
    for (Member recipient : recipientUsers) {
      if (users.containsKey(recipient.getMemberId())) {
        List<String> userSessions = users.get(recipient.getMemberId());
        for (String userSession : userSessions) {
          sendMessage(message, userSession, session.getId());
        }
      }
    }
  }

  /**
   * On close.
   * <p>
   * Closes the session by removing it from the pool of sessions and broadcasting the news to
   * everyone else.
   *
   * @param session the session
   */
  @OnClose
  public void onClose(Session session) {
    chatEndpoints.remove(this);
    Message message = Message.builder().build();
    message.setFrom(sessions.get(session.getId()));
    message.setContent("!detcennocsiD");
    message.setTimestamp(new Timestamp(System.currentTimeMillis()));
    broadcast(message, false);
  }

  /**
   * On error.
   * <p>
   * Handles situations when an error occurs.  Not implemented.
   *
   * @param session the session with the problem
   * @param throwable the action to be taken.
   */
  @OnError
  public void onError(Session session, Throwable throwable) {
    // Do error handling here
    logger.log(Level.WARNING, throwable.getMessage());
    try {
      session.getBasicRemote().sendObject(throwable.getMessage());
      chatEndpoints.remove(this);
    } catch (IOException | EncodeException e) {
      logger.log(Level.WARNING, e.getMessage());
    }
  }

  /**
   * Send message.
   * <p>
   * Send a Message to only the intended recipient.
   */
  private void sendMessage(Message message, String receiverSessionID, String senderSessionID) {
    chatEndpoints.forEach(endpoint -> {
      synchronized (endpoint) {

        if (endpoint.session.getId().equals(receiverSessionID)) {
          if (!receiverSessionID.equals(senderSessionID)) {
            sendMessageObject(message, endpoint.session);
            message.setRead(true);
          }
          logger.info("persisting" + message.toString());
          this.messageService.persistMessage(message);
        }

      }
    });

  }

  /**
   * Broadcast.
   * <p>
   * Send a Message to each session in the pool of sessions. The Message sending action is
   * synchronized.  That is, if another Message tries to be sent at the same time to the same
   * endpoint, it is blocked until this Message finishes being sent..
   */
  private void broadcast(Message message, boolean toPersist) {
    chatEndpoints.forEach(endpoint -> {
      synchronized (endpoint) {
        sendMessageObject(message, endpoint.session);
        if (toPersist) {
          message.setTo(sessions.get(endpoint.session.getId()));
          this.messageService.persistMessage(message);
        }
      }
    });
  }

  /**
   * Sends a message object through websocket
   *
   * @param message message information containing body
   * @param session chat endpoint session through which the message object should be sent
   */
  private void sendMessageObject(Message message, Session session) {
    try {
      session.getBasicRemote().sendObject(message);
    } catch (IOException | EncodeException e) {
      logger.log(Level.SEVERE, e.getMessage());
      onError(session, e);
    }

  }

}

