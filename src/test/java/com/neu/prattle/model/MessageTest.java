package com.neu.prattle.model;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MessageTest {


  @Test
  public void testTextMessageCreation() {
    Message message = Message.builder().build();
    message.setMessageId(1);
    message.setFrom("Me");
    message.setTo("You");
    message.setContent("Hi");
    assertTrue(message.getMessageId() == 1);
    assertTrue(message.getContent().contentEquals("Hi"));
    assertTrue(message.getFrom().contentEquals("Me"));
    assertTrue(message.getTo().contentEquals("You"));
    assertTrue(message.toString().contentEquals("From: MeTo: YouContent: HiisRead: false"));
  }

  @Test
  public void testBuilderMessage() {
    Message.MessageBuilder builder = Message.builder();
    builder.messageId(1);
    builder.content("Yo");
    builder.from("Me");
    builder.to("You");
    Message message = builder.build();
    assertTrue(message.getMessageId() == 1);
    assertTrue(message.getTo().contentEquals("You"));
    assertTrue(message.getFrom().contentEquals("Me"));
    assertTrue(message.getContent().contentEquals("Yo"));
  }
}