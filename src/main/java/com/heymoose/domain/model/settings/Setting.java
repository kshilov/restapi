package com.heymoose.domain.model.settings;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "setting")
public class Setting {
  
  @Id
  protected String name;
  
  @Basic(optional = false)
  protected String value;
  
  protected Setting() {}
  
  public Setting(String name, String value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Setting setting = (Setting) o;

    if (!name.equals(setting.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
