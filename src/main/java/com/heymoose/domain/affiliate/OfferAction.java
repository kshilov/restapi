package com.heymoose.domain.affiliate;

import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Offer;
import com.heymoose.domain.affiliate.base.BaseEntity;
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
@Table(name = "offer_action")
public class OfferAction extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-action-seq")
  @SequenceGenerator(name = "offer-action-seq", sequenceName = "offer_action_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "click_id")
  private Click click;

  @ManyToOne(optional = false)
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "account_tx_id")
  private AccountTx accountTx;

  @Override
  public Long id() {
    return id;
  }

  protected OfferAction() {}
  
  public OfferAction(Click click, Offer offer, String transactionId, AccountTx accountTx) {
    this.click = click;
    this.transactionId = transactionId;
    this.offer = offer;
    this.accountTx = accountTx;
  }
}
