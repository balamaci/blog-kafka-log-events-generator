package ro.fortsoft.elk.testdata.generator.event.ecommerce;

import com.typesafe.config.Config;
import ro.fortsoft.elk.testdata.generator.event.base.BaseEvent;

/**
 * @author sbalamaci
 */
public class CreateUserEvent extends BaseEvent {

    public CreateUserEvent(Config config) {
        super(config);
    }

    @Override
    public void doWork(long eventCount) {

    }
}
