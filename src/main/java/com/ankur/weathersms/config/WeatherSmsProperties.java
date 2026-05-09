package com.ankur.weathersms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public record WeatherSmsProperties(
    String city,
    String countryCode,
    String timezone,
    int smsMaxLen,
    Telegram telegram
) {}


