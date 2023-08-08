package io.example.shipping;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.LogEvent;
import io.example.shipping.ShippingOrderItemEntity.CreatedShippingOrderItemEvent;
import io.example.shipping.ShippingOrderItemEntity.OrderSkuItem;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.spring.KalixClient;

@Subscribe.EventSourcedEntity(value = ShippingOrderItemEntity.class, ignoreUnknown = true)
public class ShippingOrderItemToOrderSkuItemAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(ShippingOrderItemToOrderSkuItemAction.class);
  private final KalixClient kalixClient;

  public ShippingOrderItemToOrderSkuItemAction(KalixClient kalixClient) {
    this.kalixClient = kalixClient;
  }

  public Effect<String> on(ShippingOrderItemEntity.CreatedShippingOrderItemEvent event) {
    log.info("Event: {}", event);

    var results = event.orderSkuItems().stream()
        .map(orderSkuItem -> toCommands(event, orderSkuItem))
        .map(command -> callFor(command))
        .toList();

    return effects().asyncReply(waitForCallsToFinish(results));
  }

  private OrderSkuItemEntity.CreateOrderSkuItemCommand toCommands(CreatedShippingOrderItemEvent event, OrderSkuItem orderSkuItem) {
    LogEvent.log("ShippingOrderItem", event.shippingOrderItemId().toEntityId(), "OrderSkuItem", orderSkuItem.orderSkuItemId().toEntityId(), "color yellow");
    return new OrderSkuItemEntity.CreateOrderSkuItemCommand(
        orderSkuItem.orderSkuItemId(),
        orderSkuItem.customerId(),
        orderSkuItem.skuId(),
        orderSkuItem.skuName(),
        orderSkuItem.orderedAt());
  }

  private CompletionStage<String> callFor(OrderSkuItemEntity.CreateOrderSkuItemCommand command) {
    var path = "/order-sku-item/%s/create".formatted(command.orderSkuItemId().toEntityId());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return deferredCall.execute();
  }

  private CompletableFuture<String> waitForCallsToFinish(List<CompletionStage<String>> results) {
    return CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(__ -> "OK");
  }
}