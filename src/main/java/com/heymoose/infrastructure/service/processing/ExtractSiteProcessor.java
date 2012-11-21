package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.infrastructure.service.Sites;

public final class ExtractSiteProcessor implements Processor {

  private final Sites sites;

  @Inject
  public ExtractSiteProcessor(Sites sites) {
    this.sites = sites;
  }

  @Override
  public void process(ProcessableData data) {
    Long siteId = data.token().stat().siteId();
    if (siteId == null) return;
    data.setSite(sites.get(siteId));
  }
}
