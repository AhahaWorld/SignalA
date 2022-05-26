package info.ahaha.signala.schedule;

public interface Scheduler {
    Schedule scheduling(Runnable runnable, long laterMilliSec);
    Schedule schedulingAsync(Runnable runnable, long laterMilliSec);
}
