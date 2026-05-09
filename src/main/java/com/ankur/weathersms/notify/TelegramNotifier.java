package com.ankur.weathersms.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public final class TelegramNotifier {
  private static final ObjectMapper OM = new ObjectMapper();
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient http;
  private final String botToken;

  public TelegramNotifier(String botToken) {
    this.http = new OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(25))
        .build();
    this.botToken = botToken;
  }

  public void sendMessage(String chatId, String text) throws IOException {
    String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

    String payload = OM.writeValueAsString(Map.of(
        "chat_id", chatId,
        "text", text
    ));

    Request req = new Request.Builder()
        .url(url)
        .post(RequestBody.create(payload, JSON))
        .build();

    try (Response res = http.newCall(req).execute()) {
      String body = res.body() == null ? "" : res.body().string();
      if (!res.isSuccessful()) {
        throw new IOException("Telegram HTTP " + res.code() + (body.isBlank() ? "" : (": " + body)));
      }
    }
  }
}

