package com.neu.prattle.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_of_member", length = 1)
@Table(name = "member")
@Getter
@Setter
public abstract class Member {

  @Id
  @Column(name = "member_id")
  private String memberId;


  @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
  private List<Group> parents;


  protected Member() {
    parents = new ArrayList<>();
  }

  abstract boolean isLeaf();
}
