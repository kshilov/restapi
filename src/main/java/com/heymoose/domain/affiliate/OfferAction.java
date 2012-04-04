package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.affiliate.base.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "click_id")
  private ClickStat click;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private BaseOffer offer;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Enumerated
  private OfferActionState state;

  @Override
  public Long id() {
    return id;
  }

  protected OfferAction() {}
  
  public OfferAction(ClickStat click, BaseOffer offer, String transactionId) {
    this.click = click;
    this.transactionId = transactionId;
    this.offer = offer;
    this.state = OfferActionState.NOT_APPROVED;
  }

  public ClickStat click() {
    return click;
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
}
