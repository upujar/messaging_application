package com.neu.prattle.model;

import java.util.HashSet;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SecondaryTable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Entity
@DiscriminatorValue("G")
@SecondaryTable(name = "group_detail")
@Builder
@Getter
@Setter
public class Group extends Member {

  @Column(name = "moderator_ids", table = "group_detail")
  private String moderatorIds;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "group_member_mapping", schema = "prattle",
          joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "member_id"),
          inverseJoinColumns = @JoinColumn(name = "member_id", referencedColumnName = "member_id"))
  private Set<Member> members= new HashSet<>();

  @Tolerate
  public Group() {
    super();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
