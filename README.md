# Weather Telegram Agent (Spring Boot, Open-Meteo + Telegram)

Sends you a **daily Telegram message** with a short weather summary for your city (India) using:
- **Open-Meteo Geocoding** (city → lat/lon) (free, no key)
- **Open-Meteo Forecast** (free, no key)
- **Telegram Bot API** (free)

## Requirements
- Java **17+**
- Maven **3.8+**
- A Telegram account (to receive messages)

## Project path (as created)
`C:\Users\Ankur Vashist\Desktop\Workplace\Learning\weather-sms-agent`

## Configuration

### `application.properties`
Edit:
- `src\main\resources\application.properties`

Defaults included:
- `weather.city=Faridabad`
- `weather.countryCode=IN`
- `weather.timezone=Asia/Kolkata`
- `weather.smsMaxLen=240`

Telegram settings (required):
- `weather.telegram.botToken`
- `weather.telegram.chatId`

## Create a Telegram bot + get your chat id

1. In Telegram, open `@BotFather`
2. Send: `/newbot`
3. Follow prompts and copy the **bot token**
4. Start a chat with your bot (search your bot username and press **Start**)
5. Get your `chat_id` (pick one method):
   - Method A (simple): in a browser open:
     - `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
     - After you press Start / send a message to the bot, find `\"chat\":{\"id\": ... }`
   - Method B: add a helper bot (like `@RawDataBot`) and read your chat id

Put those values into `application.properties`.

## Secrets: do NOT commit your bot token
Keep `weather.city` in `application.properties` so you can change it for travel, but provide Telegram secrets via env vars (local) or GitHub Actions Secrets:
- `WEATHER_TELEGRAM_BOT_TOKEN` → maps to `weather.telegram.botToken`
- `WEATHER_TELEGRAM_CHAT_ID` → maps to `weather.telegram.chatId`

## Build

From the project folder:

```powershell
mvn -DskipTests package
```

This creates:
- `target\weather-sms-agent.jar`

## Run

```powershell
java -jar target\weather-sms-agent.jar
```

On success, it prints the sent message:
`Sent: ...`

## Schedule (Windows Task Scheduler) — Daily 7:00 AM

1. Open **Task Scheduler**
2. Click **Create Task...**
3. **General** tab
   - Name: `Weather SMS Agent`
   - Select: **Run whether user is logged on or not** (optional)
4. **Triggers** tab → **New...**
   - Begin the task: **On a schedule**
   - Settings: **Daily**
   - Start: set to **7:00:00 AM**
5. **Actions** tab → **New...**
   - Action: **Start a program**
   - Program/script: `java`
   - Add arguments:
     - `-jar "C:\Users\Ankur Vashist\Desktop\Workplace\Learning\weather-sms-agent\target\weather-sms-agent.jar"`
   - Start in:
     - `C:\Users\Ankur Vashist\Desktop\Workplace\Learning\weather-sms-agent`

### Setting environment variables for Task Scheduler
No environment variables are required if you keep all values in `application.properties`.

## Troubleshooting
- If Telegram fails, confirm the bot token is correct and you have started a chat with the bot at least once.
- If `getUpdates` returns nothing, send a message to your bot and refresh.

