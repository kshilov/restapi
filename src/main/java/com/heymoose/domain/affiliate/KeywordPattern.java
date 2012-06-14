package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "keyword_pattern")
public class KeywordPattern extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "keyword-pattern-id-seq")
  @SequenceGenerator(name = "keyword-pattern-id-seq", sequenceName = "keyword_pattern_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "url_pattern")
  private String urlPattern;

  @Column(name = "keywords_parameter")
  private String keywordsParameter;

  @Override
  public Long id() {
    return id;
  }

  public String urlPattern() {
    return urlPattern;
  }

  public String keywordsParameter() {
    return keywordsParameter;
  }
}
