package com.heymoose.domain.action;

import com.google.common.base.Objects;
import com.heymoose.domain.base.ModifiableEntity;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Table(name = "offer_action")
public class OfferAction extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-action-seq")
  @SequenceGenerator(name = "offer-action-seq", sequenceName = "offer_action_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "stat_id")
  private OfferStat stat;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_id")
  private OfferStat source;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aff_id")
  private User affiliate;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private BaseOffer offer;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Enumerated
  private OfferActionState state;

  @OneToOne
  @JoinColumn(name = "token_id")
  private Token token;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(name = "purchase_price", nullable = true)
  private BigDecimal purchasePrice;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "site_id")
  private Site site;

  @Override
  public Long id() {
    return id;
  }

  public OfferAction() {}

  public OfferAction(Token token, User affiliate, OfferStat stat, OfferStat source, BaseOffer offer, String transactionId) {
    this.token = token;
    this.affiliate = affiliate;
    this.stat = stat;
    this.source = source;
    this.transactionId = transactionId;
    this.offer = offer;
    this.state = OfferActionState.NOT_APPROVED;
  }

  public User affiliate() {
    return affiliate;
  }

  public BaseOffer offer() {
    return offer;
  }

  public OfferStat stat() {
    return stat;
  }

  public void approve() {
    checkArgument(state == OfferActionState.NOT_APPROVED);
    this.state = OfferActionState.APPROVED;
    touch();
  }

  public void cancel() {
    checkArgument(state == OfferActionState.NOT_APPROVED);
    this.state = OfferActionState.CANCELED;
    touch();
  }

  public OfferActionState state() {
    return state;
  }

  public String transactionId() {
    return transactionId;
  }

  public OfferAction setCreationTime(DateTime time) {
    this.creationTime = time;
    return this;
  }

  public Token token() {
    return this.token;
  }

  public Product product() {
    return this.product;
  }

  public OfferAction setProduct(Product product) {
    this.product = product;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(OfferAction.class)
        .add("id", id)
        .add("state", state)
        .add("offer", offer)
        .add("token", token)
        .add("transactionId", transactionId)
        .add("product", product).toString();
  }

  public OfferAction setId(Long id) {
    this.id = id;
    return this;
  }

  public OfferAction setAffiliate(User user) {
    this.affiliate = user;
    return this;
  }

  public OfferAction setOffer(Offer offer) {
    this.offer = offer;
    return this;
  }

  public OfferAction setToken(Token token) {
    this.token = token;
    return this;
  }

  public OfferAction setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public OfferAction setPurchasePrice(BigDecimal price) {
    this.purchasePrice = price;
    return this;
  }

  public BigDecimal purchasePrice() {
    return purchasePrice;
  }

  public OfferAction setSite(Site site) {
    this.site = site;
    return this;
  }
}
