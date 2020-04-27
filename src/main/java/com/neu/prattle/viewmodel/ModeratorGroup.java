package com.neu.prattle.viewmodel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Builder
@Getter
@Setter
public class ModeratorGroup {

  private String moderatorId;
  private String groupId;
  private String initiatorId;

  @Tolerate
  public ModeratorGroup(){
    /**
     * Empty Constructor.
     */
  }
}
