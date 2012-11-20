package com.heymoose.domain.site;

import com.heymoose.domain.base.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "site_attribute")
public class SiteAttribute extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site-attribute-seq")
  @SequenceGenerator(name = "site-attribute-seq", sequenceName = "site_attribute_seq", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "site_id")
  private Site site;

  @Column(nullable = false)
  private String key;

  @Column(nullable = false)
  private String value;

  public SiteAttribute() { }

  @Override
  public Long id() {
    return this.id();
  }

  public SiteAttribute setKey(String key) {
    this.key = key;
    return this;
  }

  public SiteAttribute setValue(String value) {
    this.value = value;
    return this;
  }

  public SiteAttribute setSite(Site site) {
    this.site = site;
    return this;
  }
}
