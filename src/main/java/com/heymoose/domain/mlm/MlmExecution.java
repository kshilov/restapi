package com.heymoose.domain.mlm;

import com.heymoose.domain.affiliate.base.BaseEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class MlmExecution extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mlm-execution-seq")
  @SequenceGenerator(name = "mlm-execution-seq", sequenceName = "mlm_execution_seq", allocationSize = 1)
  private Long id;

  @Override
  public Long id() {
    return id;
  }
}
