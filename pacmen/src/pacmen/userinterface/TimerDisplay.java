package pacmen.userinterface;

public class TimerDisplay {
    public static String formatElapsedTime(long elapsedMillis) {
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
