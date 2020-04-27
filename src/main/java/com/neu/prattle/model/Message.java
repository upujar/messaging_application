package com.neu.prattle.model;

import java.sql.Timestamp;
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
 * A Basic POJO for Message.
 *
 * @author CS5500 Fall 2019 Teaching staff
 * @version dated 2019-10-06
 */
@Entity
@Table(name = "message")
@Builder
@Getter
@Setter
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id", unique = true, nullable = false)
  private Integer messageId;

  /***
   * The name of the user who sent this message.
   */
  @Column(name = "from_user", nullable = false)
  private String from;

  /***
   * The name of the user to whom the message is sent.
   */
  @Column(name = "to_user", nullable = false)
  private String to;

  /***
   * It represents the contents of the message.
   */
  @Column(name = "content", nullable = false)
  private String content;

  /**
   * Timestamp for the message sent.
   */
  @Column(name = "timestamp")
  private Timestamp timestamp;

  /***
   * boolean flag if the message is read.
   */
  @Column(name = "is_read", nullable = false)
  private boolean isRead;


  @Tolerate
  public Message() {
    /**
     * To enable use of Builder.
     */
  }


  @Override
  public String toString() {
    return new StringBuilder()
        .append("From: ").append(from)
        .append("To: ").append(to)
        .append("Content: ").append(content)
        .append("isRead: ").append(isRead)
        .toString();
  }

}

