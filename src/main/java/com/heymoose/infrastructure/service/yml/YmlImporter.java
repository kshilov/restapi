package com.heymoose.infrastructure.service.yml;

public interface YmlImporter {
  void doImport(YmlCatalogWrapper catalog, Long parentOfferId);
}
