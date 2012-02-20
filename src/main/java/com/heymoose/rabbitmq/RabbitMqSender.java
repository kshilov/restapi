package com.heymoose.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RabbitMqSender {

  private final ConnectionFactory connectionFactory;

  @Inject
  public RabbitMqSender(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public void send(byte[] data, String exchange, String routingKey) {
    Connection connection = null;
    Channel channel = null;
    try {
      connection= connectionFactory.newConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(exchange, "direct", true, false, null);
      channel.basicPublish(exchange, routingKey, false, false, null, data);
    } catch (Exception e) {
      throw new RuntimeException("Failed to publish message", e);
    } finally {
      if (channel != null && channel.isOpen())
        try {channel.close(); } catch (Exception ignored) {}
      if (connection != null && connection.isOpen())
        try { connection.close(); } catch (Exception ignored) {}
    }
  }
}
