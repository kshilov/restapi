package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "form")
public class Form extends Reservable {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<BaseQuestion> questions;

  @Basic
  private int asked;

  private Form() {}

  public Form(Iterable<BaseQuestion> questions) {
    this.questions = Sets.newHashSet(questions);
  }

  public Set<BaseQuestion> questions() {
    if (questions == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(questions);
  }

  private void assertQuestions() {
    if (questions == null)
      questions = Sets.newHashSet();
  }

  public int asked() {
    return asked;
  }

  public void ask() {
    asked++;
  }
}
