package com.heymoose.domain;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Set;
import java.util.UUID;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Entity
@Table(name = "user_profile", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-seq")
  @SequenceGenerator(name = "user-seq", sequenceName = "user_profile_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<Order> orders;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<App> apps;

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  @Enumerated
  private Set<Role> roles;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "customer_account_id")
  private Account customerAccount;

  @Column(name = "customer_secret", nullable = true)
  private String customerSecret;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "developer_account_id")
  private Account developerAccount;

  @Basic(optional = false)
  private String email;

  @Basic(optional = false)
  private String passwordHash;

  @Basic(optional = false)
  private String nickname;

  protected User() {}

  public User(String email, String nickname, String passwordHash) {
    checkNotNull(email, nickname, passwordHash);
    this.email = email;
    this.nickname = nickname;
    this.passwordHash = passwordHash;
  }

  public Account developerAccount() {
    return developerAccount;
  }

  public Account customerAccount() {
    return customerAccount;
  }

  public String customerSecret() {
    return customerSecret;
  }

  public String email() {
    return email;
  }

  public Set<App> apps() {
    if (apps == null)
      return emptySet();
    return unmodifiableSet(apps);
  }

  public void addRole(Role role) {
    if (roles == null)
      roles = Sets.newHashSet();
    roles.add(role);
    if (role.equals(Role.CUSTOMER) && customerAccount == null) {
      customerAccount = new Account();
      customerSecret = UUID.randomUUID().toString();
    }
    if (role.equals(Role.DEVELOPER) && developerAccount == null)
      developerAccount = new Account();
  }

  public Set<Role> roles() {
    if (roles == null)
      return emptySet();
    return unmodifiableSet(roles);
  }

  public Set<Order> orders() {
    if (orders == null)
      return emptySet();
    return unmodifiableSet(orders);
  }

  public boolean isCustomer() {
    return roles != null && roles.contains(Role.CUSTOMER);
  }

  public boolean isDeveloper() {
    return roles != null && roles.contains(Role.DEVELOPER);
  }

  public String nickname() {
    return nickname;
  }

  public String passwordHash() {
    return passwordHash;
  }
}
