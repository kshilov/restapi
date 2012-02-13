package com.heymoose.resource;

import com.heymoose.domain.settings.Settings;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
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
  @Transactional
  public String get(@QueryParam("name") String name) {
    checkNotNull(name);
    return settings.getString(name);
  }
}
