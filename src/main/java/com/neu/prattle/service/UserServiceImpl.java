package com.neu.prattle.service;


import com.neu.prattle.exceptions.UserAlreadyPresentException;
import com.neu.prattle.hibernate.HibernateUtil;
import com.neu.prattle.model.Device;
import com.neu.prattle.model.User;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/***
 * Implementation of {@link UserService}
 *
 * It stores the user accounts in-memory, which means any user accounts
 * created will be deleted once the application has been restarted.
 *
 * @author CS5500 Fall 2019 Teaching staff
 * @version dated 2019-10-06
 */
public class UserServiceImpl implements UserService {

  private static UserService accountService;

  static {
    accountService = new UserServiceImpl();
  }

  private SessionFactory sessionFactory;
  private Map<String, User> userMap;


  /***
   * UserServiceImpl is a Singleton class.
   */
  private UserServiceImpl() {
    sessionFactory = HibernateUtil.getSessionFactory();
    userMap = new HashMap<>();
  }

  /**
   * Call this method to return an instance of this service.
   *
   * @return this
   */
  public static UserService getInstance() {
    return accountService;
  }

  private void updateFetchCache(String userid) {
    if(!userMap.containsKey(userid)) {
      Session session = sessionFactory.openSession();
      List<User> userList = session.createQuery("FROM User").list();
      for (User u : userList) {
        userMap.put(u.getMemberId(), u);
      }
      session.close();
    }
  }

  private boolean create(User u) {
    addUserTofriendList(u.getMemberId(),u.getMemberId());
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    session.save(u);
    session.getTransaction().commit();
    session.close();
    return true;

  }

  /***
   *
   * @param userId -> The name of the user.
   * @return An optional wrapper supplying the user.
   */

  @Override
  public Optional<User> findUserById(String userId) {
    updateFetchCache(userId);

    if (userMap.containsKey(userId)) {
      return Optional.of(userMap.get(userId));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public synchronized void addUser(User user) {
    if (userMap.containsKey(user.getMemberId())) {
      throw new UserAlreadyPresentException(
          String.format("User already present with name: %s", user.getName()));
    }
    create(user);
    userMap.put(user.getMemberId(), user);
  }

  /**
   * Fetches the latest login made by the user.
   */
  @Override
  public Timestamp fetchLatestLogin(String userId) {
    Optional<User> user = findUserById(userId);
    if (user.isPresent()) {
      List<Device> devices = user.get().getUserAccountSetting().getDevices();
      Timestamp latest = devices.get(0).getLastLogin();
      for (Device device : devices) {
        if (latest.before(device.getLastLogin())) {
          latest = device.getLastLogin();
        }
      }
      return latest;
    }
    return null;
  }
  /**
   * Validates if Moderators are valid Users.
   * @param moderatorIds comma separated string.
   * @return true false based of Validation.
   */
  @Override
  public boolean isModerators(String moderatorIds) {
    String[] moderatorIdArray = moderatorIds.split(",");
    boolean isValid = true;
    for(String moderatorId: moderatorIdArray){
      Optional<User> probableModerator = findUserById(moderatorId);
      if(!probableModerator.isPresent()){
        isValid =false;
        break;
      }
    }
    return isValid;
  }

  @Override
  public boolean addUserTofriendList(String userId1, String userId2) {
    Optional<User> user1 = findUserById(userId1);
    Optional<User> user2 = findUserById(userId2);
    if(user1.isPresent() && user2.isPresent()){
      user1.get().getMembers().add(user2.get());
      user2.get().getMembers().add(user1.get());
      Session session = sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(user1.get());
      session.saveOrUpdate(user2.get());
      session.getTransaction().commit();
      session.close();
      return true;
    }
    return false;
  }

}
