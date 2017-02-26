package ro.fortsoft.elk.testdata.generator.event.ecommerce;

import com.typesafe.config.Config;
import ro.fortsoft.elk.testdata.generator.event.ecommerce.base.BaseECommerceEvent;

/**
 * @author sbalamaci
 */
public class ViewProductEvent extends BaseECommerceEvent {


    public ViewProductEvent(Config config) {
        super(config);
    }

    @Override
    public void doWork(long eventCount) {
    }
}
