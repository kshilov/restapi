package com.heymoose.domain.site;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public final class SiteTest {

  @Test
  public void webSiteRefererMatch() throws Exception {
    Site site = new Site()
        .setType(Site.Type.WEB_SITE)
        .addAttribute("url", "http://host.com");

    assertTrue(site.matches("http://host.com"));
    assertTrue(site.matches("http://host.com/"));
    assertTrue(site.matches("http://www.host.com"));
    assertTrue(site.matches("http://host.com/bla/bla/bla"));
    assertTrue(site.matches("http://www.host.com/bla/bla/bla"));
    assertTrue(site.matches("host.com"));

    assertFalse(site.matches("http://other-host.com"));
    assertFalse(site.matches("http://sub.host.com"));
    assertFalse(site.matches("http://sub.host.com/host.com"));
    assertFalse(site.matches(null));
  }
}
