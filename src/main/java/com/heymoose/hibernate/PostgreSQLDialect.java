package com.heymoose.hibernate;

import org.hibernate.Hibernate;
import org.hibernate.dialect.function.NoArgSQLFunction;

public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
  public PostgreSQLDialect() {
    super();
    registerFunction( "rand", new NoArgSQLFunction("random", Hibernate.DOUBLE) );
  }
}
