package com.heymoose.resource;

import com.heymoose.domain.OfferShowRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("shows")
@Singleton
public class OfferShowResource {
  
  private final OfferShowRepository shows;
  
  @Inject
  public OfferShowResource(OfferShowRepository shows) {
    this.shows = shows;
  }
}
