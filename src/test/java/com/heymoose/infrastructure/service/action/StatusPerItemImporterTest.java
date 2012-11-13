package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.action.StatusPerItemActionData;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.service.Tokens;
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

    private final Products products;
    private final Tokens tokens;
    private final OfferActions actions;
    private final Processor processor;
    private final OfferLoader offers;

    public StatusPerItemImporter(Tokens tokens,
                                 OfferLoader offers,
                                 Products products,
                                 OfferActions actions,
                                 Processor processor) {
      this.processor = processor;
      this.offers = offers;
      this.products = products;
      this.tokens = tokens;
      this.actions = actions;
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

      Token token = tokens.byValue(action.token());
      Offer offer = offers.offerById(parentOfferId);

      for (StatusPerItemActionData.ItemWithStatus item : action.itemList()) {
        Product product = products.byOriginalId(parentOfferId, item.id());
        List<OfferAction> actionList = actions.listProductActions(
            token,
            action.transactionId(),
            product);
        if (actionList.size() > 0) continue;
        ProcessableData data = new ProcessableData();
        data.setProduct(product)
            .setPrice(item.price())
            .setToken(token)
            .setTransactionId(action.transactionId())
            .setOffer(offer);
        processor.process(data);
      }

    }
  }

  @Test
  public void createsActionForItem() throws Exception {
    StatusPerItemActionData data = oneItemData();
    StatusPerItemActionData.ItemWithStatus item = data.itemList().get(0);

    Token token = createToken(data.token());
    Offer offer = createOffer();
    Product product = createProduct(item.id(), offer);

    Products products = productsWith(offer, product);
    OfferLoader offers = offersWith(offer);
    Tokens tokens = tokensWith(token);
    OfferActions actions = mock(OfferActions.class);

    Processor processor = mock(Processor.class);
    StatusPerItemImporter importer =
        new StatusPerItemImporter(
            tokens,
            offers,
            products,
            actions,
            processor);

    importer.doImport(data, offer.id());

    ArgumentCaptor<ProcessableData> processableCapture =
        ArgumentCaptor.forClass(ProcessableData.class);
    verify(processor).process(processableCapture.capture());
    verifyNoMoreInteractions(processor);

    ProcessableData captured = processableCapture.getValue();

    assertEquals(data.transactionId(), captured.transactionId());
    assertEquals(token, captured.token());
    assertEquals(item.price(), captured.price());
    assertEquals(offer, captured.offer());
    assertEquals(product, captured.product());
  }

  @Test
  public void doesNotProcessIfActionWasAlreadyImported() throws Exception {
    StatusPerItemActionData data = oneItemData();
    StatusPerItemActionData.ItemWithStatus item = data.itemList().get(0);

    Token token = createToken(data.token());
    Offer offer = createOffer();
    Product product = createProduct(item.id(), offer);

    Products products = productsWith(offer, product);
    OfferLoader offers = offersWith(offer);
    Tokens tokens = tokensWith(token);

    OfferAction action = new OfferAction()
        .setId(1L)
        .setToken(token)
        .setOffer(offer)
        .setProduct(product)
        .setTransactionId(data.transactionId());

    OfferActions actions = actionsWith(action);

    Processor processor = mock(Processor.class);
    StatusPerItemImporter importer =
        new StatusPerItemImporter(
            tokens,
            offers,
            products,
            actions,
            processor);

    importer.doImport(data, offer.id());
    verifyNoMoreInteractions(processor);
  }

  private StatusPerItemActionData oneItemData() {
    String itemId = "itemId";
    BigDecimal price = new BigDecimal("10.01");
    String transactionId = "transactionId";
    String tokenValue = "tokenValue";
    StatusPerItemActionData.ItemWithStatus item =
        new StatusPerItemActionData.ItemWithStatus(itemId);
    item.setStatus(ActionStatus.CREATED).setPrice(price);
    StatusPerItemActionData data = new StatusPerItemActionData();
    data.addItem(item)
        .setTransactionId(transactionId)
        .setToken(tokenValue);
    return data;
  }

  private Product createProduct(String id, Offer offer) {
    return new Product()
        .setOffer(offer)
        .setOriginalId(id)
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

  private Token createToken(String value) {
    return new Token(null).setId(1L).setValue(value);
  }

  private Tokens tokensWith(Token token) {
    Tokens mock = mock(Tokens.class);
    when(mock.byValue(token.value())).thenReturn(token);
    return mock;
  }

  private OfferLoader offersWith(Offer offer) {
    OfferLoader mock = mock(OfferLoader.class);
    when(mock.offerById(offer.id())).thenReturn(offer);
    return mock;
  }

  private Products productsWith(Offer offer, Product product) {
    Products mock = mock(Products.class);
    when(mock.byOriginalId(offer.id(), product.originalId()))
        .thenReturn(product);
    return mock;
  }

  private OfferActions actionsWith(OfferAction action) {
    OfferActions mock = mock(OfferActions.class);
    when(mock.listProductActions(
        action.token(),
        action.transactionId(),
        action.product())).thenReturn(ImmutableList.of(action));
    return mock;
  }


}
