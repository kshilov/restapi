package com.heymoose.infrastructure.persistence;

import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
  public PostgreSQLDialect() {
    super();
    registerFunction("rand", new NoArgSQLFunction("random", StandardBasicTypes.DOUBLE, true));
  }
}
