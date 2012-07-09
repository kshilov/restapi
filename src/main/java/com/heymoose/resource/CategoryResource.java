package com.heymoose.resource;

import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.SubCategory;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlCategories;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.List;

@Path("categories")
@Singleton
public class CategoryResource {

  private final Repo repo;

  @Inject
  public CategoryResource(Repo repo) {
    this.repo = repo;
  }

  @Path("parent")
  @PUT
  @Transactional
  public String saveParent(@FormParam("id") Long id,
                           @FormParam("name") String name) {
    Category category = new Category(id, name);
    repo.session().saveOrUpdate(category);
    return category.id().toString();
  }

  @Path("sub")
  @PUT
  @Transactional
  public String saveSub(@FormParam("id") Long id,
                        @FormParam("name") String name,
                        @FormParam("category_id") Long categoryId) {
    SubCategory sub = new SubCategory(id, name, categoryId);
    repo.session().saveOrUpdate(sub);
    return sub.id().toString();
  }

  @GET
  @Transactional
  public XmlCategories list() {
    DetachedCriteria criteria = DetachedCriteria.forClass(Category.class)
        .addOrder(Order.asc("id"));
    List<Category> categories = repo.allByCriteria(criteria);
    return Mappers.toXmlCategories(categories);
  }
}
