package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.base.IdEntity;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    name = "banner_size",
    uniqueConstraints = @UniqueConstraint(columnNames = {"width", "height"})
)
public class BannerSize extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner-size-seq")
  @SequenceGenerator(name = "banner-size-seq", sequenceName = "banner_size_seq", allocationSize = 1)
  protected Long id;

  @Basic(optional = false)
  private int width;

  @Basic(optional = false)
  private int height;
  
  @Override
  public Long id() {
    return id;
  }

  protected BannerSize() {}

  public BannerSize(int width, int height) {
    checkArgument(width > 0);
    checkArgument(height > 0);
    this.width = width;
    this.height = height;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }
}
