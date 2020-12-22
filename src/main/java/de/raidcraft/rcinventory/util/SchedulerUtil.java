package de.raidcraft.rcinventory.util;

public class SchedulerUtil {

    public  final static int MS_PER_TICK = 50;

    public static int msInTicks(int ms) {

        return ms / MS_PER_TICK;
    }

    public static int minInMs(int min) {

        return min * 60 * 1000;
    }
}
