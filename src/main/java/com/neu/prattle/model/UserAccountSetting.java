package com.neu.prattle.model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Entity
@Table(name = "user_account_setting")
@Builder
@Getter
@Setter
public class UserAccountSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_account_setting_id", unique = true, nullable = false)
  private long userAccountSettingId;

  @Column(name = "is_private_account")
  private boolean isPrivateAccount;

  @OneToMany(targetEntity = Device.class, cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_account")
  private List<Device> devices;

  @Tolerate
  public UserAccountSetting() {
    /**
     * Empty Constructor.
     */
  }

}
