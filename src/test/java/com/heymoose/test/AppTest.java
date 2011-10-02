package com.heymoose.test;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class AppTest extends RestTest {

  String EMAIL = "test@heymoose.com";
  String NICKNAME = "anon";
  String PASSWORD_HASH = "3gewn4iougho";

  long create() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.DEVELOPER);
    heymoose().createApp(userId);
    XmlUser user = heymoose().getUser(userId);
    return user.app.id;
  }

  @Test public void createApp() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.DEVELOPER);
    heymoose().createApp(userId);
    XmlUser user = heymoose().getUser(userId);
    assertNotNull(user.app.id);
    assertNotNull(user.app.secret);
  }

  @Test public void createAppWithoutDeveloperRole() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    try {
      heymoose().createApp(userId);
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }

  @Test public void getApp() {
    long appId = create();
    XmlApp app = heymoose().getApp(appId);
    assertNotNull(app.id);
    assertFalse(isBlank(app.secret));
  }

  @Test public void regenerateSecret() {
    long appId = create();
    XmlApp app = heymoose().getApp(appId);
    String oldSecret = app.secret;
    heymoose().regenerateSecret(appId);
    app = heymoose().getApp(appId);
    assertFalse(isBlank(app.secret));
    assertFalse(oldSecret.equals(app.secret));
  }

  @Test public void deleteApp() {
    long appId = create();
    heymoose().deleteApp(appId);
    try {
      heymoose().getApp(appId);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }
}
