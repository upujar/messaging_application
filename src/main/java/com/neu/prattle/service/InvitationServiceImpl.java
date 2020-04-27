package com.neu.prattle.service;

import com.neu.prattle.hibernate.HibernateUtil;
import com.neu.prattle.model.Invite;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class InvitationServiceImpl implements InvitationService {

  private static InvitationService invitationService;

  private SessionFactory sessionFactory;


  /***
   * UserServiceImpl is a Singleton class.
   */
  private InvitationServiceImpl() {
    sessionFactory = HibernateUtil.getSessionFactory();
  }

  /**
   * Call this method to return an instance of this service.
   *
   * @return this
   */
  public static InvitationService getInstance() {
    if (invitationService == null) {
      invitationService = new InvitationServiceImpl();
    }
    return invitationService;
  }


  @Override
  public void persistInvitation(Invite invite) {
    invite.setInviteId(invite.getFromInvite() + invite.getToInvite() + invite.getGroupId());
    Session sessionObj = sessionFactory.openSession();
    sessionObj.beginTransaction();
    sessionObj.saveOrUpdate(invite);
    sessionObj.getTransaction().commit();
    sessionObj.close();
  }

  @Override
  public List<Invite> fetchUserInvitationForMember(String memberId) {
    Session session = sessionFactory.openSession();
    List<Invite> invitationsList = session
        .createQuery("FROM Invite Where to_invite = \'" + memberId + "\' and is_accepted = 0 and group_id = \'\'")
        .list();
    session.close();
    return invitationsList;
  }

  @Override
  public List<Invite> fetchGroupInvitationForMember(String groupId) {

    Session session = sessionFactory.openSession();
    List<Invite> invitationsList = session
        .createQuery("FROM Invite Where group_id = \'" + groupId + "\' and is_accepted = 0")
        .list();
    session.close();
    return invitationsList;
  }

}
