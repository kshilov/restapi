package com.heymoose.resource;

import com.heymoose.domain.settings.Settings;
import com.heymoose.infrastructure.persistence.KeywordPatternDao;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.resource.xml.XmlSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Singleton
@Path("settings")
public class SettingResource {

  private final Settings settings;
  private final KeywordPatternDao keywordPatternDao;

  @Inject
  public SettingResource(Settings settings, KeywordPatternDao keywordPatternDao) {
    this.settings = settings;
    this.keywordPatternDao = keywordPatternDao;
  }

  @GET
  @Path("value")
  @Transactional
  public String value(@QueryParam("name") String name) {
    checkNotNull(name);
    try {
      return settings.getString(name);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(404);
    }
  }

  @GET
  @Path("keywords/cache/reset")
  @Transactional
  public Response resetKeywordPatternCache() {
    keywordPatternDao.fetchKeywordPatters();
    return Response.ok().build();
  }

  @GET
  @Transactional
  public XmlSettings all() {
    XmlSettings xmlSettings = new XmlSettings();
    Map<String, String> settingsMap = settings.map();
    xmlSettings.M = settingsMap.get(Settings.M);
    xmlSettings.Q = settingsMap.get(Settings.Q);
    xmlSettings.Cmin = settingsMap.get(Settings.C_MIN);
    return xmlSettings;
  }

  @PUT
  @Transactional
  public void update(@FormParam("m") Double m, @FormParam("q") Double q, @FormParam("cmin") Double cmin) {
    if (m != null)
      settings.set(Settings.M, m);
    if (q != null)
      settings.set(Settings.Q, q);
    if (cmin != null)
      settings.set(Settings.C_MIN, cmin);
  }
}
