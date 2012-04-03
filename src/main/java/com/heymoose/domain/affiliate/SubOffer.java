package com.heymoose.domain.affiliate;

import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.accounting.Account;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SubOffer extends BaseOffer {

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne()
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private Offer parent;

  protected SubOffer() {}

  @Override
  public Account account() {
    return parent.account();
  }

  public SubOffer(Long parentId, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, boolean autoApprove, boolean reentrant) {
    super(PayMethod.CPA, cpaPolicy, cost, percent, title, autoApprove, reentrant);
    checkNotNull(parentId, cpaPolicy);
    this.parentId = parentId;
  }
  
  public Long parentId() {
    return parentId;
  }
  
  public Offer parent() {
    return parent;
  }
}
