package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user_profile")
public class UserProfile extends IdEntity {

  @Column(name = "ext_id")
  private String extId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_id")
  private App app;

  public UserProfile(String extId, App app) {
    this.extId = extId;
    this.app = app;
  }

  public String extId() {
    return extId;
  }

  public App app() {
    return app;
  }
}
