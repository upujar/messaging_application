package com.neu.prattle.model;

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;


@Entity
@Table(name = "profile")
@Builder
@Getter
@Setter
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "profile_id", updatable = false, nullable = false)
  private long profileId;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "display_picture")
  private Blob displayPicture;

  @Column(name = "is_online")
  private boolean isOnline;

  @Column(name = "is_dnd_account")
  private boolean isDNDAccount;

  @Tolerate
  public Profile() {
    /**
     * To enable use of Builder.
     */
  }
}
