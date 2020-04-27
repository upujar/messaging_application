package com.neu.prattle.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Entity
@Table(name = "device")
@Builder
@Getter
@Setter
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "device_id", updatable = false, nullable = false)
  private long deviceId;

  @Column(name = "device_type", nullable = false)
  private String deviceType;

  @Column(name = "last_login")
  private Timestamp lastLogin;

  @ManyToOne(fetch = FetchType.EAGER)
  private UserAccountSetting userAccountSetting;

  @Tolerate
  public Device(){
    /**
     * Empty Constructor.
     */
  }

}
