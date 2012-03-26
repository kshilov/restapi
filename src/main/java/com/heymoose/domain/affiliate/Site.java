package com.heymoose.domain.affiliate;

import com.heymoose.domain.User;
import com.heymoose.domain.base.IdEntity;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "banner_site")
public class Site extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site-seq")
  @SequenceGenerator(name = "site-seq", sequenceName = "site_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @Basic(optional = false)
  private String domain;

  @Enumerated(EnumType.STRING)
  @Column(name = "lang", nullable = false)
  private Lang lang;

  @Basic(optional = false)
  private String comment;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User affiliate;

  @ElementCollection
  private Set<String> categories;

  @Override
  public Long id() {
    return id;
  }

  protected Site() {}
  
  public Site(String name, String domain, Lang lang, String comment, User affiliate) {
    this.name = name;
    this.domain = domain;
    this.lang = lang;
    this.comment = comment;
    this.affiliate = affiliate;
  }
}
