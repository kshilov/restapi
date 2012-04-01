package com.heymoose.domain.affiliate.base;

import com.heymoose.domain.base.IdEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.criterion.DetachedCriteria;

public interface Repo {
  public <T extends IdEntity> T get(Class<T> clazz, long id);
  public <T extends IdEntity> void put(T entity);
  public <T extends IdEntity> T byHQL(Class<T> clazz, String hql, Object... params);
  public <T extends IdEntity> T byCriteria(DetachedCriteria criteria);
  public <T extends IdEntity> Map<Long, T> get(Class<T> clazz, Set<Long> ids);
}