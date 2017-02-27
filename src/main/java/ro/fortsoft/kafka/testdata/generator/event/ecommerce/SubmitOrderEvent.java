package ro.fortsoft.kafka.testdata.generator.event.ecommerce;

import com.typesafe.config.Config;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author sbalamaci
 */
public class SubmitOrderEvent extends BaseEvent {

    private static final Logger log = LoggerFactory.getLogger(SubmitOrderEvent.class);

    public SubmitOrderEvent(Config config, String jobName) {
        super(config, jobName);
    }

    @Override
    public void doWork(long eventCount) {
        MDC.put("username", randomUsername()); //we just show that it's easy to place as MDC variables
        MDC.put("store", randomStore().name); //and get the values in the log without explicit adding

        log.info("User submitted order with total amount={}",
                StructuredArguments.value("orderAmount", randomOrderValue()));

        MDC.remove("username");
        MDC.remove("store");
    }

    private int randomOrderValue() {
        return new Random().nextInt(1000);
    }

    private Store randomStore() {
        List<Pair<Store, Double>> stores = Arrays.stream(Store.values())
                .map(store -> new Pair<>(store, store.probability))
                .collect(Collectors.toList());
        return new EnumeratedDistribution<>(stores).sample();
    }

    private enum Store {
        CLOTHES("clothes.com", 0.4),
        ELECTRONICS("electronics.de", 0.4),
        COSMETICS("hairstyle.com", 0.2);


        Store(String name, Double probability) {
            this.name = name;
            this.probability = probability;
        }

        public String name;
        public Double probability;
    }

}
