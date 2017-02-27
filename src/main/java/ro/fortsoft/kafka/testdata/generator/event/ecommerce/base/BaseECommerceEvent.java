package ro.fortsoft.kafka.testdata.generator.event.ecommerce.base;

import com.typesafe.config.Config;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

/**
 * @author sbalamaci
 */
public abstract class BaseECommerceEvent extends BaseEvent {

    protected static final String HASH_START_PARTICLE = "XXXY";

    private int maxUniqueBrowsers;

    public BaseECommerceEvent(Config config, String jobName) {
        super(config, jobName);
        this.maxUniqueBrowsers = config.getInt(jobName + ".maxUniqueBrowsers");
    }

    protected String randomBrowserHash() {
        return HASH_START_PARTICLE + maxUniqueBrowsers;
    }

    protected Marker randomBrowserHashMarker() {
        return Markers.append("browserHash", HASH_START_PARTICLE + randomBrowserHash());
    }

}
