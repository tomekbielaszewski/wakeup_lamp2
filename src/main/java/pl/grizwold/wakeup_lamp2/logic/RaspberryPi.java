package pl.grizwold.wakeup_lamp2.logic;

import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.wiringpi.Gpio;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RaspberryPi {
    public static final int MAX_PWM_RATE = 1024;

    private final GpioController gpio;
    private final GpioPinPwmOutput pwm;

    public RaspberryPi() {
        gpio = GpioFactory.getInstance();
        Pin pin = CommandArgumentParser.getPin(
                RaspiPin.class,
                RaspiPin.GPIO_01);
        this.pwm = gpio.provisionPwmOutputPin(pin);
        Gpio.pwmSetMode(Gpio.PWM_MODE_BAL);
        this.pwm.setPwm(0);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public RaspberryPi setPWM(Integer pwm) {
        this.pwm.setPwm(pwm);
        return this;
    }

    public void shutdown() {
        pwm.setPwm(0);
        gpio.shutdown();
    }
}
