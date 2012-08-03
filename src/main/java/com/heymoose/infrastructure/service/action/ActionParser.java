package com.heymoose.infrastructure.service.action;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionData;

import java.io.InputStream;
import java.util.List;

public interface ActionParser {

  List<ActionData> parse(InputSupplier<InputStream> input);
}
