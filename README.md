# Bark mobile app

Mobile app to detect motion via camera.

## Features
- Works as a foreground service.
- Configurable
- Saves images to media library
- Sends images to Telegram

## Development
- create `res/values/secrets.xml` file with following content
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="telegram_bot_api_token">xxxxxx</string>
</resources>
```
