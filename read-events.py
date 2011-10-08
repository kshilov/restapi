from amqplib import client_0_8 as amqp

conn = amqp.Connection()
chan = conn.channel()

chan.queue_declare(queue="events", durable=True, exclusive=False, auto_delete=False)
chan.exchange_declare(exchange="events", type="fanout", durable=True, auto_delete=False)

chan.queue_bind(queue="events", exchange="events")

def recv_callback(msg):
    print 'Received: ' + msg.body

chan.basic_consume(queue='events', no_ack=True, callback=recv_callback)

while True:
    chan.wait()

