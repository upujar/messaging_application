package com.neu.prattle.model;

import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class InviteTest {


  Invite invite;

  @Before
  public void setup() {
    invite = Invite.builder().inviteId("u1u2g1").fromInvite("u1").toInvite("u2").groupId("g1").build();

  }

  @Test
  public void testEquals(){
    assertTrue(invite.equals(Invite.builder().inviteId("u1u2g1").build()));
    assertTrue(invite.equals(invite));
    assertFalse(invite.equals(null));
    assertFalse(invite.equals(User.builder().build()));

  }

  @Test
  public void testDefaultConst(){
    Invite invite1 = new Invite();
    invite1.setInviteId("t1");
    assertTrue(invite1.getInviteId().equals("t1"));
  }

  @Test
  public void testHashCode(){
    Invite invite1 = new Invite();
    invite1.setInviteId("u1u2g1");
    assertTrue(invite1.hashCode() == invite.hashCode());
  }
}
