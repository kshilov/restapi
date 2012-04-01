package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
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
  @JoinColumn(name = "affiliate_tx_id")
  private AccountTx affiliateTx;

  @ManyToOne(optional = false)
  @JoinColumn(name = "admin_tx_id")
  private AccountTx adminTx;

  private OfferActionState state;

  @Override
  public Long id() {
    return id;
  }

  protected OfferAction() {}
  
  public OfferAction(Click click, Offer offer, String transactionId, AccountTx affiliateTx, AccountTx adminTx) {
    this.click = click;
    this.transactionId = transactionId;
    this.offer = offer;
    this.affiliateTx = affiliateTx;
    this.adminTx = adminTx;
    this.state = OfferActionState.NOT_APPROVED;
  }

  public void approve() {
    checkArgument(state == OfferActionState.NOT_APPROVED);
    this.state = OfferActionState.APPROVED;
  }

  public void cancel() {
    checkArgument(state == OfferActionState.NOT_APPROVED);
    this.state = OfferActionState.CANCELED;
  }

  public OfferActionState state() {
    return state;
  }

  public AccountTx affiliateTx() {
    return affiliateTx;
  }

  public AccountTx adminTx() {
    return adminTx;
  }
}
