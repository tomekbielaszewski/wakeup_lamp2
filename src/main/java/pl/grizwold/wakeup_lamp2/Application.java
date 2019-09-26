package pl.grizwold.wakeup_lamp2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import pl.grizwold.wakeup_lamp2.logic.LampWorker;
import pl.grizwold.wakeup_lamp2.logic.RaspberryPi;
import pl.grizwold.wakeup_lamp2.logic.TimeService;
import pl.grizwold.wakeup_lamp2.logic.WakeUpService;
import pl.grizwold.wakeup_lamp2.model.WakeUpDay;
import pl.grizwold.wakeup_lamp2.model.WakeUpWeek;
import pl.grizwold.wakeup_lamp2.resource.RaspberryPiResource;
import pl.grizwold.wakeup_lamp2.resource.TimeResource;
import pl.grizwold.wakeup_lamp2.resource.WakeUpResource;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    public static final WakeUpWeek DEFAULT_WAKEUP = WakeUpWeek.builder()
            .weekend(WakeUpDay.builder()
                    .start(LocalTime.of(23, 0))
                    .end(LocalTime.of(23, 35))
                    .build())
            .workDay(WakeUpDay.builder()
                    .start(LocalTime.of(6, 0))
                    .end(LocalTime.of(7, 0))
                    .build())
            .dimDelay(Duration.ofMinutes(30))
            .dimDuration(Duration.ofMinutes(5))
            .build();
    private static final int BLINK_REPEATS = 3;

    private final RaspberryPi raspberryPi = new RaspberryPi();
    private final TimeService timeService = new TimeService();
    private final WakeUpService wakeUpService = new WakeUpService();
    private final LampWorker lampWorker = new LampWorker(wakeUpService, raspberryPi, timeService);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Application() {
    }

    public static void main(String[] args) {
        new Application().run(args);
    }

    private void run(String[] args) {
        welcomeBlink();
        startLampWorker();

        App.run(args);

        My.objectMapper()
                .registerModule(new JavaTimeModule());

        new TimeResource(timeService);
        new WakeUpResource(wakeUpService);
        new RaspberryPiResource(raspberryPi);
    }

    private void welcomeBlink() {
        for (int j = 0; j <= BLINK_REPEATS; j++) {
            for (int i = 0; i <= RaspberryPi.MAX_PWM_RATE; i++) {
                raspberryPi.setPWM(i);
                sleep();
            }
            for (int i = RaspberryPi.MAX_PWM_RATE; i >= 0; i--) {
                raspberryPi.setPWM(i);
                sleep();
            }
        }
    }

    @SneakyThrows
    private void sleep() {
        Thread.sleep(1);
    }

    private void startLampWorker() {
        executor.scheduleAtFixedRate(lampWorker::processLampState, 1, 1, TimeUnit.SECONDS);
    }
}
