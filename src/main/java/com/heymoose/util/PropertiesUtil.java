package com.heymoose.util;

import java.util.Properties;

public class PropertiesUtil {
  private PropertiesUtil() {}

  public static Properties subTree(Properties props, String prefix, String newPrefix) {
    prefix = prefix + ".";
    if (newPrefix != null)
      newPrefix = newPrefix + ".";
    else
      newPrefix = "";
    Properties ret = new Properties();
    for (String i : props.stringPropertyNames()) {
      if (!i.startsWith(prefix))
        continue;
      String suffix = i.substring(prefix.length());
      ret.put(newPrefix + suffix, props.get(i));
    }
    return ret;
  }

  public static void main(String[] args) {
    Properties p = new Properties();
    p.setProperty("foo.bar", "val");
    p.setProperty("baz.bag", "val2");
   // p = subTree(p, "foo", "foo");
    System.out.println(p);
  }
}
