package ro.fortsoft.kafka.testdata.generator.event;

import com.typesafe.config.Config;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author sbalamaci
 */
public class LoginEvent extends BaseEvent {

    private static final Logger log = LoggerFactory.getLogger(LoginEvent.class);

    private static final String ipNetworkPattern = "192.168.0.";
    private static final int MIN_VALID_LOGIN_IP = 100;

    private int randomIp;

    public LoginEvent(Config config, String jobName) {
        super(config, jobName);
        randomIp = ThreadLocalRandom.current().nextInt(0, 192);
    }

    @Override
    public void doWork(long eventCount) {
        Marker ipMarker = Markers.append("remoteIP", ipNetworkPattern + randomIp);
        String username = randomUsername();

        if(randomIp < MIN_VALID_LOGIN_IP) {
            StructuredArgument userNameArgument = StructuredArguments.value("userName", username);
            log.info(ipMarker, "SUCCESS login for user='{}'", userNameArgument);
        } else {
            log.error(ipMarker, "FAILED login for user='{}'", username);
        }
    }

}
