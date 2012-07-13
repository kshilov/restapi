package com.heymoose.domain.model;

import com.heymoose.domain.model.base.IdEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "ip_segment")
public class IpSegment extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ip-segment-seq")
  @SequenceGenerator(name = "ip-segment-seq", sequenceName = "ip_segment_seq", allocationSize = 1)
  private Long id;

  @Column(name = "start_ip_addr")
  private String startIpAddr;

  @Column(name = "end_ip_addr")
  private String endIpAddr;

  @Column(name = "start_ip_num")
  private Long startIpNum;

  @Column(name = "end_ip_num")
  private Long endIpNum;

  @Column(name = "country_code")
  private String countryCode;

  @Column(name = "country_name")
  private String countryName;
  
  public String countryCode() {
    return countryCode;
  }

  @Override
  public Long id() {
    return id;
  }
  
  public String code() {
    return countryCode;
  }
}
