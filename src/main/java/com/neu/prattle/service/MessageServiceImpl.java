package com.neu.prattle.service;

import com.neu.prattle.hibernate.HibernateUtil;
import com.neu.prattle.model.Message;
import com.neu.prattle.model.User;
import java.sql.Timestamp;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class MessageServiceImpl implements MessageService {

  private static MessageService messageServiceObj;

  private SessionFactory sessionFactory;


  /***
   * UserServiceImpl is a Singleton class.
   */
  private MessageServiceImpl() {
    sessionFactory = HibernateUtil.getSessionFactory();
  }

  /**
   * Call this method to return an instance of this service.
   *
   * @return this
   */
  public static MessageService getInstance() {
    if (messageServiceObj == null) {
      messageServiceObj = new MessageServiceImpl();
    }
    return messageServiceObj;
  }

  /**
   * Responsible to persist Message to the database.
   *
   * @param message is of type Message.
   */
  @Override
  public void persistMessage(Message message) {
    Session sessionObj = sessionFactory.openSession();
    sessionObj.beginTransaction();
    sessionObj.saveOrUpdate(message);
    sessionObj.getTransaction().commit();
    sessionObj.close();
  }

  @Override
  public List<Message> fetchUnreadMessages(User toUser, Timestamp lastLogin) {
    Session session = sessionFactory.openSession();

    Query query = session.createQuery(
        "FROM Message where timestamp > :lastLogin  and is_read = 0 and to_user = \'" + toUser
            .getMemberId() + "\'");
    query.setTimestamp("lastLogin", lastLogin);
    List<Message> messages = query.list();
    session.close();
    return messages;
  }

  @Override
  public List<Message> fetchAllUnreadMessages(Timestamp lastLogin) {
    Session session = sessionFactory.openSession();

    Query query = session.createQuery(
            "FROM Message");
    List<Message> messages = query.list();
    session.close();
    return messages;
  }

  @Override
  public List<Message> fetchUserTimeRange(Timestamp from ,Timestamp to, String user){
    Session session = sessionFactory.openSession();


    Query query = session.createQuery(
            "FROM Message where timestamp > :from  and timestamp<= :to and from_user = \'" + user + "\' or to_user = \'"+ user+"\'");
    query.setParameter("from",from);
    query.setParameter("to",to);
    List<Message> messages = query.list();
    session.close();
    return messages;

  }


}
