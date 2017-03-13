package ro.fortsoft.kafka.testdata.generator.event.base;

import com.typesafe.config.Config;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.slf4j.MDC;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sbalamaci
 */
public abstract class BaseEvent implements Runnable {

    private static AtomicLong eventCounter = new AtomicLong(0);

    protected Config config;
    protected String jobName;

    public BaseEvent(Config config, String jobName) {
        this.config = config;
        this.jobName = jobName;
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
        boolean hasCustomWait = config.hasPath(jobName + ".waitBeforeStart");

        long waitMs = 0;
        if(hasCustomWait) {
            String fixedWaitKey = jobName + ".waitBeforeStart.fixed";
            boolean isFixedWait = config.hasPath(jobName + ".waitBeforeStart.fixed");
            if(isFixedWait) {
                waitMs = config.getDuration(fixedWaitKey, TimeUnit.MILLISECONDS);
            } else {
                int minWait = config.getInt(jobName + ".waitBeforeStart.random.min");
                int maxWait = config.getInt(jobName + ".waitBeforeStart.random.max");

                waitMs = randomInt(minWait, maxWait);
            }
        }

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

    protected int randomInt(int maxVal) {
        return ThreadLocalRandom.current().nextInt(maxVal);
    }

}
