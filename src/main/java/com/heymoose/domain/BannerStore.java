package com.heymoose.domain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import sun.misc.BASE64Decoder;

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
  
  public void saveBanner(Banner banner) throws IOException {
    saveBanner(banner.id(), decodeBase64(banner.imageBase64()));
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
