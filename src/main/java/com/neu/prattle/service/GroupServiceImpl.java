package com.neu.prattle.service;

import com.neu.prattle.exceptions.AlreadyExistingModeratorException;
import com.neu.prattle.exceptions.GroupAlreadyPresentException;
import com.neu.prattle.exceptions.GroupDoesNotExistException;
import com.neu.prattle.exceptions.MemberDoesNotExistInGroupException;
import com.neu.prattle.exceptions.NotExistingMember;
import com.neu.prattle.hibernate.HibernateUtil;
import com.neu.prattle.model.Group;
import com.neu.prattle.model.Member;
import com.neu.prattle.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.NotAuthorizedException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class GroupServiceImpl implements GroupService {

    private static GroupService uniqueGroupServiceImplInstance;

    private SessionFactory sessionFactory;
    private Map<String, Group> groupMap;

    private GroupServiceImpl() {
        sessionFactory = HibernateUtil.getSessionFactory();
        groupMap = new HashMap<>();
    }

    public static GroupService getInstance() {
        if (uniqueGroupServiceImplInstance == null) {
            uniqueGroupServiceImplInstance = new GroupServiceImpl();
        }
        return uniqueGroupServiceImplInstance;
    }

    private void updateFetchCache(String memerid) {
        if (!groupMap.containsKey(memerid)) {
            Session session = sessionFactory.openSession();
            groupMap.clear();
            List<Group> groupList = session.createQuery("FROM Group").list();
            for (Group grp : groupList) {
                groupMap.put(grp.getMemberId(), grp);
            }
            session.close();
        }
    }

    @Override
    public List<String> getGroupsForUser(String memberId) {
        updateFetchCache(memberId);
        User u = User.builder().build();
        u.setMemberId(memberId);
        return groupMap.values().stream().filter(e -> e.getMembers().contains(u))
                .map(Group::getMemberId).collect(Collectors.toList());
    }

    private boolean createGroup(Group group) {
        boolean isSuccessful = true;
        Session sessionObj = sessionFactory.openSession();
        sessionObj.beginTransaction();
        sessionObj.save(group);
        sessionObj.getTransaction().commit();
        sessionObj.close();
        return isSuccessful;

    }


    @Override
    public Optional<Group> findGroupById(String groupId) {
        updateFetchCache(groupId);
        if (groupMap.containsKey(groupId)) {
            return Optional.of(groupMap.get(groupId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean addMemberToGroupDB(String groupId, Member member) {
        Optional<Group> current = findGroupById(groupId);
        Set<Member> currentMembers = new HashSet<>();
        if (current.isPresent()) {
            if (current.get().getMembers() != null) {
                currentMembers.addAll(current.get().getMembers());
            }
            currentMembers.add(member);
            current.get().setMembers(currentMembers);
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(current.get());
            session.getTransaction().commit();
            session.close();
            return true;
        }
        return false;
    }

    @Override
    public void addGroup(Group group) {
        if (groupMap.containsKey(group.getMemberId())) {
            throw new GroupAlreadyPresentException(
                    String.format("Group already present with id: %s", group.getMemberId()));
        }
        createGroup(group);
        groupMap.put(group.getMemberId(), group);
    }

    @Override
    public Set<Member> getMembers(String groupId) {
        if (groupMap.containsKey(groupId)) {
            return groupMap.get(groupId).getMembers();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public boolean deleteGroup(String groupId) {
        Optional<Group> groupToDelete = findGroupById(groupId);
        if (groupToDelete.isPresent()) {
            Group groupObj = groupToDelete.get();
            Session sessionObj = sessionFactory.openSession();
            sessionObj.beginTransaction();
            sessionObj.delete(groupObj);
            sessionObj.getTransaction().commit();
            sessionObj.close();
            return true;
        } else {
            throw new GroupDoesNotExistException("The group with groupId does not exist: " + groupId);
        }
    }

    @Override
    public void removeMemberFromGroup(String memberToRemoveId, String groupId) {
        Optional<Group> optionalGroup = findGroupById(groupId);
        if (optionalGroup.isPresent()) {
            Group groupObj = optionalGroup.get();
            Session sessionObj = sessionFactory.openSession();
            sessionObj.beginTransaction();
            Optional<Member> memberRemove = groupObj.getMembers().stream()
                    .filter(member -> member.getMemberId().contentEquals(memberToRemoveId)).findAny();
            if (memberRemove.isPresent()) {
                groupObj.getMembers().remove(memberRemove.get());
            } else {
                throw new MemberDoesNotExistInGroupException(
                        "Member : " + memberToRemoveId + " doesn't exist in : " + groupId);
            }
            sessionObj.saveOrUpdate(groupObj);
            sessionObj.getTransaction().commit();
            sessionObj.close();
        } else {
            throw new GroupDoesNotExistException("Group doesn't exist :" + groupId);
        }
    }

    @Override
    public boolean addModeratorToGroup(String newModeratorId, String groupId, String initiator) {
        Optional<Group> groupToUpdate = findGroupById(groupId);
        if (groupToUpdate.isPresent()) {
            Group groupObj = groupToUpdate.get();
            Set<String> moderatorIds = new HashSet<>(
                    Arrays.asList(groupObj.getModeratorIds().split(",")));
            if (moderatorIds.contains(initiator)) {
                Optional<Member> newModerator = groupObj.getMembers().stream()
                        .filter(member -> member.getMemberId().contentEquals(newModeratorId)).findAny();
                if (newModerator.isPresent()) {
                    if (!moderatorIds.contains(newModeratorId)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(groupObj.getModeratorIds()).append(",").append(newModeratorId);
                        groupObj.setModeratorIds(sb.toString());
                    } else {
                        throw new AlreadyExistingModeratorException(
                                "ModeratorId : " + newModeratorId + " already exists in the group : " + groupId);
                    }
                } else {
                    throw new NotExistingMember("Member : " + newModeratorId);
                }
            } else {
                throw new NotAuthorizedException(
                        "Initiator : " + initiator + "is not the moderator of the group : " + groupId);
            }
            Session sessionObj = sessionFactory.openSession();
            sessionObj.beginTransaction();
            sessionObj.saveOrUpdate(groupObj);
            sessionObj.getTransaction().commit();
            sessionObj.close();
            return true;
        } else {
            throw new GroupDoesNotExistException("The group with groupId does not exist: " + groupId);
        }
    }

    @Override
    public void transferModeratorResponsibility(String newModeratorId, String groupId,
                                                String initiator) {
        addModeratorToGroup(newModeratorId, groupId, initiator);
        removeModerator(groupId, initiator);
    }

    private void removeModerator(String groupId, String initiator) {
        Optional<Group> optionalGroup = findGroupById(groupId);
        if (optionalGroup.isPresent()) {
            Group groupObj = optionalGroup.get();
            Set<String> moderatorIds = new HashSet<>(
                    Arrays.asList(groupObj.getModeratorIds().split(",")));
            moderatorIds.remove(initiator);
            String sb = String.join(",", moderatorIds);
            groupObj.setModeratorIds(sb);
            Session sessionObj = sessionFactory.openSession();
            sessionObj.beginTransaction();
            sessionObj.saveOrUpdate(groupObj);
            sessionObj.getTransaction().commit();
            sessionObj.close();
        }
    }

    @Override
    public List<String> getGroupIdsWhereUserIsModerator(String memberId) {
        updateFetchCache(memberId);
        List<String> groupIdsWhereUserIsModerator = new ArrayList<>();
        for (Group group : groupMap.values()) {
            if (group.getModeratorIds() != null && group.getModeratorIds().contains(memberId)) {
                groupIdsWhereUserIsModerator.add(group.getMemberId());
            }
        }
        return groupIdsWhereUserIsModerator;
    }
}
