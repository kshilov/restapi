package com.heymoose.resource;

import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlRegions;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Singleton
@Path("regions")
public class RegionResource {

  private final Repo repo;

  @Inject
  public RegionResource(Repo repo) {
    this.repo = repo;
  }

  @GET
  @Transactional
  public XmlRegions list() {
    List<Object[]> dbResult = repo.session()
        .createSQLQuery("select distinct country_code, country_name from ip_segment")
        .list();
    Map<String, String> countriesByCode = newHashMap();
    for (Object[] data : dbResult)
      countriesByCode.put((String) data[0], (String) data[1]);
    return Mappers.toXmlRegions(countriesByCode);
  }
}
