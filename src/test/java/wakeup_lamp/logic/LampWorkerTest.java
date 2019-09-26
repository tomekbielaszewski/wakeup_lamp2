package wakeup_lamp.logic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pl.grizwold.wakeup_lamp2.logic.LampWorker;
import pl.grizwold.wakeup_lamp2.logic.RaspberryPi;
import pl.grizwold.wakeup_lamp2.logic.TimeService;
import pl.grizwold.wakeup_lamp2.logic.WakeUpService;
import pl.grizwold.wakeup_lamp2.model.WakeUpDay;

import java.time.Duration;
import java.time.LocalTime;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static pl.grizwold.wakeup_lamp2.logic.RaspberryPi.MAX_PWM_RATE;
import static wakeup_lamp.matchers.IsCloseToInteger.closeTo;

public class LampWorkerTest {
    private WakeUpService wakeUpService = mock(WakeUpService.class);
    private RaspberryPi raspberryPi = mock(RaspberryPi.class);
    private TimeService time = mock(TimeService.class);

    private LampWorker lampWorker = new LampWorker(wakeUpService, raspberryPi, time);

    @Test
    public void lamp_is_shut_down_before_lightning_up() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(0, 59));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(0, 5)));
    }

    @Test
    public void sets_light_power_to_lowest_when_just_started() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(1, 1));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(0, 5)));
    }

    @Test
    public void sets_light_power_to_max_when_close_to_end_time() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(9, 59));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(MAX_PWM_RATE, 5)));
    }

    @Test
    public void keeps_max_value_during_dim_delay() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(10, 5));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(MAX_PWM_RATE, 5)));
    }

    @Test
    public void lamp_is_still_close_to_max_light_when_started_dimming() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(10, 10).plusSeconds(1));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(MAX_PWM_RATE, 5)));
    }

    @Test
    public void lamp_is_close_to_shut_down_at_the_end_of_dimming() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofMinutes(10));
        setDimDuration(Duration.ofMinutes(10));
        setCurrentTime(LocalTime.of(10, 20).minusSeconds(1));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(0, 5)));
    }

    @Test
    public void shuts_down_the_lamp_after_dimming() {
        setWakeUpDay(
                LocalTime.of(1, 0),
                LocalTime.of(10, 0)
        );
        setDimDelay(Duration.ofSeconds(10));
        setDimDuration(Duration.ofSeconds(10));
        setCurrentTime(LocalTime.of(10, 21));

        lampWorker.processLampState();

        Mockito.verify(raspberryPi).setPWM(argThat(closeTo(0, 5)));
    }

    private void setWakeUpDay(LocalTime start, LocalTime end) {
        Mockito.when(wakeUpService.getTodayWakeUpDay()).thenReturn(WakeUpDay.builder()
                .start(start)
                .end(end)
                .build());
    }

    private void setDimDelay(Duration value) {
        Mockito.when(wakeUpService.getDimDelay()).thenReturn(value);
    }

    private void setDimDuration(Duration value) {
        Mockito.when(wakeUpService.getDimDuration()).thenReturn(value);
    }

    private void setCurrentTime(LocalTime now) {
        Mockito.when(time.now()).thenReturn(now);
    }
}
