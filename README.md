# vp-kuljetus-messaging-service-api

### Usage guide:

Sample configuration:
```
amqp-host=localhost
amqp-port=5672
amqp-username=guest
amqp-password=guest

mp.messaging.outgoing.vp-out.connector=smallrye-rabbitmq        # name of connector, do not change
mp.messaging.outgoing.vp-out.exchange.name=common-exchange

mp.messaging.incoming.vp-in.connector=smallrye-rabbitmq         # name of connector, do not change
mp.messaging.incoming.vp-in.queue.name=app1-queue               # unique incming queue name for each app
mp.messaging.incoming.vp-in.queue.x-queue-type=quorum
mp.messaging.incoming.vp-in.exchange.name=common-exchange
mp.messaging.incoming.vp-in.routing-keys=EVENT_TYPE1,EVENT_TYPE2  # routing keys for each event type that is accepted by this queue, default is #
```

#### Error handling:
 - by default refused and failed messages are removed from queue
 - DLX(dead letter exchange) can be set up on RabbitMQ server and clients can be pointed to it to handle refused and failed messages __not included in sameple config__
 - For additional settings:
   - If failing message should be re-queued use:
        ```
      event.nack(Throwable("Failed to process the messave"), Metadata.of(RabbitMQRejectMetadata(true)))
       ```
    - To avoid constant re-sending unprocessable message delivery-limit policy can be set at RabbitMQ server (receiving messages
will get the x-delivery-limit header). After the delivery limit is reached the message will be moved to DLX.
