package com.heymoose.domain.model;

import com.heymoose.domain.model.base.BaseEntity;
import java.io.IOException;
import java.math.BigInteger;
import static java.util.Collections.emptyMap;
import java.util.Map;
import java.util.Random;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.heymoose.domain.model.statistics.OfferStat;
import org.codehaus.jackson.map.ObjectMapper;

@Entity
@Table(name = "token")
public class Token extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token-seq")
  @SequenceGenerator(name = "token-seq", sequenceName = "token_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String value;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "stat_id")
  private OfferStat stat;

  @Column(name = "aff_params", nullable = true)
  private String affParamsJson;

  @Override
  public Long id() {
    return id;
  }

  protected Token() {
  }

  public Token(OfferStat stat) {
    this.stat = stat;
    Random random = new Random();
    this.value = new BigInteger(160, random).toString(32);
  }

  public OfferStat stat() {
    return stat;
  }

  public String value() {
    return value;
  }

  public Map<String, String> affParams() {
    if (affParamsJson == null)
      return emptyMap();
    return fromJson(affParamsJson);
  }

  public void setAffParams(Map<String, String> affParams) {
    affParamsJson = toJson(affParams);
  }

  private static String toJson(Map<String, String> map) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(map);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private static Map<String, String> fromJson(String json) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return (Map<String, String>) mapper.readValue(json, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
