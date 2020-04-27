package com.neu.prattle.viewmodel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Builder
@Getter
@Setter
public class GroupMember {
  private String memberId;
  private String groupId;

  @Tolerate
  public GroupMember(){
    /**
     * Empty Constructor.
     */
  }

}
