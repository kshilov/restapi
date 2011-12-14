package com.heymoose.util;

import org.hibernate.proxy.HibernateProxy;

public class HibernateUtil {
  
  public static <T> T unproxy(T entity) {
    if (entity instanceof HibernateProxy)
      return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    return entity;
  }
  
}
