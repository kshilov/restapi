package com.heymoose.infrastructure.service;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.site.BlackListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlackList {

  private static final Logger log = LoggerFactory.getLogger(BlackList.class);

  private final Repo repo;

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
}
