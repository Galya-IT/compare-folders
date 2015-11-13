package io.galya.files.model;

import java.util.Date;

/**
 * Created by Galya on 13/11/15.
 */
public class Timer {
    private long checkPoint1;
    private long checkPoint2;
    private boolean isRunning;

    public enum UNIT {
        MILLISECONDS,
        SECONDS
    }

    public Timer() {

    }

    public void start() {
        checkPoint1 = new Date().getTime();
    }

    public void stop() {
        checkPoint2 = new Date().getTime();
    }

    public long getInterval(UNIT unit) throws CompareFoldersAppException {
        if (isRunning) {
            throw new CompareFoldersAppException("Timer is still running!");
        }
        long resMilliseconds = checkPoint2 - checkPoint1;
        long result;
        if (unit == UNIT.SECONDS) {
            result = resMilliseconds / 1000;
        } else {
            result = resMilliseconds;
        }
        return result;
    }

}
