package com.neu.prattle.service;

import com.neu.prattle.model.Request;

import java.util.List;

public interface RequestService {
  void createRequest(Request request);

  void approveRequest(Request request);

  void rejectRequest(Request request);

  List<Request> fetchAllActiveEvictRequestsForUser(String memberId);
}
