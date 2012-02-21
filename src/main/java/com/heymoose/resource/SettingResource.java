package com.heymoose.resource;

import com.heymoose.domain.settings.Settings;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.XmlSettings;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Singleton
@Path("settings")
public class SettingResource {

  private final Settings settings;

  @Inject
  public SettingResource(Settings settings) {
    this.settings = settings;
  }

  @GET
  @Path("value")
  @Transactional
  public String value(@QueryParam("name") String name) {
    checkNotNull(name);
    return settings.getString(name);
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
