## Java app monitoring with Kafka


## Prerequisites
  - Running Kafka instance. You can use [kafka-docker](https://github.com/wurstmeister/kafka-docker) as it's a good tutorial on how to set it up.
  - Clone this repository
```bash
$ git clone git@github.com:balamaci/blog-kafka-log-events-generator.git
```

The [logback.xml](https://github.com/balamaci/blog-kafka-log-events-generator/blob/master/src/main/resources/logback.xml) config is using the [logback-kafka-appender](https://github.com/danielwegener/logback-kafka-appender) to write log events to Kafka.  
We're using the [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) library to transform log events to json as explained in detail in the older [blog post](https://balamaci.ro/java-app-monitoring-with-elk-logstash/). 

## Code run for generating the events
Start the Kafka stack as explained in [kafka-docker](https://github.com/wurstmeister/kafka-docker).

**logback.xml** config is using an environment variable 'KAFKA_BOOTSTRAP_SERVERS' to specify the Kafka brokers where logback appender should be writing, so you need to find the IP of the Kafka brokers.(I just grepped for 9092, the default Kafka port 'netstat -pantu | grep 9092')
It's also using the **'logs'** topic for appending the json log events. You can choose how many partitions to use for the topic directly from the **docker-compose.yml**

```
KAFKA_CREATE_TOPICS: "logs:2:1,intermediateTopic:3:1"
```

To generate the events we just need to pass to maven the env variable with the ip and port extracted from above for ex:

```bash
mvn -DKAFKA_BOOTSTRAP_SERVERS=172.18.0.2:9092 compile exec:java
```

The number and type of events and is configured in the **[jobs.conf](https://github.com/balamaci/blog-kafka-log-events-generator/blob/master/src/main/resources/jobs.conf)** file:
 ```
events {
    number:100,
    threads:1,
    jobs:[
        {
            name:viewProduct
            probability:0.7
        },
        {
            name:captchaVerified
            probability:0.3
        }
    ]
}

captchaVerified {
   waitBeforeStart {
        fixed:500 ms
   }
   class : ro.fortsoft.kafka.testdata.generator.event.ecommerce.BrowserCaptchaVerified
   maxUniqueBrowsers:5
   maxProducts:10
}

viewProduct {
   waitBeforeStart {
        random {
            min:500 ms,
            max:1 seconds
        }
   }

   class : ro.fortsoft.kafka.testdata.generator.event.ecommerce.ViewProductEvent
   maxProducts:10
   maxUniqueBrowsers:5
}
```
you can create and add your own event by extending **[BaseEvent](https://github.com/balamaci/blog-kafka-log-events-generator/blob/master/src/main/java/ro/fortsoft/kafka/testdata/generator/event/base/BaseEvent.java)** and adding it to the list of jobs.


#### How the events are generated

The code that fires the events:

````java
ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentThreads);

/** From 0->numberOfEvents we produce an Event(extends Runnable) which
we submit to the Executor service **/
for(int i=0; i < numberOfEvents; i++) {
      BaseEvent randomEvent = eventBuilder.randomEvent(config);
      executorService.submit(randomEvent);
}

//since all the jobs have been submitted we notify the pool that it can shutdown
executorService.shutdown();

try {
      //wait for the submitted tasks to finish, but no more
      executorService.awaitTermination(5, TimeUnit.MINUTES);
} catch (InterruptedException ignored) {
} finally {
     //signal the async shipping to Logstash threads to terminate
     shutdownLogger();
}
```

**_Executors.newFixedThreadPool(numberOfThreads)_** method which creates an ExecutorService with a pool of threads, but also as parameter an unbounded(MAX_INT) - **LinkedBlockingQueue**-.
If we submit more jobs than there are free threads in the pool, the new jobs which are held "in store" until one of the worker threads is free to take a new job from the queue.

This means the ExecutorService can accept quickly all the submitted jobs. It's not blocking at any of the executorService.submit() call, since the **BlockingQueue** is unbounded).

Since all the jobs have been submitted quite fast, we notify the pool that it can shutdown so the Main thread can eventually exit
````java
executorService.shutdown();
````

but we need to wait for the jobs that were submitted and not yet processed - those stored in the **BlockingQueue**- to finish with a generous grace period
````java
executorService.awaitTermination(5, TimeUnit.MINUTES);
````

In the end, the **shutdownLogger** command is necessary to stop the async threads which are pushing the log events into Logstash and to close the connection

