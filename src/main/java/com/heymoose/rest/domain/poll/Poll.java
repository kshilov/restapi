package com.heymoose.rest.domain.poll;

import com.google.common.collect.Sets;
import com.heymoose.rest.domain.order.Order;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "poll")
public class Poll extends BaseQuestion {

  @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Choice> choices;

  public Poll(String text, Order order, Iterable<Choice> choices) {
    super(text, order);
    this.choices = Sets.newHashSet(choices);
  }

  public Set<Choice> choices() {
    if (choices == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(choices);
  }
}
