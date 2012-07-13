package com.heymoose.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.errorinfo.ErrorInfo;
import com.heymoose.domain.errorinfo.ErrorInfoRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlErrorsInfo;
import org.joda.time.DateTime;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("errors")
@Singleton
public class ErrorInfoResource {

  private final ErrorInfoRepository repository;

  @Inject
  public ErrorInfoResource(ErrorInfoRepository repository) {
    this.repository = repository;
  }

  @Transactional
  @GET
  public XmlErrorsInfo list(@QueryParam("offset") @DefaultValue("0") int offset,
                            @QueryParam("limit") @DefaultValue("20") int limit,
                            @QueryParam("from") @DefaultValue("0") Long start,
                            @QueryParam("to") Long end) {
    DateTime dateFrom = new DateTime(start);
    DateTime dateTo = new DateTime(end);
    List<ErrorInfo> result = repository
        .list(offset, limit, dateFrom, dateTo);
    Long count = repository.count(dateFrom ,dateTo);
    return Mappers.toXmlErrorsInfo(result, count);
  }
}
