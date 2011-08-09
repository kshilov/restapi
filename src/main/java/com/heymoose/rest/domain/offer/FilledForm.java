package com.heymoose.rest.domain.offer;

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
public class FilledForm extends Result<Form> {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "filled_form_id")
  private Set<Result<SingleQuestion>> results;

  protected FilledForm() {}

  public FilledForm(Iterable<Result<SingleQuestion>> results) {
    Set<SingleQuestion> questions = Sets.newHashSet();
    for (Result<SingleQuestion> result : results)
      questions.add(result.offer());
    if (!questions.equals(offer().questions()))
      throw new IllegalArgumentException("Answers does not matches questions");
    this.results = Sets.newHashSet(results);
  }

  public Set<Result<SingleQuestion>> results() {
    if (results == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(results);
  }
}
