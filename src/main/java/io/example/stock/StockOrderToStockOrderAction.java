package io.example.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.any.Any;

import kalix.javasdk.DeferredCall;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

@Subscribe.EventSourcedEntity(value = StockOrderEntity.class, ignoreUnknown = true)
public class StockOrderToStockOrderAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(StockOrderToStockOrderAction.class);
  private final ComponentClient componentClient;

  public StockOrderToStockOrderAction(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  public Effect<String> on(StockOrderEntity.CreatedStockOrderEvent event) {
    log.info("Event: {}", event);
    return effects().forward(callFor(event.stockOrderId()));
  }

  public Effect<String> on(StockOrderEntity.GeneratedStockSkuItemIdsEvent event) {
    log.info("Event: {}", event);
    return effects().forward(callFor(event.stockOrderId()));
  }

  private DeferredCall<Any, String> callFor(String stockOrderId) {
    var command = new StockOrderEntity.GenerateStockSkuItemIdsCommand(stockOrderId);

    return componentClient.forEventSourcedEntity(stockOrderId)
        .call(StockOrderEntity::generateStockSkuItemIds)
        .params(command);
  }
}
