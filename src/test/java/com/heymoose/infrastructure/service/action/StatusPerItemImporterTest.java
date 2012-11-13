package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.StatusPerItemActionData;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import com.heymoose.infrastructure.service.processing.Processor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public final class StatusPerItemImporterTest {

  public static class StatusPerItemImporter
      implements ActionDataImporter<StatusPerItemActionData> {

    private final Repo repo;
    private final Processor processor;

    public StatusPerItemImporter(Repo repo, Processor processor) {
      this.processor = processor;
      this.repo = repo;
    }

    @Override
    public void doImport(List<StatusPerItemActionData> actionList,
                         Long parentOfferId) {
      for (StatusPerItemActionData action : actionList) {
        doImport(action, parentOfferId);
      }
    }

    @Override
    public void doImport(StatusPerItemActionData action, Long parentOfferId) {

      Token token = repo.byHQL(Token.class,
          "from Token where value = ?",
          action.token());

      Offer offer = repo.get(Offer.class, parentOfferId);

      for (StatusPerItemActionData.ItemWithStatus item : action.itemList()) {
        ProcessableData data = new ProcessableData();
        Product product = repo.byHQL(Product.class,
            "from Product where originalId = ?", item.id());
        data.setProduct(product)
            .setPrice(item.price())
            .setToken(token)
            .setTransactionId(action.transactionId())
            .setOffer(offer);
        processor.process(data);
      }

    }
  }

  private static class MockRepo {
    private final Repo mock;

    public MockRepo() {
      this.mock = mock(Repo.class);
    }

    public <T extends IdEntity> MockRepo with(Class<? extends T> clz, T entity) {
      when(mock.get(eq(clz), eq(entity.id()))).thenReturn(entity);
      when(mock.byHQL(eq(clz), anyString(), any())).thenReturn(entity);
      return this;
    }

    public Repo repo() {
      return mock;
    }
  }

  @Test
  public void createsActionForItem() throws Exception {
    String transactionId = "transactionId";
    Token token = createToken();
    Offer offer = createOffer();
    Product product = createProduct(offer);
    BigDecimal price = new BigDecimal("10.10");
    Long offerId = 1L;
    MockRepo mockRepo = new MockRepo()
        .with(Token.class, token)
        .with(Offer.class, offer)
        .with(Product.class, product);

    Processor processor = mock(Processor.class);
    StatusPerItemImporter importer =
        new StatusPerItemImporter(mockRepo.repo(), processor);

    StatusPerItemActionData.ItemWithStatus item =
        new StatusPerItemActionData.ItemWithStatus(product.originalId());
    item.setStatus(ActionStatus.CREATED).setPrice(price);
    StatusPerItemActionData data = new StatusPerItemActionData();
    data.addItem(item)
        .setTransactionId(transactionId)
        .setToken(token.value());

    importer.doImport(data, offerId);

    ArgumentCaptor<ProcessableData> processableCapture =
        ArgumentCaptor.forClass(ProcessableData.class);
    verify(processor).process(processableCapture.capture());
    verifyNoMoreInteractions(processor);

    ProcessableData captured = processableCapture.getValue();

    assertEquals(transactionId, captured.transactionId());
    assertEquals(token, captured.token());
    assertEquals(price, captured.price());
    assertEquals(offer, captured.offer());
    assertEquals(product, captured.product());
  }

  private Product createProduct(Offer offer) {
    return new Product()
        .setOffer(offer)
        .setOriginalId("itemId")
        .setName("Fake product")
        .setId(1L);
  }

  private Offer createOffer() {
    Offer offer = new Offer();
    offer.setId(1L)
        .setName("Fake offer")
        .setTitle("Fake offer. Title");
    return offer;
  }

  private Token createToken() {
    return new Token(null).setId(1L);
  }
}
