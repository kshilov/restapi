package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.BaseOrder;
import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "reservable")
public abstract class Reservable<T extends BaseOrder> extends IdEntity {

  @Basic
  protected int asked;

  protected Reservable() {}

  @OneToMany(mappedBy = "target", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  private Set<Reservation> reservations;

  public void addReservation(Reservation reservation) {
    assertReservations();
    reservations.add(reservation);
  }

  public Set<Reservation> reservations() {
    if (reservations == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(reservations);
  }

  private void assertReservations() {
    if (reservations == null)
      reservations = Sets.newHashSet();
  }

  public int asked() {
    return asked;
  }

  public void ask() {
    asked++;
  }

  public void restore() {
    asked--;
  }

  public abstract void setOrder(T order);

  public abstract T order();
}
