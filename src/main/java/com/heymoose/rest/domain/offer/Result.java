package com.heymoose.rest.domain.offer;

import com.heymoose.rest.domain.app.UserProfile;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_answer")
public abstract class Result<T extends Offer> extends IdEntity {

  @ManyToOne(targetEntity = Offer.class, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private T offer;

  @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private UserProfile user;

  protected Result(){}
  
  public Result(T offer, UserProfile user) {
    this.offer = offer;
    this.offer.addAnswer(this);
    this.user = user;
  }

  public T offer() {
    return offer;
  }

  public UserProfile user() {
    return user;
  }
}
