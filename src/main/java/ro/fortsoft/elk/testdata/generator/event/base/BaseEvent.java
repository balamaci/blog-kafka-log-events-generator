package ro.fortsoft.elk.testdata.generator.event.base;

import com.typesafe.config.Config;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.slf4j.MDC;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sbalamaci
 */
public abstract class BaseEvent implements Runnable {

    private int minWaitMs = 500;
    private int maxWaitMs = 2500;

    private static AtomicLong eventCounter = new AtomicLong(0);

    protected Config config;

    public BaseEvent(Config config) {
        this.config = config;
    }

    public abstract void doWork(long eventCount);

    @Override
    public void run() {
        waitBeforeStart();

        long eventId = eventCounter.incrementAndGet();
        try {
            MDC.put("eventId", String.valueOf(eventId));
            doWork(eventId);
        } finally {
            MDC.remove("eventId");
        }
    }

    private void waitBeforeStart() {
        int waitMs = randomInt(minWaitMs, maxWaitMs);
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException ignored) {  }
    }

    protected String randomUsername() {
        Fairy fairy = Fairy.create();
        Person person = fairy.person();
        return person.email();
    }

    protected int randomInt(int minVal, int maxVal) {
        return ThreadLocalRandom.current().nextInt(minVal, maxVal);
    }

    public void setMinWaitMs(int minWaitMs) {
        this.minWaitMs = minWaitMs;
    }

    public void setMaxWaitMs(int maxWaitMs) {
        this.maxWaitMs = maxWaitMs;
    }
}
