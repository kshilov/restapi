package com.heymoose.rest.domain.app;


import com.google.common.collect.Maps;
import com.heymoose.rest.domain.account.Account;
import org.xml.sax.InputSource;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@Entity
@Table(name = "application")
public class App {

  @Id
  private Integer id;

  @Basic(optional = false)
  private String secret;

  @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  public App(int id, String secret) {
    this.id = id;
    this.secret = secret;
    this.account = new Account();
  }

  protected App() {
  }

  public Account account() {
    return account;
  }

  public String secret() {
    return secret;
  }

  public Integer id() {
    return id;
  }

  public void refreshSecret(String secret) {
    this.secret = secret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof App)) return false;

    App app = (App) o;

    if (id != null ? !id.equals(app.id) : app.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  private static final Map<ByteArrayWrapper, byte[]> cache = new HashMap<ByteArrayWrapper, byte[]>();

  public static byte[] generate(byte[] src) {
    byte[] generated = cache.get(key(src));
    if (generated == null) {
      synchronized (cache) {
        generated = cache.get(key(src));
        if (generated == null) {
          generated = doGenerate(src);
          cache.put(key(src), generated);
        }
      }
    }
    return generated;
  }

  private final static ConcurrentMap<String, byte[]> cache2 = new ConcurrentHashMap<String, byte[]>();

  public static byte[] generate2(byte[] src) {
    byte[] generated = cache2.get(key(src));
    return null;
  }

  public static class ByteArrayWrapper {

    public final byte[] target;

    public ByteArrayWrapper(byte[] target) {
      this.target = target;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ByteArrayWrapper that = (ByteArrayWrapper) o;

      if (!Arrays.equals(target, that.target)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(target);
    }
    
    public static ByteArrayWrapper create(byte[] from) {
      return new ByteArrayWrapper(from);
    }
  }

  private static ByteArrayWrapper key(byte[] b) {
    return ByteArrayWrapper.create(b);
  }

  private static byte[] doGenerate(byte[] src) {
    return new byte[] {1};
  }

  public static void main(String... args) throws IOException, XPathExpressionException {
    XPathExpression expr = XPathFactory
            .newInstance()
            .newXPath()
            .compile("/xml_api_reply/weather/current_conditions/temp_c/@data");
    URL weatherUrl = new URL("http://www.google.com/ig/api?weather=moscow");
    String temp = expr.evaluate(new InputSource(weatherUrl.openStream()));
    System.out.println(temp);
  }
}
