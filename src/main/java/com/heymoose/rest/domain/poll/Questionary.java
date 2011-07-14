package com.heymoose.rest.domain.poll;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "questionary")
public class Questionary extends IdEntity {

  private Set<BaseQuestion> questions;

  private Questionary() {}

  public Questionary(Iterable<BaseQuestion> questions) {
    this.questions = Sets.newHashSet(questions);
  }

  public Set<BaseQuestion> questions() {
    return Collections.unmodifiableSet(questions);
  }
}
