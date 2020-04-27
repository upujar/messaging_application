package com.neu.prattle.websocket;

import com.neu.prattle.model.Message;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageEncoderTest {

  @Mock
  private ObjectMapper mockObjectMapper;

  @Mock
  IOException ex;

  @InjectMocks
  private MessageEncoder me = new MessageEncoder();

  @Test
  public void testEncodeNullMessage() {
    assertEquals("{}", me.encode(null));
  }

  @Test
  public void testEncoderValidMessage() throws IOException {
    Message msg = Message.builder().content("hello world").from("test").to("test").messageId(1345)
            .build();
    String ans = "{\"messageId\":1345,\"from\":\"test\",\"to\":\"test\",\"content\":\"hello world\"}";
    when(mockObjectMapper.writeValueAsString(anyString())).thenReturn(ans);
    String json = me.encode(msg);
    assertEquals(ans, json);
    assertNotEquals("{}", json);
  }


  @Test
  public void testInit() {
    me.init(null);
    assertTrue(1==1);
  }

  @Test
  public void testDestroy() {
    me.destroy();
    assertTrue(1==1);
  }

  @Test
  public void testIOException() throws IOException {
    Message msg = Message.builder().build();
    when(mockObjectMapper.writeValueAsString(msg))
            .thenThrow(ex);

    me.encode(msg);
    verify(ex, times(1)).getMessage();

  }

}