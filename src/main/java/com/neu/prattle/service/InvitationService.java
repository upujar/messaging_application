package com.neu.prattle.service;

import com.neu.prattle.model.Invite;
import java.util.List;

public interface InvitationService {

  void persistInvitation(Invite invitation);

  List<Invite> fetchUserInvitationForMember(String memberId);

  List<Invite> fetchGroupInvitationForMember(String groupId);
}
