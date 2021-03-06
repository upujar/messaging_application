package com.neu.prattle.service;

import com.neu.prattle.model.User;

import java.sql.Timestamp;
import java.util.Optional;

/***
 * Acts as an interface between the data layer and the
 * servlet controller.
 *
 * The controller is responsible for interfacing with this instance
 * to perform all the CRUD operations on user accounts.
 *
 * @author CS5500 Fall 2019 Teaching staff
 * @version dated 2019-10-06
 *
 */
public interface UserService {
  /***
   * Returns an optional object which might be empty or wraps an object
   * if the System contains a {@link User} object having the same name
   * as the parameter.
   *
   * @param userId The name of the user
   * @return Optional object.
   */
  Optional<User> findUserById(String userId);


  /***
   * Tries to add a user in the system
   * @param user User object
   *
   */
  void addUser(User user);

  /**
   * Fetches the lastest login made by the user.
   */
  Timestamp fetchLatestLogin(String userId);

  boolean isModerators(String moderatorIds);

  boolean addUserTofriendList(String userId1, String userId2);
}
