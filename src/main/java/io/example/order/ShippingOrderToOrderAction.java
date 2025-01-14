package io.example.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.LogEvent;
import io.example.shipping.ShippingOrderEntity;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

@Subscribe.EventSourcedEntity(value = ShippingOrderEntity.class, ignoreUnknown = true)
public class ShippingOrderToOrderAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(ShippingOrderToOrderAction.class);
  private final ComponentClient componentClient;

  public ShippingOrderToOrderAction(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  public Effect<String> on(ShippingOrderEntity.ReadyToShipOrderItemEvent event) {
    log.info("Event: {}", event);
    return callFor(event);
  }

  public Effect<String> on(ShippingOrderEntity.ReadyToShipOrderEvent event) {
    log.info("Event: {}", event);
    LogEvent.log("ShippingOrder", event.orderId(), "Order", event.orderId(), "color green");
    return callFor(event);
  }

  public Effect<String> on(ShippingOrderEntity.ReleasedOrderItemEvent event) {
    log.info("Event: {}", event);
    return callFor(event);
  }

  public Effect<String> on(ShippingOrderEntity.ReleasedOrderEvent event) {
    log.info("Event: {}", event);
    LogEvent.log("ShippingOrder", event.orderId(), "Order", event.orderId(), "color yellow");
    return callFor(event);
  }

  public Effect<String> on(ShippingOrderEntity.BackOrderedOrderItemEvent event) {
    log.info("Event: {}", event);
    return callFor(event);
  }

  public Effect<String> on(ShippingOrderEntity.BackOrderedOrderEvent event) {
    log.info("Event: {}", event);
    LogEvent.log("ShippingOrder", event.orderId(), "Order", event.orderId(), "color red");
    return callFor(event);
  }

  private Effect<String> callFor(ShippingOrderEntity.ReadyToShipOrderItemEvent event) {
    var command = new OrderEntity.ReadyToShipOrderItemCommand(event.orderId(), event.skuId(), event.readyToShipAt());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::shipOrderSku)
            .params(command));
  }

  private Effect<String> callFor(ShippingOrderEntity.ReadyToShipOrderEvent event) {
    var command = new OrderEntity.ReadyToShipOrderCommand(event.orderId(), event.readyToShipAt());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::shipOrder)
            .params(command));
  }

  private Effect<String> callFor(ShippingOrderEntity.ReleasedOrderItemEvent event) {
    var command = new OrderEntity.ReleaseOrderItemCommand(event.orderId(), event.skuId());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::releaseOrderSku)
            .params(command));
  }

  private Effect<String> callFor(ShippingOrderEntity.ReleasedOrderEvent event) {
    var command = new OrderEntity.ReleaseOrderCommand(event.orderId());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::releaseOrder)
            .params(command));
  }

  private Effect<String> callFor(ShippingOrderEntity.BackOrderedOrderItemEvent event) {
    var command = new OrderEntity.BackOrderOrderItemCommand(event.orderId(), event.skuId(), event.backOrderedAt());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::backOrderSku)
            .params(command));
  }

  private Effect<String> callFor(ShippingOrderEntity.BackOrderedOrderEvent event) {
    var command = new OrderEntity.BackOrderOrderCommand(event.orderId(), event.backOrderedAt());
    return effects().forward(
        componentClient.forEventSourcedEntity(event.orderId())
            .call(OrderEntity::backOrder)
            .params(command));
  }
}
