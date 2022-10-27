package info.ahaha.signala.schedule;

import info.ahaha.signala.Connection;

public interface Scheduler {
    Schedule scheduling(Runnable runnable, long laterMilliSec);

    Schedule schedulingAsync(Runnable runnable, long laterMilliSec);


    Schedule scheduling(Connection connection, Runnable runnable, long laterMilliSec);

    Schedule schedulingAsync(Connection connection, Runnable runnable, long laterMilliSec);

    boolean cancelSchedules(Connection connection);
}
