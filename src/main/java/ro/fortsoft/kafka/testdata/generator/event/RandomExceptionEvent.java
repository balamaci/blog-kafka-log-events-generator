package ro.fortsoft.kafka.testdata.generator.event;

import com.typesafe.config.Config;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Serban Balamaci
 */
public class RandomExceptionEvent extends BaseEvent {

    private static final Logger log = LoggerFactory.getLogger(RandomExceptionEvent.class);

    public RandomExceptionEvent(Config config, String jobName) {
        super(config, jobName);
    }


    @Override
    public void doWork(long eventCount) {
        try {
            randomException();
        } catch (Exception e) {
            log.error("Uncaught exception", e);
        }
    }

    private void randomException() throws Exception {
        List<Pair<Exception, Double>> stores = Arrays.stream(Exceptions.values())
                .map(ex -> new Pair<>(ex.get(), ex.probability))
                .collect(Collectors.toList());
        throw (new EnumeratedDistribution<>(stores)).sample();
    }

    private enum Exceptions {
        NULL_POINTER(() -> new NullPointerException("Generated Exception"), 0.4),
        ILLEGAL_ARGUMENT(() -> new IllegalAccessException("Some Runtime Exception"), 0.4),
        OUT_OF_BOUNDS(() -> new ArrayIndexOutOfBoundsException("We went too far"), 0.2);


        Exceptions(Supplier<Exception> exceptionGenerator, Double probability) {
            this.exceptionGenerator = exceptionGenerator;
            this.probability = probability;
        }

        public Supplier<Exception> exceptionGenerator;
        public Double probability;

        public Exception get() {
            return exceptionGenerator.get();
        }
    }

}
