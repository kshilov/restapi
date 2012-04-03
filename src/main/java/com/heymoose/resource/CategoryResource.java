package com.heymoose.resource;

import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlCategories;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.hibernate.criterion.DetachedCriteria;

@Path("categories")
@Singleton
public class CategoryResource {

  private final Repo repo;

  @Inject
  public CategoryResource(Repo repo) {
    this.repo = repo;
  }

  @GET
  @Transactional
  public XmlCategories list() {
    List<Category> categories = repo.allByCriteria(DetachedCriteria.forClass(Category.class));
    return Mappers.toXmlCategories(categories);
  }
}
