package com.heymoose.resource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.heymoose.domain.affiliate.ErrorInfo;
import com.heymoose.domain.affiliate.ErrorInfoRepository;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlErrorsInfo;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

public final class ErrorInfoResourceTest {


  private static class HashErrorRepository implements ErrorInfoRepository {
    private ListMultimap<Long, ErrorInfo> map = ArrayListMultimap.create();

    public boolean put(ErrorInfo info) {
      return map.put(info.affiliateId(), info);
    }

    @Override
    public List<ErrorInfo> list(int offset, int limit, Long affiliateId,
                                DateTime from, DateTime to) {
      return map.get(affiliateId);
    }

    @Override
    public boolean track(Long affiliateId, String uri, DateTime date,
                         Throwable cause) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void callsRepositoryAndPassesGivenParamsOnList() throws Exception {
    int limit = 100;
    int offset = 0;
    Long affId = 1L;
    DateTime dateFrom = DateTime.now().minusDays(1);
    DateTime dateTo = DateTime.now();

    ErrorInfoRepository repository = mockRepo();
    expect(repository.list(offset, limit, affId, dateFrom, dateTo))
        .andReturn(ImmutableList.<ErrorInfo>of());
    replay(repository);


    ErrorInfoResource resource = new ErrorInfoResource(repository);
    resource.list(offset, limit, affId, dateFrom.getMillis(), dateTo.getMillis());

    verify(repository);
  }

  @Test
  public void returnsEmptyXmlOnNoData() throws Exception {
    ErrorInfoRepository repository = mockRepo();
    expect(repository.list(anyInt(), anyInt(), anyLong(),
        anyObject(DateTime.class), anyObject(DateTime.class)))
        .andReturn(ImmutableList.<ErrorInfo>of());
    replay(repository);

    ErrorInfoResource resource = new ErrorInfoResource(repository);
    XmlErrorsInfo info = resource.list(0, 1, 1L, null, null);

    assertTrue(info.list.isEmpty());
    assertEquals((Long) 0L, info.count);
    verify(repository);
  }


  @Test
  public void returnsDataAsXmlIfExists() throws Exception {
    Long affiliateId = 1L;
    ErrorInfo info = ErrorInfo.fromException(
        affiliateId,
        "http://url.com",
        DateTime.now(),
        new Exception("Message"));

    ErrorInfoRepository repository = repoWithError(info);

    ErrorInfoResource resource = new ErrorInfoResource(repository);
    XmlErrorsInfo result = resource.list(0, 1, affiliateId, null, null);

    XmlErrorsInfo expected = Mappers.toXmlErrorsInfo(ImmutableList.of(info));
    for (int i = 0; i < expected.list.size(); i++) {
      assertEquals(expected.list.get(i), result.list.get(i));
    }
  }

  private ErrorInfoRepository mockRepo() {
    return createMock(ErrorInfoRepository.class);
  }

  private ErrorInfoRepository repoWithError(ErrorInfo info) {
    HashErrorRepository repo = new HashErrorRepository();
    repo.put(info);
    return repo;
  }
}
