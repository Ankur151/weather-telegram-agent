package com.ankur.weathersms.config;

import java.time.ZoneId;

public final class Env {
  private Env() {}

  public static String require(String key) {
    String v = System.getenv(key);
    if (v == null || v.trim().isEmpty()) {
      throw new IllegalArgumentException("Missing env var: " + key);
    }
    return v.trim();
  }

  public static String optional(String key, String defaultValue) {
    String v = System.getenv(key);
    if (v == null || v.trim().isEmpty()) return defaultValue;
    return v.trim();
  }

  public static int optionalInt(String key, int defaultValue) {
    String v = System.getenv(key);
    if (v == null || v.trim().isEmpty()) return defaultValue;
    try {
      return Integer.parseInt(v.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid integer env var: " + key);
    }
  }

  public static ZoneId optionalZoneId(String key, ZoneId defaultValue) {
    String v = System.getenv(key);
    if (v == null || v.trim().isEmpty()) return defaultValue;
    try {
      return ZoneId.of(v.trim());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone in env var: " + key);
    }
  }
}

