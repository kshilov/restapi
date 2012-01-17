package com.heymoose.test;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserTest extends RestTest {

  String EMAIL = "test@heymoose.com";
  String NICKNAME = "anon";
  String PASSWORD_HASH = "3gewn4iougho";

  void baseValidateUser(XmlUser user) {
    assertEquals(EMAIL, user.email);
    assertEquals(NICKNAME, user.nickname);
    assertEquals(PASSWORD_HASH, user.passwordHash);
  }

  @Test public void register() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    XmlUser user = heymoose().getUser(userId);
    baseValidateUser(user);
  }

  @Test public void changePassword() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    XmlUser user = heymoose().getUser(userId);
    assertEquals(PASSWORD_HASH, user.passwordHash);
    String newHash = "sfnslflwe";
    heymoose().updateUser(userId, newHash);
    user = heymoose().getUser(userId);
    assertEquals(newHash, user.passwordHash);
  }

  @Test public void getNonExistent() {
    try {
      heymoose().getUser(1L);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }

  @Test public void getByEmail() {
    heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    XmlUser user = heymoose().getUserByEmail(EMAIL);
    baseValidateUser(user);
  }

  @Test public void getNonExistentByEmail() {
    try {
      heymoose().getUserByEmail(EMAIL);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }

  @Test public void addRole() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    XmlUser user = heymoose().getUser(userId);
    assertEquals(0, user.roles.size());
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    user = heymoose().getUser(userId);
    assertEquals(1, user.roles.size());
    assertEquals(Role.CUSTOMER, Role.valueOf(user.roles.iterator().next()));
  }

  @Test public void addToCustomerAccount() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    double amount = 1.2;
    heymoose().addToCustomerAccount(userId, amount);
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(amount), user.customerAccount.balance);
  }

  @Test public void addToCustomerAccountWithoutCustomerRole() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    double amount = 1.2;
    try {
      heymoose().addToCustomerAccount(userId, amount);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }
}
