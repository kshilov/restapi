package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.OrderBase;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "question_base")
public abstract class QuestionBase<T extends AnswerBase, F extends OrderBase> extends IdEntity {

  @OneToMany(targetEntity = AnswerBase.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "question")
  private Set<T> answers;

  @Basic
  protected int asked;

  @OneToMany(mappedBy = "question", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  private Set<Reservation> reservations;

  protected QuestionBase() {}

  public abstract F order();

  public abstract void setOrder(F order);

  public Set<T> answers() {
    if (answers == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(answers);
  }

  public void addAnswer(T answer) {
    if (answers == null)
      answers = Sets.newHashSet();
    answers.add(answer);
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
}
