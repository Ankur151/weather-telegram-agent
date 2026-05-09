package com.ankur.weathersms.weather;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class OpenMeteoSummary {
  private OpenMeteoSummary() {}

  public record TodayForecast(
      String dateLabel,
      String description,
      double tempMaxC,
      double tempMinC,
      int rainChancePct,
      int windKmh
  ) {}

  public static TodayForecast extractToday(JsonNode forecastJson, ZoneId zoneId) {
    // Daily arrays have one element since forecast_days=1
    JsonNode daily = forecastJson.path("daily");

    String dateStr = daily.path("time").path(0).asText("");
    String dateLabel;
    try {
      LocalDate d = LocalDate.parse(dateStr);
      dateLabel = d.format(DateTimeFormatter.ofPattern("EEE dd MMM"));
    } catch (Exception e) {
      dateLabel = "Today";
    }

    double tMax = daily.path("temperature_2m_max").path(0).asDouble(Double.NaN);
    double tMin = daily.path("temperature_2m_min").path(0).asDouble(Double.NaN);
    int pop = (int) Math.round(daily.path("precipitation_probability_max").path(0).asDouble(0.0));

    // Current weather provides windspeed (km/h) and weathercode
    JsonNode current = forecastJson.path("current_weather");
    int windKmh = (int) Math.round(current.path("windspeed").asDouble(0.0));
    int code = current.path("weathercode").asInt(-1);
    String desc = weatherCodeToText(code);
    return new TodayForecast(dateLabel, desc, tMax, tMin, pop, windKmh);
  }

  public static String summarizeToday(TodayForecast t, String locationLabel) {
    String dateLabel = t.dateLabel();
    String desc = t.description();
    double tMax = t.tempMaxC();
    double tMin = t.tempMinC();
    int pop = t.rainChancePct();
    int windKmh = t.windKmh();

    String loc = (locationLabel == null || locationLabel.isBlank()) ? "" : (" in " + locationLabel);

    StringBuilder sb = new StringBuilder();
    sb.append(dateLabel).append(loc).append(": ").append(desc).append(". ");

    if (!Double.isNaN(tMax) && !Double.isNaN(tMin)) {
      sb.append(Math.round(tMax)).append("°C/").append(Math.round(tMin)).append("°C");
    } else {
      sb.append("Temps unavailable");
    }

    sb.append(", rain chance ").append(pop).append("%");
    sb.append(", wind ").append(windKmh).append(" km/h.");
    return sb.toString();
  }

  public static String trimToMaxLen(String s, int maxLen) {
    if (s == null) return "";
    if (maxLen <= 0) return s;
    if (s.length() <= maxLen) return s;
    if (maxLen <= 1) return s.substring(0, maxLen);
    return s.substring(0, Math.max(0, maxLen - 1)).trim() + "…";
  }

  // Basic mapping; keeps message short and understandable.
  static String weatherCodeToText(int code) {
    return switch (code) {
      case 0 -> "Clear sky";
      case 1, 2, 3 -> "Partly cloudy";
      case 45, 48 -> "Fog";
      case 51, 53, 55 -> "Drizzle";
      case 56, 57 -> "Freezing drizzle";
      case 61, 63, 65 -> "Rain";
      case 66, 67 -> "Freezing rain";
      case 71, 73, 75 -> "Snow";
      case 77 -> "Snow grains";
      case 80, 81, 82 -> "Rain showers";
      case 85, 86 -> "Snow showers";
      case 95 -> "Thunderstorm";
      case 96, 99 -> "Thunderstorm with hail";
      default -> "Forecast";
    };
  }
}

