package com.ankur.weathersms.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;

public final class OpenMeteoClient {
  private static final ObjectMapper OM = new ObjectMapper();

  private final OkHttpClient http;

  public OpenMeteoClient() {
    this.http = new OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(25))
        .build();
  }

  public record LatLon(double lat, double lon, String resolvedName) {}

  public LatLon geocodeCity(String city, String countryCode) throws IOException {
    HttpUrl url = HttpUrl.parse("https://geocoding-api.open-meteo.com/v1/search")
        .newBuilder()
        .addQueryParameter("name", city)
        .addQueryParameter("count", "5")
        .addQueryParameter("language", "en")
        .addQueryParameter("format", "json")
        .build();

    JsonNode json = getJson(url);
    JsonNode results = json.path("results");
    if (!results.isArray() || results.isEmpty()) {
      throw new IOException("Open-Meteo geocoding: no results for name=" + city);
    }

    // Prefer matching country_code if provided; else use first.
    JsonNode chosen = results.get(0);
    if (countryCode != null && !countryCode.isBlank()) {
      String cc = countryCode.trim().toUpperCase();
      for (JsonNode r : results) {
        if (cc.equalsIgnoreCase(r.path("country_code").asText(""))) {
          chosen = r;
          break;
        }
      }
    }

    double lat = chosen.path("latitude").asDouble(Double.NaN);
    double lon = chosen.path("longitude").asDouble(Double.NaN);
    if (Double.isNaN(lat) || Double.isNaN(lon)) {
      throw new IOException("Open-Meteo geocoding: missing latitude/longitude for " + city);
    }

    String name = chosen.path("name").asText(city);
    String admin1 = chosen.path("admin1").asText("");
    String resolved = admin1.isBlank() ? name : (name + ", " + admin1);
    return new LatLon(lat, lon, resolved);
  }

  public JsonNode fetchForecast(double lat, double lon, String timezone) throws IOException {
    HttpUrl.Builder b = HttpUrl.parse("https://api.open-meteo.com/v1/forecast")
        .newBuilder()
        .addQueryParameter("latitude", String.valueOf(lat))
        .addQueryParameter("longitude", String.valueOf(lon))
        .addQueryParameter("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_max")
        .addQueryParameter("current_weather", "true")
        .addQueryParameter("forecast_days", "1");

    if (timezone != null && !timezone.isBlank()) {
      b.addQueryParameter("timezone", timezone.trim());
    }

    return getJson(b.build());
  }

  private JsonNode getJson(HttpUrl url) throws IOException {
    Request req = new Request.Builder().url(url).get().build();
    try (Response res = http.newCall(req).execute()) {
      String body = res.body() == null ? "" : res.body().string();
      if (!res.isSuccessful()) {
        throw new IOException("Open-Meteo HTTP " + res.code() + " for " + url + (body.isBlank() ? "" : (": " + body)));
      }
      return OM.readTree(body);
    }
  }
}

