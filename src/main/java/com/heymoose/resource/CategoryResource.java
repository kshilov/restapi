package com.heymoose.resource;

import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.CategoryGroup;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlCategories;
import com.heymoose.resource.xml.XmlCategoryGroups;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("categories")
@Singleton
public class CategoryResource {

  private final Repo repo;

  @Inject
  public CategoryResource(Repo repo) {
    this.repo = repo;
  }

  @POST
  @Path("groups")
  @Transactional
  public String createGroup(@FormParam("name") String name) {
    CategoryGroup group = new CategoryGroup(name);
    repo.session().save(group);
    return group.id().toString();
  }

  @PUT
  @Path("groups/{id}")
  @Transactional
  public Response updateGroup(@PathParam("id") Long id,
                            @FormParam("name") String name) {
    CategoryGroup group = new CategoryGroup(id, name);
    repo.session().update(group);
    return Response.ok().build();
  }

  @DELETE
  @Path("groups/{id}")
  @Transactional
  public Response deleteGroup(@PathParam("id") Long id) {
    int rows = repo.session()
        .createQuery("delete from CategoryGroup where id = :id")
        .setParameter("id", id)
        .executeUpdate();
    if (rows == 1)
      return Response.ok().build();
    return Response.status(404).build();
  }

  @GET
  @Path("groups")
  @Transactional
  @SuppressWarnings("unchecked")
  public XmlCategoryGroups listGroups() {
    List<CategoryGroup> groupList = repo.session()
        .createCriteria(CategoryGroup.class)
        .addOrder(Order.asc("id"))
        .list();
    return new XmlCategoryGroups(groupList);
  }

  @POST
  @Transactional
  public String createCategory(@FormParam("name") String name,
                               @FormParam("category_group_id") Long groupId) {
    Category category = new Category(name, groupId);
    repo.session().save(category);
    return category.id().toString();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response updateCategory(@PathParam("id") Long id,
                               @FormParam("name") String name,
                               @FormParam("category_group_id")
                               Long categoryGroupId) {
    Category category = new Category(id, name, categoryGroupId);
    repo.session().update(category);
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response deleteCategory(@PathParam("id") Long id) {
    int rows = repo.session()
        .createQuery("delete from Category where id = :id")
        .setParameter("id", id)
        .executeUpdate();
    if (rows == 1)
      return Response.ok().build();
    return Response.status(404).build();
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
