package com.leave.management.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

@FunctionalInterface
interface TemperatureMeasurementCallback {
    public void temperatureMeasured(int temperature);
}
interface Thermometer {
    public int measure();
}

@Configuration
@Import({FakeThermometer.class, WeatherForecastService.class})
class Config {
//
    @Bean
    public TemperatureMeasurementCallback callback() {
        System.out.println("Registering TemperatureMeasurementCallback bean");
        return (temperature) -> System.out.println(temperature);
    }
}

@Component
class FakeThermometer implements Thermometer {

    private int currentTemperature = 21;

    @Override
    public int measure() { return currentTemperature++; }
}

@Service
class WeatherForecastService {

    @Autowired
    private Thermometer thermometer;
    @Autowired
    private TemperatureMeasurementCallback callback;

    @Scheduled(fixedRate = 50)
    public void takeTemperatureMeasurement() {
        int temperature = thermometer.measure();
        callback.temperatureMeasured(temperature);
    }
}