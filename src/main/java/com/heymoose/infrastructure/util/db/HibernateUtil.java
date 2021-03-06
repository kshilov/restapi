package com.heymoose.infrastructure.util.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

import java.util.Collection;

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


  public static Criterion sqlInRestriction(String sql,
                                           Collection<?> elementCollection,
                                           Type type) {
    Type[] typeArray = new Type[elementCollection.size()];
    StringBuilder inPart = new StringBuilder();
    inPart.append("in (");
    for (int i = 0; i < elementCollection.size(); i++) {
      if (i != elementCollection.size() - 1) {
        inPart.append("?, ");
      } else {
        inPart.append("?) ");
      }
      typeArray[i] = type;
    }
    sql = sql.replace("in (?)", inPart.toString());
    return Restrictions.sqlRestriction(
        sql, elementCollection.toArray(), typeArray);
  }


  public static Criteria addSqlInRestriction(Criteria criteria, String sql,
                                             Collection<?> elementCollection,
                                             Type type) {
    if (elementCollection.size() == 0) {
      return criteria;
    }
    criteria.add(sqlInRestriction(sql, elementCollection, type));
    return criteria;
  }



  public static DetachedCriteria addEqOrIsNull(DetachedCriteria criteria,
                                               String property, Object value) {
    if (value == null) return criteria.add(Restrictions.isNull(property));
    return criteria.add(Restrictions.eq(property, value));
  }

}
