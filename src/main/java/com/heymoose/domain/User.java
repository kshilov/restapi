package com.heymoose.domain;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.net.URI;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import java.util.UUID;
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
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

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

  @Column(name = "first_name", nullable = false)
  private String firstName;
  
  @Column(name = "last_name", nullable = false)
  private String lastName;
  
  @Basic
  private String organization;
  
  @Basic
  private String phone;
  
  @Column(name = "source_url")
  private String sourceUrl;
  
  @Enumerated
  @Column(name = "messenger_type")
  private MessengerType messengerType;
  
  @Column(name = "messenger_uid")
  private String messengerUid;

  @Column(name = "referrer")
  private Long referrerId;
  
  @Basic(optional = false)
  private boolean confirmed;
  
  @Basic(optional = false)
  private boolean blocked;
  
  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "register_time", nullable = false)
  private DateTime registerTime;
  
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
  private UserStat stat;

  @Basic
  private int fee;

  protected User() {}

  public User(String email, String passwordHash, String firstName, String lastName, String organization,
              String phone, URI sourceUrl, MessengerType messengerType, String messengerUid,
              Long referrerId) {
    checkNotNull(email, passwordHash, firstName, lastName);
    this.email = email;
    this.passwordHash = passwordHash;
    this.firstName = firstName;
    this.lastName = lastName;
    this.organization = organization;
    this.phone = phone;
    this.sourceUrl = sourceUrl != null ? sourceUrl.toString() : null;
    this.messengerType = messengerType;
    this.messengerUid = messengerUid;
    this.referrerId = referrerId;
    this.confirmed = false;
    this.blocked = false;
    this.registerTime = DateTime.now();
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
  
  public void setEmail(String email) {
    checkNotNull(email);
    this.email = email;
  }
  
  public void changeEmail(String email) {
    setEmail(email);
    confirmed = false;
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
      customerAccount = new Account(false);
      customerSecret = UUID.randomUUID().toString();
    }
    if (role.equals(Role.DEVELOPER) && developerAccount == null)
      developerAccount = new Account(false);
    if (role.equals(Role.AFFILIATE) && developerAccount == null)
      developerAccount = new Account(false);
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

  public String firstName() {
    return firstName;
  }
  
  public void setFirstName(String firstName) {
    checkNotNull(firstName);
    this.firstName = firstName;
  }
  
  public String lastName() {
    return lastName;
  }
  
  public void setLastName(String lastName) {
    checkNotNull(lastName);
    this.lastName = lastName;
  }
  
  public String organization() {
    return organization;
  }
  
  public void setOrganization(String organization) {
    this.organization = organization;
  }
  
  public String phone() {
    return phone;
  }
  
  public void setPhone(String phone) {
    this.phone = phone;
  }
  
  public URI sourceUrl() {
    return sourceUrl != null ? URI.create(sourceUrl) : null;
  }
  
  public void setSourceUrl(URI sourceUrl) {
    this.sourceUrl = sourceUrl != null ? sourceUrl.toString() : null;
  }
  
  public MessengerType messengerType() {
    return messengerType;
  }
  
  public String messengerUid() {
    return messengerUid;
  }
  
  public void setMessenger(MessengerType type, String uid) {
    this.messengerType = type;
    this.messengerUid = uid;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public void changePasswordHash(String passwordHash) {
    checkNotNull(passwordHash);
    this.passwordHash = passwordHash;
  }
  
  public Long referrerId() {
    return referrerId;
  }
  
  public boolean confirmed() {
    return confirmed;
  }
  
  public void setConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }
  
  public boolean blocked() {
    return blocked;
  }
  
  public void setBlocked(boolean blocked) {
    this.blocked = blocked;
  }
  
  public DateTime registerTime() {
    return registerTime;
  }
  
  public UserStat stat() {
    return stat;
  }
  
  public int fee() {
    return fee;
  }
}
