package ro.fortsoft.kafka.testdata.generator.event.ecommerce.base;

import com.typesafe.config.Config;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;

/**
 * @author sbalamaci
 */
public abstract class BaseProductEvent extends BaseECommerceEvent {

    private int maxProducts;

    public BaseProductEvent(Config config, String jobName) {
        super(config, jobName);
        this.maxProducts = config.getInt(jobName + ".maxProducts");
    }

    protected Integer randomProduct() {
        return randomInt(1, maxProducts);
    }

    protected StructuredArgument randomProductStructuredArg() {
        Integer randomProduct = randomProduct();
        return StructuredArguments.value("productId", randomProduct);
    }

}
