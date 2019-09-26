package pl.grizwold.wakeup_lamp2.logic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;

@Slf4j
public class TimeService {
    public LocalTime now() {
        return LocalTime.now();
    }

    @SneakyThrows
    public LocalTime setSystemTime(String time) {
        Runtime.getRuntime().exec(String.format("date -s '%s'", time));
        return now();
    }
}
