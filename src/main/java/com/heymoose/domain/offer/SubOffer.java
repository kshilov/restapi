package com.heymoose.domain.offer;

import com.heymoose.domain.accounting.Account;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Set;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Entity
@DiscriminatorValue("2")
public class SubOffer extends BaseOffer {

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private Offer parent;

  @Override
  public Account account() {
    return parent.account();
  }

  @Override
  public long master() {
    return parentId;
  }

  @Override
  public Offer masterOffer() {
    return this.parent();
  }

  @Override
  public Set<String> regions() {
    return parent().regions();
  }

  public SubOffer() { }

  public SubOffer(Long parentId, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal cost2, BigDecimal percent,
                  String title, boolean autoApprove, boolean reentrant, String code, int holdDays) {
    super(PayMethod.CPA, cpaPolicy, cost, cost2, percent, title, autoApprove, reentrant, code, holdDays);
    checkNotNull(parentId, cpaPolicy);
    this.parentId = parentId;
    this.active = true;
  }
  
  public Long parentId() {
    return parentId;
  }
  
  public Offer parent() {
    return parent;
  }

  public SubOffer setParentId(Long parentId) {
    this.parentId = parentId;
    return this;
  }
}
