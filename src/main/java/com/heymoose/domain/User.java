package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Set;

@Entity
@Table(name = "user_profile", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User extends IdEntity {

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  public Set<Order> orders;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  public Set<App> apps;

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  @Enumerated
  public Set<Role> roles;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "customer_account_id")
  public Account customerAccount;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "developer_account_id")
  public Account developerAccount;

  @Basic(optional = false)
  public String email;

  @Basic(optional = false)
  public String passwordHash;

  @Basic(optional = false)
  public String nickname;
}
