package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.offer.CategoryGroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "category-groups")
public final class XmlCategoryGroups {

  private static class XmlCategoryShort {

    @XmlAttribute
    public Long id;

    @XmlElement
    public String name;

    XmlCategoryShort() { }

    public XmlCategoryShort(Category category) {
      this.id = category.id();
      this.name = category.name();
    }
  }

  private static final class XmlCategoryGroup {
    @XmlAttribute
    public Long id;

    @XmlAttribute
    public String name;

    @XmlElement(name = "category")
    List<XmlCategoryShort> categoryList = Lists.newArrayList();

    XmlCategoryGroup() { }

    public XmlCategoryGroup(CategoryGroup group) {
      this.id = group.id();
      this.name = group.name();
      for (Category category : group.categorySet()) {
        this.categoryList.add(new XmlCategoryShort(category));
      }
    }
  }

  @XmlElement(name = "group")
  public List<XmlCategoryGroup> groupList = Lists.newArrayList();

  XmlCategoryGroups() { }

  public XmlCategoryGroups(Iterable<CategoryGroup> groupList) {
    for (CategoryGroup group : groupList) {
      this.groupList.add(new XmlCategoryGroup(group));
    }
  }
}
