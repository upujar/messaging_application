package com.neu.prattle.service;

import com.neu.prattle.model.Message;
import com.neu.prattle.model.User;
import java.sql.Timestamp;
import java.util.List;

public interface MessageService {

  /**
   * Responsible to persist Message to the database.
   *
   * @param message is of type Message.
   */
  void persistMessage(Message message);

  List<Message> fetchUnreadMessages(User toUser, Timestamp lastLogin);

  List<Message> fetchAllUnreadMessages(Timestamp lastLogin);

  List<Message> fetchUserTimeRange(Timestamp from ,Timestamp to, String user);
}
