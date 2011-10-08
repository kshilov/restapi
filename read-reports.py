from amqplib import client_0_8 as amqp

conn = amqp.Connection()
chan = conn.channel()

chan.queue_declare(queue="reports.notify", durable=True, exclusive=False, auto_delete=False)
chan.exchange_declare(exchange="reports", type="direct", durable=True, auto_delete=False)

chan.queue_bind(queue="reports.notify", exchange="reports", routing_key="notify")

def recv_callback(msg):
    print 'Received: ' + msg.body

chan.basic_consume(queue='reports.notify', no_ack=True, callback=recv_callback)

while True:
    chan.wait()

