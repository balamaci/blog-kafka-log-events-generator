package ro.fortsoft.kafka.testdata.generator.event.ecommerce;

import com.typesafe.config.Config;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

/**
 * @author sbalamaci
 */
public class CreateUserEvent extends BaseEvent {

    public CreateUserEvent(Config config, String jobName) {
        super(config, jobName);
    }

    @Override
    public void doWork(long eventCount) {

    }
}
