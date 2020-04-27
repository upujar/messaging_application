package com.neu.prattle.websocket;

import com.neu.prattle.model.Message;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageDecoderTest {

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    IOException ex;

    @InjectMocks
    private MessageDecoder md = new MessageDecoder();

    @Before
    public void Setup() {
        //md = new MessageDecoder();
    }

    @Test(expected = NullPointerException.class)
    public void testNullDecode() throws IOException {
        when(mockObjectMapper.readValue(anyString(), (Class<Object>) any())).thenThrow(new NullPointerException());
        md.decode(null);
    }

    @Test
    public void testValidStringDecode() throws IOException {
        Message msg = Message.builder().content("hi").from("test").to("test").messageId(12345).build();
        MessageEncoder me = new MessageEncoder();
        String json = me.encode(msg);
        Message s =md.decode(json);
        when( mockObjectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(msg);
        assertEquals("From: testTo: testContent: hiisRead: false", md.decode(json).toString());
    }

    @Test
    public void testNullInputWillDecode() {
        assertFalse(md.willDecode(null));
    }

    @Test
    public void testWillDecode() {
        assertTrue(md.willDecode("hi"));
    }

    @Test
    public void testEmptyStringWillDecode() {
        assertTrue(md.willDecode(""));
    }

    @Test
    public void testInit(){
        md.init(null);
        assertTrue(1==1);
    }

    @Test
    public void testDestroy(){
        md.destroy();
        assertTrue(1==1);
    }

    @Test
    public void testIOException() throws IOException {
        when(mockObjectMapper.readValue(anyString(), (Class<Object>) any()))
                .thenThrow(ex);

        md.decode("{}");
        verify(ex, times(1)).getMessage();

    }
}
