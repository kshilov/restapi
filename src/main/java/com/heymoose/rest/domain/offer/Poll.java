package com.heymoose.rest.domain.offer;

import com.google.common.collect.Sets;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "poll")
public class Poll extends SingleQuestion<Vote> {

  private Poll() {}

  @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Choice> choices;

  public Poll(String text, Iterable<Choice> choices) {
    super(text);
    this.choices = Sets.newHashSet(choices);
    for (Choice c : this.choices)
      c.setPoll(this);
  }

  public Set<Choice> choices() {
    if (choices == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(choices);
  }
}
