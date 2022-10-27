package info.ahaha.signala.schedule;

import java.util.function.Consumer;

public class Schedule {
    protected final long estimatedExecutionEpoch;
    protected final Consumer<Schedule> canceler;
    protected boolean executed = false, canceled = false;
    protected Runnable runnable;

    public Schedule(Runnable runnable, long estimatedExecutionEpoch, Consumer<Schedule> canceler) {
        this.runnable = runnable;
        this.estimatedExecutionEpoch = estimatedExecutionEpoch;
        this.canceler = canceler;
    }

    public void execute() {
        if (canceled)
            return;
        runnable.run();
        executed = true;
    }

    public boolean isExecutable() {
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

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public long getEstimatedExecutionEpoch() {
        return estimatedExecutionEpoch;
    }

    public void cancel() {
        this.canceled = true;
        canceler.accept(this);
    }
}
