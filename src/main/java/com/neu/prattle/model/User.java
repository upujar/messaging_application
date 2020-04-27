package com.neu.prattle.model;

import java.util.HashSet;
import java.util.Objects;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

/***
 * A User object represents a basic account information for a user.
 *
 * @author CS5500 Fall 2019 Teaching staff
 * @version dated 2019-10-06
 */
@Entity
@DiscriminatorValue("U")
@SecondaryTable(name = "user")
@Builder
@Getter
@Setter
public class User extends Member {

  @Column(name="password",table = "user")
  private String password;

  @Column(name = "name", table = "user")
  private String name;

  @OneToOne(targetEntity = Profile.class, cascade = {CascadeType.ALL})
  @JoinColumn(name = "profile_id_fk", referencedColumnName = "profile_id", table = "user")
  private Profile profile;

  @OneToOne(targetEntity = UserAccountSetting.class, cascade = {CascadeType.ALL})
  @JoinColumn(name = "user_account_setting_id_fk", referencedColumnName = "user_account_setting_id")
  private UserAccountSetting userAccountSetting;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_mapping", schema = "prattle",
      joinColumns = @JoinColumn(name = "user_id1", referencedColumnName = "member_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id2", referencedColumnName = "member_id"))
  private Set<User> members= new HashSet<>();

  @Tolerate
  public User() {
    super();
  }


  /***
   * Returns the hashCode of this object.
   *
   * As name can be treated as a sort of identifier for
   * this instance, we can use the hashCode of "name"
   * for the complete object.
   *
   *
   * @return hashCode of "this"
   */
  @Override
  public int hashCode() {
    return Objects.hash(getMemberId());
  }

  /***
   * Makes comparison between two user accounts.
   *
   * Two user objects are equal if their name are equal ( names are case-sensitive )
   *
   * @param obj Object to compare
   * @return a predicate value for the comparison.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User)) {
      return false;
    }

    User user = (User) obj;
    return user.getMemberId().equals(this.getMemberId());
  }

  @Override
  public boolean isLeaf() {
    return true;
  }
}
