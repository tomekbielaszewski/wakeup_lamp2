package pl.grizwold.wakeup_lamp2.resource;

import lombok.extern.slf4j.Slf4j;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.lambda.TwoParamLambda;
import org.rapidoid.setup.On;
import pl.grizwold.wakeup_lamp2.logic.RaspberryPi;

@Slf4j
public class RaspberryPiResource {
    private final RaspberryPi raspberryPi;

    public RaspberryPiResource(RaspberryPi raspberryPi) {
        this.raspberryPi = raspberryPi;

        On.get("/raspberry/{pwm}").json((TwoParamLambda<Resp, Integer, Req>) this::setPwm);
    }

    private Resp setPwm(Integer pwm, Req request) {
        log.info("Setting PWM to {}", pwm);
        this.raspberryPi.setPWM(pwm);
        return request.response().code(202);
    }
}
