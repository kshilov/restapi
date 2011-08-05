package com.heymoose.rest.domain.question;

import com.google.common.collect.Sets;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "filled_form")
public class FilledForm extends AnswerBase<Form> {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "filled_form_id")
  private Set<AnswerBase<SingleQuestion>> answers;

  protected FilledForm() {}

  public FilledForm(Iterable<AnswerBase<SingleQuestion>> answers) {
    Set<SingleQuestion> questions = Sets.newHashSet();
    for (AnswerBase<SingleQuestion> answer : answers)
      questions.add(answer.question());
    if (!questions.equals(question().questions()))
      throw new IllegalArgumentException("Answers does not matches questions");
    this.answers = Sets.newHashSet(answers);
  }

  public Set<AnswerBase<SingleQuestion>> answers() {
    if (answers == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(answers);
  }
}
