package com.heymoose.resource.api.data;

import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.Offer;
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.VideoOffer;
import java.math.BigDecimal;

public class OfferData {

  public final Long id;
  public final Integer type;
  public final String title;
  public final String payment;
  public final String description;
  public final String image;
  public final String videoUrl;

  public OfferData(Offer offer, BigDecimal payment, String description, String image, String videoUrl) {
    this.id = offer.id();
    this.type = offer.type().ordinal();
    this.title = offer.title();
    this.payment = payment.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
    this.description = description;
    this.image = image;
    this.videoUrl = videoUrl;
  }

  public static OfferData toOfferData(BannerOffer bannerOffer, BigDecimal payment, BannerSize bannerSize) {
    Banner banner = null;
    for (Banner b : bannerOffer.banners())
      if (b.size().equals(bannerSize))
        banner = b;
    return new OfferData(bannerOffer, payment, null, banner.imageBase64(), null);
  }

  public static OfferData toOfferData(RegularOffer regularOffer, BigDecimal payment) {
    return new OfferData(regularOffer, payment, regularOffer.description(), regularOffer.imageBase64(), null);
  }

  public static OfferData toOfferData(VideoOffer videoOffer, BigDecimal payment) {
    return new OfferData(videoOffer, payment, null, null, videoOffer.videoUrl());
  }
}
