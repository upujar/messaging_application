package com.neu.prattle.service;

import com.neu.prattle.model.Group;
import com.neu.prattle.model.Member;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupService {

  Optional<Group> findGroupById(String groupId);

  boolean addMemberToGroupDB(String groupId, Member member);

  void addGroup(Group group);

  Set<Member> getMembers(String groupId);

  boolean deleteGroup(String groupId);

  void removeMemberFromGroup(String memberToRemoveId, String groupId);

  boolean addModeratorToGroup(String newModeratorId, String groupId, String initiator);

  void transferModeratorResponsibility(String newModeratorId, String groupId, String initiator);

  List<String> getGroupsForUser(String memberId);

  List<String> getGroupIdsWhereUserIsModerator(String memberId);
}
