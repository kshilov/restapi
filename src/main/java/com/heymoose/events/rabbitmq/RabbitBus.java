package com.heymoose.events.rabbitmq;

import com.heymoose.events.Event;
import com.heymoose.events.EventBus;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class RabbitBus implements EventBus {

  private final static Logger log = LoggerFactory.getLogger(RabbitBus.class);

  private final ConnectionFactory connectionFactory;
  private final String exchange;

  @Inject
  public RabbitBus(ConnectionFactory connectionFactory, @Named("event-bus") String exchange) {
    this.connectionFactory = connectionFactory;
    this.exchange = exchange;
  }

  @Override
  public void publish(Event event) {
    try {
      Connection connection = connectionFactory.newConnection();
      Channel channel = connection.createChannel();
      ObjectNode json = event.toJson();
      ObjectMapper mapper = new ObjectMapper();
      byte[] body = mapper.writeValueAsString(json).getBytes("UTF-8");
      channel.exchangeDeclare(exchange, "fanout", true, false, null);
      channel.basicPublish(exchange, "", false, false, null, body);
      channel.close();
      connection.close();
    } catch (Exception e) {
      log.error("Failed to publish event", e);
    }
  }
}
