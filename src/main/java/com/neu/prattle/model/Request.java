package com.neu.prattle.model;

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

/***
 * A Basic POJO for Request.
 */
@Entity
@Table(name = "request")
@Builder
@Getter
@Setter
public class Request {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "request_id", unique = true, nullable = false)
  private Integer requestId;

  @Column(name = "initiator")
  private String initiator;

  @Column(name = "regarding")
  private String regarding;

  @Column(name = "approver")
  private String approver;

  @Column(name = "place")
  private String place;

  @Column(name = "is_approved")
  private boolean isApproved;

  @Column(name = "is_rejected")
  private boolean isRejected;

  @Column(name = "type")
  private char type;

  @Tolerate
  public Request(){
    /**
     * Empty Constructor.
     */
  }

}
