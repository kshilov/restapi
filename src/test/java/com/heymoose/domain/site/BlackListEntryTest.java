package com.heymoose.domain.site;

import org.junit.Test;

import java.net.URL;

import static junit.framework.Assert.*;

public final class BlackListEntryTest {

  @Test
  public void hostMatching() throws Exception {
    BlackListEntry entry = new BlackListEntry().setHost("heymoose.com");
    assertTrue(entry.matches("heymoose.com"));

    assertTrue(entry.matches("http://heymoose.com"));
    assertTrue(entry.matches("http://heymoose.com/"));

    assertTrue(entry.matches("https://heymoose.com"));
    assertTrue(entry.matches("https://heymoose.com/"));

    assertTrue(entry.matches("http://www.heymoose.com"));
    assertTrue(entry.matches("www.heymoose.com"));

    assertTrue(entry.matches("test.heymoose.com"));
    assertTrue(entry.matches("test.heymoose.com/test"));
    assertTrue(entry.matches("test.heymoose.com/test/test"));

    assertFalse(entry.matches("example.com"));
    assertFalse(entry.matches("http://example.com"));
    assertFalse(entry.matches("http://example.com/"));
    assertFalse(entry.matches("http://www.example.com/"));
  }


  @Test
  public void subDomainMatching() throws Exception {
    BlackListEntry entry = new BlackListEntry()
        .setHost("heymoose.com")
        .setSubDomainMask("sub");

    assertTrue(entry.matches("sub.heymoose.com"));

    assertFalse(entry.matches("heymoose.com"));
    assertFalse(entry.matches("other.heymoose.com"));
    assertFalse(entry.matches("other.sub.heymoose.com"));
    assertFalse(entry.matches("http://other.heymoose.com"));
  }

  @Test
  public void pathMatching() throws Exception {
    BlackListEntry entry = new BlackListEntry()
        .setHost("heymoose.com")
        .setPathMask("resource/?");

    assertTrue(entry.matches("heymoose.com/resource"));
    assertTrue(entry.matches("http://heymoose.com/resource/"));

    assertFalse(entry.matches("http://heymoose.com"));
    assertFalse(entry.matches("heymoose.com/resource/resource"));
    assertFalse(entry.matches("heymoose.com/resource/resource"));
    assertFalse(entry.matches("heymoose.com/something/resource"));
  }

  @Test
  public void queryParams() throws Exception {
    String url = "http://black-host.com/bla-bla/bla-bla?xxx=yyy";

    assertEquals("black-host.com", BlackListEntry.extractHost(url));
  }

  @Test
  public void urlHostTest() throws Exception {
    URL url = new URL("http://test.heymoose.com/blabla/haha/?xxx=yyy");
    assertEquals("test.heymoose.com", url.getHost());
    assertEquals("/blabla/haha/", url.getPath());
  }
}
