package info.ahaha.signala.schedule;

public class Schedule {
    protected boolean executed = false, canceled = false;
    protected final Runnable runnable;
    protected final long estimatedExecutionEpoch;

    public Schedule(Runnable runnable, long estimatedExecutionEpoch) {
        this.runnable = runnable;
        this.estimatedExecutionEpoch = estimatedExecutionEpoch;
    }

    public void execute(){
        runnable.run();
    }

    public boolean isExecutable(){
        return !executed && !canceled;
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public long getEstimatedExecutionEpoch() {
        return estimatedExecutionEpoch;
    }
}
