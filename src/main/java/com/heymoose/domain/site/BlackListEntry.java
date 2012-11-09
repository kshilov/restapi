package com.heymoose.domain.site;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.heymoose.domain.base.IdEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "black_list")
public final class BlackListEntry extends IdEntity {

  private static final Logger log =
      LoggerFactory.getLogger(BlackListEntry.class);

  private static final Pattern HOST_PATTERN =
      Pattern.compile("(www\\.)?(([^\\.]+\\.)*)([^\\.]+\\.[^\\.]+)");

  private static final Pattern REG_EXP =
      Pattern.compile(
          "(https?://)?"
          + "(www\\.)?"
          + "(([^\\./]+\\.)*)"        // sub domains
          + "([^\\./]+\\.[^\\./]+)"  // host
          + "((/[^\\./]+)*)/?"       // path
          + ".*");                  // query string and hash


  public static String extractHost(String test) {
    try {
      URL url = new URL(test);
      Matcher hostMatcher = HOST_PATTERN.matcher(url.getHost());
      if (!hostMatcher.matches())
        throw new IllegalArgumentException("Bad url " + test);
      return hostMatcher.group(4);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "black_list-seq")
  @SequenceGenerator(name = "black_list-seq", sequenceName = "black_list_seq", allocationSize = 1)
  private Long id;

  @Basic(optional = false)
  private String host;

  @Column(name = "sub_domain_mask")
  private String subDomainMask;

  @Column(name = "path_mask")
  private String pathMask;

  @Override
  public Long id() {
    return id;
  }

  public BlackListEntry setHost(String host) {
    this.host = host;
    return this;
  }

  public String host() {
    return this.host;
  }

  public BlackListEntry setSubDomainMask(String subDomainMask) {
    this.subDomainMask = subDomainMask;
    return this;
  }

  public BlackListEntry setPathMask(String pathMask) {
    this.pathMask = pathMask;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(BlackListEntry.class)
        .add("host", host)
        .add("subDomainMask", subDomainMask)
        .add("pathMask", pathMask)
        .toString();
  }

  public boolean matches(String test) {
    if (!test.contains("://")) {
      test = "http://" + test;
    }
    try {
      URL url = new URL(test);
      Matcher hostMatcher = HOST_PATTERN.matcher(url.getHost());
      if (!hostMatcher.matches()) return false;
      if (!hostMatcher.group(4).equals(host)) return false;

      String subDomainString = hostMatcher.group(2);
      if (!Strings.isNullOrEmpty(subDomainMask)) {
        if (Strings.isNullOrEmpty(subDomainString)) return false;
        // remove last dot
        subDomainString = subDomainString
            .substring(0, subDomainString.length() - 1);
        Pattern subPattern = Pattern.compile(subDomainMask);
        if (!subPattern.matcher(subDomainString).matches()) return false;
      }

      // path
      String path = url.getPath();
      if (!Strings.isNullOrEmpty(pathMask)) {
        // remove first slash
        if (!Strings.isNullOrEmpty(path)) path = path.substring(1);
        Pattern pathPattern = Pattern.compile(pathMask);
        if (!pathPattern.matcher(path).matches()) return false;
      }

      return true;
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private void logMatcher(Matcher matcher) {
    if (matcher.matches()) {
      for (int i = 0; i <= matcher.groupCount(); i++) {
        log.debug("Group #{}: {}", i, matcher.group(i));
      }
    } else {
      log.debug("Not matched");
    }
  }
}
