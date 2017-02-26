package ro.fortsoft.elk.testdata.generator.event.ecommerce.base;

import com.typesafe.config.Config;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;
import ro.fortsoft.elk.testdata.generator.event.base.BaseEvent;

/**
 * @author sbalamaci
 */
public abstract class BaseECommerceEvent extends BaseEvent {

    protected static final String HASH_START_PARTICLE = "XXXY";

    private int maxUniqueBrowsers;

    public BaseECommerceEvent(Config config) {
        super(config);
        this.maxUniqueBrowsers = config.getInt("maxUniqueBrowsers");
    }

    protected String randomBrowserHash() {
        return HASH_START_PARTICLE + maxUniqueBrowsers;
    }

    protected Marker randomBrowserHashMarker() {
        return Markers.append("browserHash", HASH_START_PARTICLE + randomBrowserHash());
    }

}
