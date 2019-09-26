package pl.grizwold.wakeup_lamp2.logic;

import lombok.extern.slf4j.Slf4j;
import pl.grizwold.wakeup_lamp2.model.WakeUpDay;
import pl.grizwold.wakeup_lamp2.model.WakeUpWeek;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static pl.grizwold.wakeup_lamp2.Application.DEFAULT_WAKEUP;

@Slf4j
public class WakeUpService {
    private WakeUpWeek wakeUpWeek;

    public WakeUpWeek getWakeUpWeek() {
        return Optional.ofNullable(wakeUpWeek)
                .orElse(DEFAULT_WAKEUP);
    }

    public WakeUpWeek updateWakeUpWeek(WakeUpWeek wakeUpWeek) {
        validate(wakeUpWeek);
        return this.wakeUpWeek = wakeUpWeek;
    }

    public WakeUpWeek setDefaultWakeUpWeek() {
        return this.wakeUpWeek = DEFAULT_WAKEUP;
    }

    public WakeUpDay getTodayWakeUpDay() {
        WakeUpWeek wakeUpWeek = this.getWakeUpWeek();
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        boolean isWeekend = DayOfWeek.SATURDAY.equals(dayOfWeek) || DayOfWeek.SUNDAY.equals(dayOfWeek);
        return isWeekend ? wakeUpWeek.getWeekend() : wakeUpWeek.getWorkDay();
    }

    public Duration getDimDelay() {
        return this.getWakeUpWeek().getDimDelay();
    }

    public Duration getDimDuration() {
        return this.getWakeUpWeek().getDimDuration();
    }

    private void validate(WakeUpWeek wakeUpWeek) {
        validate(wakeUpWeek.getWorkDay());
        validate(wakeUpWeek.getWeekend());

        assertTrue(!wakeUpWeek.getDimDelay().isNegative(), "Dim delay cannot be negative!");
        assertTrue(!wakeUpWeek.getDimDuration().isNegative(), "Dim duration cannot be negative!");
    }

    private void validate(WakeUpDay wakeUpDay) {
        assertTrue(wakeUpDay.getStart().isBefore(wakeUpDay.getEnd()), "Wakeup end cannot be before or equal to wakeup start");
    }

    private void assertTrue(boolean assertionResult, String assertionFailureMessage) {
        if (!assertionResult) throw new IllegalArgumentException(assertionFailureMessage);
    }
}
