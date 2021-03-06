package com.heymoose.infrastructure.service;

import org.apache.commons.io.IOUtils;
import sun.misc.BASE64Decoder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Singleton
public class BannerStore {

  private final String bannersDir;

  @Inject
  public BannerStore(@Named("banners-dir") String bannersDir) {
    this.bannersDir = bannersDir;
  }

  public File path(long bannerId) {
    File bannersDir = new File(this.bannersDir);
    if (!bannersDir.exists() || !bannersDir.isDirectory())
      throw new IllegalArgumentException("Bad directory: " + this.bannersDir);
    return new File(bannersDir, Long.toString(bannerId));
  }

  public void saveBanner(long bannerId, String base64) throws IOException {
    saveBanner(bannerId, decodeBase64(base64));
  }

  public byte[] getBannerBody(long bannerId) throws IOException {
    File bannerPath = path(bannerId);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    FileInputStream fileIn = new FileInputStream(bannerPath);
    try {
      IOUtils.copy(fileIn, byteOut);
    } finally {
      if (fileIn != null)
        fileIn.close();
    }
    return byteOut.toByteArray();
  }

  public void saveBanner(long bannerId, byte[] data) throws IOException {
    File bannerFile = path(bannerId);
    ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
    FileOutputStream fileOut = new FileOutputStream(bannerFile, false);
    try {
      IOUtils.copy(byteIn, fileOut);
    } finally {
      if (fileOut != null)
        fileOut.close();
    }
  }

  public byte[] decodeBase64(String base64) {
    BASE64Decoder decoder = new BASE64Decoder();
    try {
      return decoder.decodeBuffer(base64);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
