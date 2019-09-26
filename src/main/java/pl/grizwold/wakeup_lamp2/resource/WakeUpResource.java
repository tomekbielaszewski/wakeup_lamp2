package pl.grizwold.wakeup_lamp2.resource;

import lombok.extern.slf4j.Slf4j;
import org.rapidoid.lambda.OneParamLambda;
import org.rapidoid.setup.On;
import pl.grizwold.wakeup_lamp2.logic.RaspberryPi;
import pl.grizwold.wakeup_lamp2.logic.WakeUpService;
import pl.grizwold.wakeup_lamp2.model.WakeUpWeek;

@Slf4j
public class WakeUpResource {
    private final WakeUpService wakeUpService;

    public WakeUpResource(WakeUpService wakeUpService) {
        this.wakeUpService = wakeUpService;

        On.get("/wakeup").json(this::getWakeUpWeek);
        On.put("/wakeup").json((OneParamLambda<WakeUpWeek, WakeUpWeek>) this::updateWakeUpWeek);
        On.put("/wakeup/default").json(this::setDefaultWakeUpWeek);
    }

    private WakeUpWeek getWakeUpWeek() {
        log.info("Getting current wake up week config");
        return wakeUpService.getWakeUpWeek();
    }

    private WakeUpWeek updateWakeUpWeek(WakeUpWeek wakeUpWeek) {
        log.info("Updating wake up config to {}", wakeUpWeek);
        return wakeUpService.updateWakeUpWeek(wakeUpWeek);
    }

    private WakeUpWeek setDefaultWakeUpWeek() {
        log.info("Setting wake up config to default");
        return wakeUpService.setDefaultWakeUpWeek();
    }
}
