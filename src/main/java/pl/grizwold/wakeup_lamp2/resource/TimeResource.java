package pl.grizwold.wakeup_lamp2.resource;

import lombok.extern.slf4j.Slf4j;
import org.rapidoid.lambda.OneParamLambda;
import org.rapidoid.setup.On;
import pl.grizwold.wakeup_lamp2.logic.TimeService;

import java.time.LocalTime;

@Slf4j
public class TimeResource {
    private final TimeService timeService;

    public TimeResource(TimeService timeService) {
        this.timeService = timeService;

        On.get("/time").json(this::getTime);
        On.put("/time").json((OneParamLambda<LocalTime, String>) this::setTime);
    }

    private LocalTime getTime() {
        log.info("Getting local time");
        return timeService.now();
    }

    private LocalTime setTime(String time) {
        log.info("Updating system time to {}", time);
        return timeService.setSystemTime(time);
    }
}
