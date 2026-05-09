package com.ankur.weathersms.weather;

import java.util.ArrayList;
import java.util.List;

public final class AdviceGenerator {
  private AdviceGenerator() {}

  public static String advice(OpenMeteoSummary.TodayForecast t) {
    List<String> tips = new ArrayList<>();

    int pop = t.rainChancePct();
    double maxC = t.tempMaxC();
    int wind = t.windKmh();

    if (pop >= 50) tips.add("Carry an umbrella or raincoat");
    else if (pop >= 30) tips.add("Light chance of rain—umbrella may help");

    if (!Double.isNaN(maxC)) {
      if (maxC >= 38) tips.add("High heat—hydrate, avoid afternoon sun, use sunscreen");
      else if (maxC >= 34) tips.add("Warm day—use sunscreen and drink water");
    }

    if (wind >= 25) tips.add("Windy—secure loose items");

    if (tips.isEmpty()) return "";
    return "Tips: " + String.join("; ", tips) + ".";
  }
}

