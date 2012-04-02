package com.heymoose.domain.affiliate.base;

import com.heymoose.domain.base.IdEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;

public interface Repo {
  <T extends IdEntity> T get(Class<T> clazz, long id);
  <T extends IdEntity> void put(T entity);
  <T extends IdEntity> T byHQL(Class<T> clazz, String hql, Object... params);
  <T extends IdEntity> List<T> allByHQL(Class<T> clazz, String hql, Object... params);
  <T extends IdEntity> T byCriteria(DetachedCriteria criteria);
  Criteria getExecutableCriteria(DetachedCriteria detachedCriteria);
  <T extends IdEntity> List<T> allByCriteria(DetachedCriteria criteria);
  <T extends IdEntity> Map<Long, T> get(Class<T> clazz, Set<Long> ids);
  <T extends IdEntity> T lock(T entity);
  <T extends IdEntity> void remove(T entity);
}
