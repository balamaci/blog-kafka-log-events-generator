package ro.fortsoft.kafka.testdata.generator.event.builder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import ro.fortsoft.kafka.testdata.generator.config.JobInfo;
import ro.fortsoft.kafka.testdata.generator.event.base.BaseEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Builds random events based on the probability defined in 'jobs.conf' file
 *
 * @author sbalamaci
 */
public class EventBuilder {

    private final EnumeratedDistribution<JobInfo> jobs;

    public EventBuilder(Config config) {
        List<Pair<JobInfo, Double>> jobsWithProb = config.getList("events.jobs").stream()
                .map(configValue -> jobConfigWithProbability(config, configValue))
                .collect(Collectors.toList());

        jobs = new EnumeratedDistribution<>(jobsWithProb);
    }

    public BaseEvent randomEvent(Config config) {
        JobInfo jobInfo = jobs.sample();
        return createEvent(jobInfo, config);
    }

    private BaseEvent createEvent(JobInfo jobInfo, Config config) {
        Class jobClass = jobInfo.getJobClass();
        try {
            return (BaseEvent) jobClass.getConstructor(Config.class, String.class)
                    .newInstance(config, jobInfo.getJobName());
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of BaseEvent " + jobClass.getName(), e);
        }
    }

    private Pair<JobInfo, Double> jobConfigWithProbability(Config config, ConfigValue configValue) {
        Map jobConf = (Map) configValue.unwrapped();
        String jobName = (String) jobConf.get("name");
        String jobClass = config.getString(jobName + ".class");
        Double jobProbability = (Double) jobConf.get("probability");

        try {
            Class<?> cls = Class.forName(jobClass);
            return new Pair<>(new JobInfo(cls, jobName), jobProbability);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Event class='" + jobClass + "'", e);
        }
    }
}
