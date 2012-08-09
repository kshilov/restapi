package com.heymoose.infrastructure.util;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;

public class HibernateUtil {

  @SuppressWarnings("unchecked")
  public static <T> T unproxy(T entity) {
    if (entity instanceof HibernateProxy)
      return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    return entity;
  }

  public static Criteria addEqRestrictionIfNotNull(Criteria criteria,
                                                    String paramName,
                                                    Object value) {
    if (value != null) {
      return criteria.add(Restrictions.eq(paramName, value));
    }
    return criteria;
  }

}
