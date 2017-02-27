package ro.fortsoft.kafka.testdata.generator.config;

/**
 * Hold
 *
 * @author sbalamaci
 */
public class JobInfo {

    private String jobName;
    private Class jobClass;

    public JobInfo(Class jobClass, String jobName) {
        this.jobClass = jobClass;
        this.jobName = jobName;
    }

    public Class getJobClass() {
        return jobClass;
    }

    public String getJobName() {
        return jobName;
    }
}
