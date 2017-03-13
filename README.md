#### Why can't we just use RxJava or Spring Reactor?
RxJava and Spring Reactor are great generic reactive streams processing frameworks. We surely can use them to process a Kafka stream, but dealing with large amounts of data in streams means we also need an external storage system, preferably offheap(to not be impacted by GC). 
We could store it in 
Probably we also want to be able to start the processing on multiple machines. We also want to be 
Kafka Streams already has the solution for these types of problems. This doesn't mean that maybe someone 

#### Streaming frameworks
There are many streaming frameworks that can work with Kafka streams. 
 be usecases where the amount of data. Combine that with the fact that maybe we want to 


### A few words about how Kafka works.
At it's base, Kafka has the distributed log concept. By log we understand an immutable(append only) data structure. 
https://kafka.apache.org/0102/images/log_consumer.png

So a **Producer**(or more) **append** entries(**never overwrite or delete existing data**) at the end of the data structure, while any number of  **Consumers**  can read from it at their own pace by each keeping track of the offset from where they want to read the next data and advance to the next record and so on.

![Topic with partitions]()http://cdn2.hubspot.net/hub/540072/file-3062873568-png/blog-files/logs-24.png)

A **Topic** is like a category "/orders", "/logins", a feed name to which **Producers** can write to.

But **why would Kafka be so fast if multiple users would have to synchronize to append after each other to the same Topic?**
Well sequential writes to the filesystem are fast, but a very big performance boost comes from the fact that **Topics can be split into multiple Partitions which can reside on different machines**. So multiple **Producers can write to different Partitions** of the same Topic. 
**Partitions can be distributed on different machines in a cluster** in order to achieve high performance with horizontal scalability. 


![Topic with partitions](https://sookocheff.com/post/kafka/kafka-in-a-nutshell/log-anatomy.png)

So multiple producers can write to different partitions(and maybe different machines) to achieve high throughput. 
Notice how in the image above Partition 1 seems to have fewer entries than the others. 
**Order is maintained only in a single partition**, producers write at their own pace so **order of events cannot be guaranteed across partitions**(even of the same topic). Luckily there is a way to implement **custom partitionioning Strategy** so the messages goes to the same partition based on the data. 
This means we can for example have all the events of a certain 'userId' to be sent to the same partition. Also a 'Round Robin' partition strategy can be chosen so that the data is evenly distributed across partitions.     

On the other hand multiple producers can write to the same topic partition, but there are no guarantees that messages will not be intermixed.**There is no locking concept** as a producer to be blocked 
waiting for the other producer to finish writing a batch of messages and messages sent to a topic partition will be appended to the commit log in the order they are sent.     

#### Kafka consistency and failover
Each node in the cluster is called a **Kafka Broker**.

Each partition is replicated across multiple Kafka broker nodes to tolerate node failures. 
One of a partition's replicas is chosen as leader, and **the leader handles all reads and writes of messages in that partition**. 
Writes are serialized by the leader and synchronously replicated to a configurable number of replicas(the number of replicas can be set on a topic-by-topic basis). 
On leader failure, one of the in-sync replicas is chosen as the new leader.

![Partition leader](https://sookocheff.com/post/kafka/kafka-in-a-nutshell/producing-to-partitions.png)

Another partition is leader on another broker. 
![Another partition is leader](https://sookocheff.com/post/kafka/kafka-in-a-nutshell/producing-to-second-partition.png)


More about failover and replication [here](https://kafka.apache.org/documentation.html#replication) 
or [here](https://www.confluent.io/blog/hands-free-kafka-replication-a-lesson-in-operational-simplicity/)

A message is considered "committed" when all in sync replicas for that partition have applied it to their log. 
**Only committed messages are ever given out to the consumer.**

So when creating a Topic we need to specify in how many partitions we want to split it and how many replicas we want.

```
$KAFKA_HOME/bin/kafka-topics.sh --create --topic userLogins --partitions 4 --zookeeper zkEndpoint --replication-factor 2
```

```

```

### Writing application log events to Kafka
To write data into Kafka it's pretty simple using the client api. A record consists of a **Key** and **Value**.
**Key** plays a role into assigning the partition(the default Kafka Producer hashes the key and sends the record always to the same
partition for the same hash). The **Key** can be null in this case a round-robin algorithm is used.     
 
```
  Properties props = new Properties();
  props.put("bootstrap.servers", "localhost:9092");

  Producer<String, String> producer = new KafkaProducer<>(props);
  for(int i = 0; i < 100; i++) {
      Future<RecordMetadata> responseFuture = producer.send(new ProducerRecord<String, String>("my-topic", "KEY-" + Integer.toString(i), "VALUE-" + Integer.toString(i)));     
  }
```
the writing is async as the _send()_ method returns a Future. We can do _future.get()_ to turn it into a blocking one where we wait for the response.
There is another _send()_ method which takes a callback to be invoked if the message was written to Kafka or if an exception was encountered: 
```
producer.send(new ProducerRecord<String, String>("my-topic", "KEY-" + Integer.toString(i), "VALUE-" + Integer.toString(i)), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                long partition = metadata.partition(); //the partition where the message was written
            }
        });
```
The response object RecordMetadata has the 'partition' where the record was written and the 'offset' of the message. 

For high throughput, the Kafka Producer allows to wait and buffer for multiple messages and send them as a batch with fewer network requests.
```
//batch.size in bytes of h, set it to 0 to disable batching altogether
props.put("batch.size", 16384);

//how much to wait for other records before flushing the batch for network sending
props.put("linger.ms", 5); 

//buffer.memory - the amount of memory
//important as if network sending cannot keep up(or if the browkers are down), it will block 
props.put("buffer.memory", 33554432); 

//we can control how much time it will block before sending throws a BufferExhaustedException
props.put("max.block.ms", 10); 

try {
    Future<RecordMetadata> responseFuture = producer.send(new ProducerRecord<String, String>("my-topic", ..));
} catch (BufferExhaustedException e) { ... }
```  

But following on the steps of the blog other entries on  application logs processing we're going to use simple json log entries submitted by a **Logback Appender** directly into Kafka.
Notice how with &lt;producerConfig&gt; we can pass the producer config we explained above.
```
<appender name="STASH" class="com.github.danielwegener.logback.kafka.KafkaAppender">

    <encoder class="com.github.danielwegener.logback.kafka.encoding.PatternLayoutKafkaMessageEncoder">
       <layout class="net.logstash.logback.layout.LoggingEventCompositeJsonLayout">
          <providers>
             <mdc/>
             <context/>
             <version/>

             <logLevel/>
             <loggerName/>

             <pattern>
                <pattern>
                  {
                    "appName": "testdata",
                    "appVersion": "1.0"
                  }
                 </pattern>
             </pattern>
             <threadName/>

             <message/>

             <logstashMarkers/>
             <arguments/>

             <stackTrace/>
          </providers>
       </layout>
    </encoder>

    <topic>logs</topic>
        
    
    <!-- delivery strategy that uses the producer.send() method with callback -->
    <deliveryStrategy class="com.github.danielwegener.logback.kafka.delivery.AsynchronousDeliveryStrategy" />
    <!-- synchronous strategy does a Future<>.get() from the 


    <!-- each <producerConfig> translates to regular kafka-client config (format: key=value) -->
    <!-- producer configs are documented here: https://kafka.apache.org/documentation.html#newproducerconfigs -->
    <!-- bootstrap.servers is the only mandatory producerConfig -->    
    <producerConfig>bootstrap.servers=${KAFKA_BOOTSTRAP_SERVERS}</producerConfig>
    
       
    <!-- RoundRobinKeyingStrategy just returns null for the record KEY, thus the 
    <keyingStrategy class="com.github.danielwegener.logback.kafka.keying.RoundRobinKeyingStrategy" />

</appender>
```
We can control explicitly that related events go to the same partition by providing consistent keys for the log events. 
Remember that the [default](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/clients/producer/internals/DefaultPartitioner.java) producer partitioner 
uses hash of the keys to choose the partitions, or a round-robin strategy if the key is null.
 
For example having the thread-name hashcode used as the key as it's already provided by the logback-kafka library:
```
public class ThreadNameKeyingStrategy implements KeyingStrategy<ILoggingEvent> {

    @Override
    public byte[] createKey(ILoggingEvent e) {
        return ByteBuffer.allocate(4).putInt(e.getThreadName().hashCode()).array();
    }
}
```  

A custom Partitioner can be set in the Producer properties if for some reason you don't want to base on the keys:
```
public class KafkaCustomPartionStrategy implements Partitioner {
            
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partitions = cluster.availablePartitionsForTopic("logs").size();
        String json = new String(valueBytes, StandardCharsets.UTF_8);
        ...
        return partitionNumber;
    }
}
//and set it on the Producer
<producerConfig>partitioner.class=ro.fortsoft.kafka.partitioner.KafkaCustomPartionStrategy</producerConfig>
```

### Consumer groups
Consuming Kafka messages is more interesting as we can start multiple instances of consumers. Now the problem arise on 
how the topic partitions are to be distributed and how the consumers can work in parallel and collaborate to consume messages, 
scale out or fail over. 
This is solved in Kafka by marking the consumers with a common _group identifier_ that they belong to the same **Consumer Group** while another _group identifier_ would mean another Consumer Group who want to process the messages in the same topic for some other usecase. 
For each consumer group, one of the brokers is selected as the group coordinator, with the job to assign partitions when new members arrive, or reassign partitions when old members depart or topic metadata changes.    
It's irrelevant if by Consumers we mean multiple instances of our consumer app residing on the same machine or on different machines.

![Consumer group](http://cdn2.hubspot.net/hubfs/540072/New_Consumer_figure_1.png)  

Kafka brokers keep tracks of the **offset**(position) of the consumed messages in a partition for each consumer group. **The messages in each partition log are then read sequentially**.
As the consumer makes progress, it **commits the offsets** of messages it has successfully processed. 
The **commit offset** message from the consumer can come later after a larger batch of messages is processed. 

![](http://cdn2.hubspot.net/hubfs/540072/New_Consumer_Figure_2.png)

Should the consumer fails before being able to send the **commit offset XXX** to the Kafka broker, on restart(or reallocation ), the new Consumer instance 
will continue from the last committed offset that the broker has, meaning it will reprocess some of the messages(this is 'at least once' behavior). 
For ex. if the 
      
    
For this to work though, it means that we can't have more that a single consumer(from the same group) allocated on the same partition.    
  

Because 

If the number of Consumer Group instances is more than the number of partitions, the excess nodes remain idle. This might be desirable to handle failover.  
If there are more partitions than Consumer Group instances, then some nodes will be reading more than one partition.   
   
The Kafka broker group coordinator will have to  


### Setting up a Kafka
We can use the docker image. A detailed guide how to run it is provided, so no need to   
