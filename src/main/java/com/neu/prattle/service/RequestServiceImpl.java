package com.neu.prattle.service;

import com.neu.prattle.hibernate.HibernateUtil;
import com.neu.prattle.model.Request;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;

public class RequestServiceImpl implements RequestService {

  private static RequestService uniqueRequestServiceImplInstance;

  private SessionFactory sessionFactory;

  private RequestServiceImpl(){
    sessionFactory = HibernateUtil.getSessionFactory();
  }

  public static RequestService getInstance(){
    if(uniqueRequestServiceImplInstance == null){
      uniqueRequestServiceImplInstance = new RequestServiceImpl();
    }
    return uniqueRequestServiceImplInstance;
  }

  @Override
  public void createRequest(Request request) {
    Session sessionObj = sessionFactory.openSession();
    sessionObj.beginTransaction();
    sessionObj.save(request);
    sessionObj.getTransaction().commit();
    sessionObj.close();

  }

  @Override
  public void approveRequest(Request request) {
    Session sessionObj = sessionFactory.openSession();
    sessionObj.beginTransaction();
    request.setApproved(true);
    sessionObj.saveOrUpdate(request);
    sessionObj.getTransaction().commit();
    sessionObj.close();
  }

  @Override
  public void rejectRequest(Request request) {
    Session sessionObj = sessionFactory.openSession();
    sessionObj.beginTransaction();
    request.setRejected(true);
    sessionObj.saveOrUpdate(request);
    sessionObj.getTransaction().commit();
    sessionObj.close();
  }

  @Override
  public List<Request> fetchAllActiveEvictRequestsForUser(String memberId) {
    Session session = sessionFactory.openSession();
    List<Request> requestList = session.createQuery("FROM Request where is_rejected<>1 and is_approved<>1 and approver="+memberId).list();
    session.close();
    return requestList;
  }

}
