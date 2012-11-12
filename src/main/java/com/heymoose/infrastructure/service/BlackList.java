package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.site.BlackListEntry;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.HibernateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlackList {

  private static final Logger log = LoggerFactory.getLogger(BlackList.class);

  private final Repo repo;

  @Inject
  public BlackList(Repo repo) {
    this.repo = repo;
  }

  public boolean ban(String url) {
    Iterable<BlackListEntry> entryList = repo.allByHQL(BlackListEntry.class,
        "from BlackListEntry where host = ?", BlackListEntry.extractHost(url));
    for (BlackListEntry entry : entryList) {
      if (entry.matches(url)) {
        log.info("Url {} banned because of {}", url, entry);
        return true;
      }
    }
    return false;
  }

  public BlackListEntry add(String host, String pathMask, String subDomainMask) {
    Preconditions.checkNotNull(host, "Host can not be null.");
    BlackListEntry entry = new BlackListEntry()
        .setHost(host)
        .setPathMask(pathMask)
        .setSubDomainMask(subDomainMask);
    repo.put(entry);
    return entry;
  }

  public boolean remove(Long id) {
    Preconditions.checkNotNull(id, "Id can not be null");
    return repo.session()
        .createQuery("delete from BlackListEntry where id = ?")
        .setParameter(0, id)
        .executeUpdate() > 0;
  }

  public BlackListEntry getById(Long id) {
    Preconditions.checkNotNull(id, "Id can not be null");
    return repo.get(BlackListEntry.class, id);
  }

  @SuppressWarnings("unchecked")
  public Pair<Iterable<BlackListEntry>, Long> getByHost(String host,
                                                        int offset, int limit) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(host),
        "Host can not be empty or null");
    return HibernateQuery.<BlackListEntry>create(
        "from BlackListEntry where host = ?", repo.session())
        .setParameter(0, host.toLowerCase())
        .executeAndCount(offset, limit);
  }

  @SuppressWarnings("unchecked")
  public Pair<Iterable<BlackListEntry>, Long> all(int offset, int limit) {
    return HibernateQuery.<BlackListEntry>create(
        "from BlackListEntry order by host", repo.session())
        .executeAndCount(offset, limit);
  }

  public BlackListEntry put(BlackListEntry entry) {
    repo.put(entry);
    return entry;
  }
}
