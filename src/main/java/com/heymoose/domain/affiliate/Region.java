package com.heymoose.domain.affiliate;

import static com.google.common.collect.Sets.newHashSet;
import java.util.Set;

public enum Region {

  RUSSIAN("RU"),
  UKRAINE("UA"),
  BELARUS("BY"),
  POLAND("PL"),
  LATVIA("LV"),
  GERMANY("DE"),
  CSI("AZ", "AM", "BY", "KZ", "KG", "MD", "RU", "TJ", "TM", "UZ", "UA");

  private Set<String> codes;
  
  private Region(String... codes) {
    this.codes = newHashSet(codes);
  }
  
  public static Set<Region> find(String code) {
    Set<Region> ret = newHashSet();
    for (Region region : Region.values())
      if (region.codes.contains(code))
        ret.add(region);
    return ret;
  }
}
