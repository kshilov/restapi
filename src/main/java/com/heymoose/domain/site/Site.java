package com.heymoose.domain.site;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.base.Moderatable;
import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.user.User;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "site")
public class Site extends ModifiableEntity implements Moderatable {

  public enum Type { WEB_SITE, SOCIAL_NETWORK, GRANT }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site-seq")
  @SequenceGenerator(name = "site-seq", sequenceName = "site_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @Column(name = "aff_id")
  private Long affId;

  @Enumerated(EnumType.STRING)
  private Type type;

  @Column(name = "admin_state", nullable = false)
  @Enumerated(EnumType.STRING)
  private AdminState adminState = AdminState.MODERATION;

  @Column(name = "admin_comment")
  private String adminComment;

  @Column(name = "description")
  private String description;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "site",
      cascade = CascadeType.REMOVE)
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

  public Site setName(String name) {
    this.name = name;
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

  public String name() {
    return this.name;
  }

  public Type type() {
    return type;
  }

  public User affiliate() {
    return affiliate;
  }

  public Map<String, String> attributeMap() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    for (SiteAttribute entry : attributeList) {
      builder.put(entry.key(), entry.value());
    }
    return builder.build();
  }

  @Override
  public void touch() {
    super.touch();
  }

  public boolean approvedByAdmin() {
    return AdminState.APPROVED == adminState;
  }

  public Site setDescription(String description) {
    this.description = description;
    return this;
  }

  public String description() {
    return this.description;
  }

  public boolean matches(String referer) {
    switch (this.type) {
      case WEB_SITE:
        if (!referer.contains("://")) referer = "http://" + referer;
        try {
          URL refererUrl = new URL(referer);
          URL siteUrl = new URL(this.attributeMap().get("url"));
          return siteUrl.getHost().equals(refererUrl.getHost());
        } catch (MalformedURLException e) {
          throw new RuntimeException("Illegal url. " + referer + " " + this);
        }
      case SOCIAL_NETWORK:
        return this.attributeMap().get("url").equals(referer);
      default: return true;
    }
  }
  @Override
  public AdminState adminState() {
    return adminState;
  }

  @Override
  public String adminComment() {
    return this.adminComment;
  }

  public Site setAdminComment(String comment) {
    this.adminComment = comment;
    return this;
  }


  public Site setAdminState(AdminState state) {
    this.adminState = state;
    return this;
  }

  public Site setAttribute(String name, String value) {
    boolean set = false;
    for (SiteAttribute attr : attributeList) {
      if (attr.key().equals(name)) {
        set = true;
        attr.setValue(value);
      }
    }
    if (!set) attributeList.add(new SiteAttribute()
        .setKey(name)
        .setValue(value));
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(Site.class)
        .add("id", id)
        .add("type", type)
        .add("name", name)
        .add("adminState", adminState)
        .toString();
  }
}
