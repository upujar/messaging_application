package com.neu.prattle.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Entity
@Table(name = "invite")
@Builder
@Getter
@Setter
public class Invite {

  @Id
  @Column(name = "invite_id", unique = true, nullable = false)
  private String inviteId;

  @Column(name = "from_invite", nullable = false)
  private String fromInvite;

  @Column(name = "to_invite", nullable = false)
  private String toInvite;

  @Column(name = "is_accepted")
  private int isAccepted;

  @Column(name = "group_id")
  private String groupId;

  @Column(name = "approver_id")
  private String approverId;


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Invite invite = (Invite) o;
    return Objects.equals(inviteId, invite.inviteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inviteId);
  }

  @Tolerate
  public Invite(){
    /**
     * Empty Constructor.
     */
  }

  @Override
  public String toString() {
    return "Invite{" +
        "inviteId='" + inviteId + '\'' +
        ", fromInvite='" + fromInvite + '\'' +
        ", toInvite='" + toInvite + '\'' +
        ", isAccepted=" + isAccepted +
        ", groupId='" + groupId + '\'' +
        ", approverId='" + approverId + '\'' +
        '}';
  }
}
