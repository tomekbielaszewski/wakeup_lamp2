package pl.grizwold.wakeup_lamp2.logic;

import lombok.extern.slf4j.Slf4j;
import pl.grizwold.wakeup_lamp2.model.WakeUpDay;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.SECONDS;
import static pl.grizwold.wakeup_lamp2.logic.RaspberryPi.MAX_PWM_RATE;

@Slf4j
public class LampWorker {
    private final Map<LampState, Consumer<WakeUpDay>> stateHandlers;

    private final WakeUpService wakeUpService;
    private final RaspberryPi raspberryPi;
    private final TimeService time;

    public LampWorker(WakeUpService wakeUpService, RaspberryPi raspberryPi, TimeService time) {
        this.wakeUpService = wakeUpService;
        this.raspberryPi = raspberryPi;
        this.time = time;
        this.stateHandlers = new HashMap<>();
        this.stateHandlers.put(LampState.OFF, this::shutDown);
        this.stateHandlers.put(LampState.LIGHTNING_UP, this::lightUp);
        this.stateHandlers.put(LampState.ON, this::turnOn);
        this.stateHandlers.put(LampState.DIMMING, this::dim);
    }

    public void processLampState() {
        WakeUpDay todayWakeUp = wakeUpService.getTodayWakeUpDay();
        LampState state = getState(todayWakeUp);
        stateHandlers.get(state).accept(todayWakeUp);
    }

    private LampState getState(WakeUpDay wakeUpDay) {
        LampState state = LampState.OFF;
        LocalTime start = wakeUpDay.getStart();
        LocalTime end = wakeUpDay.getEnd();
        LocalTime dimStart = getDimStart(wakeUpDay);
        LocalTime dimEnd = getDimEnd(wakeUpDay);

        if (isBetween(start, end)) state = LampState.LIGHTNING_UP;
        if (isBetween(end, dimStart)) state = LampState.ON;
        if (isBetween(dimStart, dimEnd)) state = LampState.DIMMING;

        return state;
    }

    private void shutDown(WakeUpDay wakeUpDay) {
        raspberryPi.setPWM(0);
    }

    private void lightUp(WakeUpDay wakeUpDay) {
        LocalTime start = wakeUpDay.getStart();
        LocalTime end = wakeUpDay.getEnd();
        smoothlyLightUpLamp(start, end);
    }

    private void turnOn(WakeUpDay wakeUpDay) {
        raspberryPi.setPWM(MAX_PWM_RATE);
    }

    private void dim(WakeUpDay wakeUpDay) {
        LocalTime dimStart = getDimStart(wakeUpDay);
        LocalTime dimEnd = getDimEnd(wakeUpDay);
        smoothlyDimLamp(dimStart, dimEnd);
    }

    private void smoothlyLightUpLamp(LocalTime start, LocalTime end) {
        int pwmForThisSecond = calculateLightPower(start, end);

        log.info("Light power: {}/{}", pwmForThisSecond, MAX_PWM_RATE);
        raspberryPi.setPWM(pwmForThisSecond);
    }

    private void smoothlyDimLamp(LocalTime start, LocalTime end) {
        int pwmForThisSecond = MAX_PWM_RATE - calculateLightPower(start, end);

        log.info("Light power: {}/{}", pwmForThisSecond, MAX_PWM_RATE);
        raspberryPi.setPWM(pwmForThisSecond);
    }

    private int calculateLightPower(LocalTime start, LocalTime end) {
        long secondsInTotal = SECONDS.between(start, end);
        long secondsElapsed = SECONDS.between(start, time.now());

        return (int) (((float) secondsElapsed / (float) secondsInTotal) * MAX_PWM_RATE);
    }

    private LocalTime getDimEnd(WakeUpDay todayWakeUp) {
        return this.getDimStart(todayWakeUp).plus(wakeUpService.getDimDuration());
    }

    private LocalTime getDimStart(WakeUpDay todayWakeUp) {
        return todayWakeUp.getEnd().plus(wakeUpService.getDimDelay());
    }

    private boolean isBetween(LocalTime start, LocalTime end) {
        LocalTime now = time.now();
        return now.compareTo(start) == 0 ||
                (now.isAfter(start) &&
                        now.isBefore(end));
    }

    private enum LampState {
        OFF,
        LIGHTNING_UP,
        ON,
        DIMMING
    }
}
