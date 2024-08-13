# vp-kuljetus-messaging-service-api

### Usage guide:

Sample configuration:
```
amqp-host=localhost
amqp-port=5672
amqp-username=guest
amqp-password=guest

mp.messaging.outgoing.vp-out.connector=smallrye-rabbitmq
mp.messaging.outgoing.vp-out.exchange.name=common-exchange

mp.messaging.incoming.vp-in.connector=smallrye-rabbitmq
mp.messaging.incoming.vp-in.queue.name=app1-queue
mp.messaging.incoming.vp-in.queue.x-queue-type=quorum
mp.messaging.incoming.vp-in.exchange.name=common-exchange

vp.senderid=app1
```
Unique **vp.senderid** string property to identify itself.

Unique queue name **mp.messaging.incoming.vp-in.queue.name=app1-queue**

### RabbitMq setup:
- Bindings and routing rules should be set up on RabbitMQ server
- Each queue should be bound to the exchange with routing key equal to the routing keys it wants to receive

#### Error handling:
 - by default refused and failed messages are removed from queue.
 - DLX(dead letter exchange) can be set up on RabbitMQ server to handle refused and failed messages.
   - If failing message should be requeued use:

      event.nack(Throwable("Failed to process the messave"), Metadata.of(RabbitMQRejectMetadata( true)))
    
    - To avoid constant re-sending unprocessable message delivery-limit policy can be set at RabbitMQ server (receiving messages
will get the x-delivery-limit header). After the delivery limit is reached the message will be moved to DLX.
