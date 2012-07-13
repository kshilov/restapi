package com.heymoose.domain.model.mlm;

import com.heymoose.domain.model.base.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "mlm_execution")
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
