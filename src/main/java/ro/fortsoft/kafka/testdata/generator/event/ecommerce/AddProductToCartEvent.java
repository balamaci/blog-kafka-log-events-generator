package ro.fortsoft.kafka.testdata.generator.event.ecommerce;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.kafka.testdata.generator.event.ecommerce.base.BaseProductEvent;

/**
 * @author sbalamaci
 */
public class AddProductToCartEvent extends BaseProductEvent {

    private static final Logger log = LoggerFactory.getLogger(AddProductToCartEvent.class);

    public AddProductToCartEvent(Config config, String jobName) {
        super(config, jobName);
    }

    @Override
    public void doWork(long eventCount) {
        log.info(randomBrowserHashMarker(), "ProductId={} Added to cart", randomProductStructuredArg());
    }
}
