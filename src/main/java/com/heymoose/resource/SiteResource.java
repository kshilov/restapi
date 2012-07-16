package com.heymoose.resource;

import static com.google.common.collect.Sets.newHashSet;

import com.heymoose.domain.user.Lang;
import com.heymoose.domain.user.Site;
import com.heymoose.domain.user.User;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Singleton
@Path("sites")
public class SiteResource {

  private final Repo repo;

  @Inject
  public SiteResource(Repo repo) {
    this.repo = repo;
  }

  @POST
  @Transactional
  public void register(@FormParam("userId") Long userId,
                       @FormParam("name") String name,
                       @FormParam("domain") String domain,
                       @FormParam("lang") Lang lang,
                       @FormParam("comment") String comment,
                       @FormParam("category") List<Long> categories,
                       @FormParam("region") List<String> regions) {

    checkNotNull(userId, name, domain, lang, comment);
    User user = repo.get(User.class, userId);
    if (user == null)
      throw notFound();
    Map<Long, Category> categoryMap = repo.get(Category.class, newHashSet(categories));
    Site site = new Site(name, domain, lang, comment, user, newHashSet(categoryMap.values()), newHashSet(regions));
    repo.put(site);
  }
}
