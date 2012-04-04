package com.heymoose.domain.affiliate;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.User;
import com.heymoose.domain.base.IdEntity;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "site")
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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User affiliate;

  @ManyToMany
  @JoinTable(
      name = "site_category",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
  )
  private Set<Category> categories;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(
      name = "site_region",
      joinColumns = @JoinColumn(name = "site_id", referencedColumnName = "id")
  )
  @Column(name = "region")
  private Set<Region> regions;

  @Override
  public Long id() {
    return id;
  }

  protected Site() {}
  
  public Site(String name, String domain, Lang lang, String comment, User affiliate, Set<Category> categories, Set<Region> regions) {
    this.name = name;
    this.domain = domain;
    this.lang = lang;
    this.comment = comment;
    this.affiliate = affiliate;
    this.categories = newHashSet(categories);
    this.regions = newHashSet(regions);
  }
  
  public Set<Category> categories() {
    if (categories == null)
      return emptySet();
    return unmodifiableSet(categories);
  }
  
  public Set<Region> regions() {
    if (regions == null)
      return emptySet();
    return unmodifiableSet(regions);
  }
}
