package com.ankur.weathersms;

import com.ankur.weathersms.config.Telegram;
import com.ankur.weathersms.config.WeatherSmsProperties;
import com.ankur.weathersms.notify.TelegramNotifier;
import com.ankur.weathersms.weather.OpenMeteoClient;
import com.ankur.weathersms.weather.AdviceGenerator;
import com.ankur.weathersms.weather.OpenMeteoSummary;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.ZoneId;

@SpringBootApplication
@EnableConfigurationProperties(WeatherSmsProperties.class)
public class Main implements CommandLineRunner {
  private final WeatherSmsProperties props;

  public Main(WeatherSmsProperties props) {
    this.props = props;
  }

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    String city = props.city();
    String countryCode = props.countryCode();
    if (city == null || city.isBlank()) throw new IllegalArgumentException("Missing property: weather.city");
    if (countryCode == null || countryCode.isBlank()) throw new IllegalArgumentException("Missing property: weather.countryCode");

    Telegram tg = props.telegram();
    if (tg == null) throw new IllegalArgumentException("Missing property group: weather.telegram");
    if (tg.botToken() == null || tg.botToken().isBlank()) throw new IllegalArgumentException("Missing property: weather.telegram.botToken");
    if (tg.chatId() == null || tg.chatId().isBlank()) throw new IllegalArgumentException("Missing property: weather.telegram.chatId");

    ZoneId zoneId = (props.timezone() == null || props.timezone().isBlank())
        ? ZoneId.of("Asia/Kolkata")
        : ZoneId.of(props.timezone().trim());
    int maxLen = props.smsMaxLen() <= 0 ? 240 : props.smsMaxLen();

    OpenMeteoClient meteo = new OpenMeteoClient();
    OpenMeteoClient.LatLon ll = meteo.geocodeCity(city, countryCode);
    JsonNode forecast = meteo.fetchForecast(ll.lat(), ll.lon(), zoneId.getId());

    OpenMeteoSummary.TodayForecast today = OpenMeteoSummary.extractToday(forecast, zoneId);
    String summary = OpenMeteoSummary.summarizeToday(today, ll.resolvedName());
    String tips = AdviceGenerator.advice(today);
    if (!tips.isBlank()) summary = summary + " " + tips;
    summary = OpenMeteoSummary.trimToMaxLen(summary, maxLen);

    new TelegramNotifier(tg.botToken().trim()).sendMessage(tg.chatId().trim(), summary);
    System.out.println("Sent Telegram: " + summary);
  }
}

