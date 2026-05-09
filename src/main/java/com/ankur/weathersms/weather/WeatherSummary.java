package com.ankur.weathersms.weather;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class WeatherSummary {
  private WeatherSummary() {}

  public static String summarizeToday(JsonNode oneCallJson, ZoneId zoneId, String locationLabel) {
    JsonNode daily0 = oneCallJson.path("daily").path(0);
    if (daily0.isMissingNode()) {
      throw new IllegalArgumentException("OpenWeather One Call missing daily[0]");
    }

    long dt = daily0.path("dt").asLong(0);
    String date = dt == 0
        ? "Today"
        : DateTimeFormatter.ofPattern("EEE dd MMM").withZone(zoneId).format(Instant.ofEpochSecond(dt));

    String desc = daily0.path("weather").path(0).path("description").asText("Forecast");
    desc = capitalize(desc);

    int max = (int) Math.round(daily0.path("temp").path("max").asDouble(Double.NaN));
    int min = (int) Math.round(daily0.path("temp").path("min").asDouble(Double.NaN));
    int humidity = daily0.path("humidity").asInt(-1);
    int pop = (int) Math.round(daily0.path("pop").asDouble(0.0) * 100.0);

    // wind_speed in m/s
    int windKmh = (int) Math.round(daily0.path("wind_speed").asDouble(0.0) * 3.6);

    String loc = (locationLabel == null || locationLabel.isBlank()) ? "" : (" in " + locationLabel);

    StringBuilder sb = new StringBuilder();
    sb.append(date).append(loc).append(": ").append(desc).append(". ");

    if (!Double.isNaN(max) && !Double.isNaN(min)) {
      sb.append(max).append("°C/").append(min).append("°C");
    } else {
      sb.append("Temps unavailable");
    }

    if (humidity >= 0) sb.append(", humidity ").append(humidity).append("%");
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

  private static String capitalize(String s) {
    if (s == null) return "";
    String t = s.trim();
    if (t.isEmpty()) return t;
    return Character.toUpperCase(t.charAt(0)) + t.substring(1);
  }
}

