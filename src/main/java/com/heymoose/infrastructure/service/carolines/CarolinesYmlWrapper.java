package com.heymoose.infrastructure.service.carolines;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.NoInfoException;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;
import com.heymoose.infrastructure.service.yml.YmlUtil;

import java.math.BigDecimal;
import java.util.List;

public final class CarolinesYmlWrapper extends YmlCatalogWrapperBase {

  private static final List<Integer> GROUP1 = ImmutableList.of(
      221,224,230,316,742,883,994,995,998,1018,1078,1101,1154,1160, 1161,1165,
      1167,1168,1169,1180,1182,1218,1234,1235,1236,1237,1238,1239,1242,1243,
      1244,1245,1246,1247,1248,1334,1337,1381,1389,1500,1535,1536,1544,1548,
      1549,1550,1552,1556,1557,1559,1560,1561,1563,1564,1565,1569,1571,1579,
      1580,1581,1584,1592,1597,1600,1601,1615,1618,1667,1668,1673,1675,1678,
      1679,1680,1681,1682,1683,1684,1685,1686,1688,1690,1691,1692,1693,1694,
      1697,1698,1729,1731,1732,1733,1734,1735,1736,1737,1738,1739,1740,1741,
      1743,1744,1745,1746,1747,1748,1749,1750,1751,1752,1753,1755,1756,1764,
      1775,1783,1784,1785,1786,1787,1788,1789,1793,1795,1807,1811,1812,1814,
      1815,1816,1821,1824,1825,1831,1835,1836,1837,1839,1840,1841,1842,1845,
      1936,1937,1943,1945,1947,1949,1951,1982,1985,1988,1994,1996,1998,2007,
      2012,2052,2054,2055,2056,2065,2066,2120,2126,2128,2129,2130,2139,2144);
  private static final BigDecimal GROUP1_COST = new BigDecimal(300);

  private static final List<Integer> GROUP2 = ImmutableList.of(
      219,222,231,234,243,315,373,380,384,798,799,800,851,875,882,1082,1249,
      1273,1434,1450,1471,1489,1609,1655,1699,1806,1834,1843,1975,1981,2114,
      2138);
  private static final BigDecimal GROUP2_COST = new BigDecimal(240);

  private static final List<Integer> GROUP3 = ImmutableList.of(
      202,227,238,241,251,252,307,308,311,314,318,372,378,399,400,567,569,581,
      632,633,634,635,672,710,717,719,720,722,764,766,767,779,781,783,785,796,
      797,871,872,997,1017,1019,1074,1079,1145,1147,1150,1231,1251,1278,1299,
      1301,1311,1315,1342,1352,1370,1372,1373,1382,1405,1409,1413,1414,1425,
      1428,1429,1440,1446,1449,1455,1459,1468,1472,1473,1476,1478,1486,1488,
      1520,1622,1627,1631,1632,1640,1641,1642,1643,1644,1645,1646,1647,1648,
      1649,1650,1651,1652,1654,1665,1669,1676,1677,1702,1708,1757,1758,1763,
      1771,1773,1790,1791,1792,1822,1828,1846,1931,1963,1965,1983,1984,2057,
      2072,2087,2088,2089,2092,2121,2122,2134,2135,2137,2140);
  private static final BigDecimal GROUP3_COST = new BigDecimal(145);

  private static final BigDecimal DEFAULT_COST = new BigDecimal(50);


  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.FIXED;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer)
      throws NoInfoException {
    Integer id = Integer.valueOf(catalogOffer.getId());
    if (GROUP1.contains(id))
      return GROUP1_COST;
    if (GROUP2.contains(id))
      return GROUP2_COST;
    if (GROUP3.contains(id))
      return GROUP3_COST;
    return DEFAULT_COST;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer)
      throws NoInfoException {
    Integer id = Integer.valueOf(catalogOffer.getId());
    return GROUP1.contains(id) || GROUP2.contains(id) || GROUP3.contains(id);
  }

  @Override
  public String getOfferTitle(Offer offer) {
    return YmlUtil.titleFor(offer, Vendor.class, Model.class);
  }
}
