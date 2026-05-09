package com.ankur.weathersms.config;

public record Telegram(
    String botToken,
    String chatId
) {}

