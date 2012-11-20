package com.heymoose.domain.site;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.user.User;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "site")
public class Site extends ModifiableEntity {

  public enum Type { WEB_SITE, SOCIAL_NETWORK }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site-seq")
  @SequenceGenerator(name = "site-seq", sequenceName = "site_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @Column(name = "aff_id")
  private Long affId;

  @Enumerated(EnumType.STRING)
  private Type type;

  @Column(name = "approved")
  private boolean approvedByAdmin = false;


  @OneToMany(mappedBy = "site")
  private List<SiteAttribute> attributeList = Lists.newArrayList();


  @Override
  public Long id() {
    return id;
  }

  protected Site() {}
  
  public Site(Type type) {
    this.type = type;
  }

  public Site setType(Type type) {
    this.type = type;
    return this;
  }

  public Site setDescription(String description) {
    this.description = description;
    return this;
  }

  public Site setAffId(Long affId) {
    this.affId = affId;
    return this;
  }

  public Site addAttribute(String key, String value) {
    SiteAttribute attr = new SiteAttribute()
        .setSite(this)
        .setKey(key)
        .setValue(value);
    this.attributeList.add(attr);
    return this;
  }

  public Site addAttributesFromMap(Map<String, String> siteAttributeMap) {
    for (Map.Entry<String, String> entry : siteAttributeMap.entrySet()) {
      addAttribute(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public List<SiteAttribute> attributeList() {
    return ImmutableList.copyOf(this.attributeList);
  }


}
