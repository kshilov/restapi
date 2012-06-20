package com.heymoose.resource;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.affiliate.ErrorInfoRepository;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public final class ErrorInfoResourceTest {

  @Test
  public void callsRepositoryAndPassesGivenParamsOnList() throws Exception {
    ErrorInfoRepository repository = createMock(ErrorInfoRepository.class);
    ErrorInfoResource resource = new ErrorInfoResource(repository);

    int limit = 100;
    int offset = 0;
    Long affId = 1L;

    expect(repository.list(offset, limit, affId))
        .andReturn(ImmutableList.of());
    replay(repository);

    resource.list(offset, limit, affId);

    verify(repository);
  }
}
