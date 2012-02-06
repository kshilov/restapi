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
  String PASSWORD_HASH = "3gewn4iougho";
  String FIRST_NAME = "Ivan";
  String LAST_NAME = "Ivanov";
  String PHONE = "+7 (915) 123-45-67";

  void baseValidateUser(XmlUser user) {
    assertEquals(EMAIL, user.email);
    assertEquals(PASSWORD_HASH, user.passwordHash);
    assertEquals(FIRST_NAME, user.firstName);
    assertEquals(LAST_NAME, user.lastName);
    assertEquals(PHONE, user.phone);
  }

  @Test public void register() {
    long userId = heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
    XmlUser user = heymoose().getUser(userId);
    baseValidateUser(user);
  }

  @Test public void changePassword() {
    long userId = heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
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
    heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
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
    long userId = heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
    XmlUser user = heymoose().getUser(userId);
    assertEquals(0, user.roles.size());
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    user = heymoose().getUser(userId);
    assertEquals(1, user.roles.size());
    assertEquals(Role.CUSTOMER, Role.valueOf(user.roles.iterator().next()));
  }

  @Test public void addToCustomerAccount() {
    long userId = heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    double amount = 1.2;
    heymoose().addToCustomerAccount(userId, amount);
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(amount), user.customerAccount.balance);
  }

  @Test public void addToCustomerAccountWithoutCustomerRole() {
    long userId = heymoose().registerUser(EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE);
    double amount = 1.2;
    try {
      heymoose().addToCustomerAccount(userId, amount);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }
}
