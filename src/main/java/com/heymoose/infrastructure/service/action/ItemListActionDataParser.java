package com.heymoose.infrastructure.service.action;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ItemListActionData;

import java.io.InputStream;
import java.util.List;

public interface ItemListActionDataParser {

  List<ItemListActionData> parse(InputSupplier<InputStream> input);
}
