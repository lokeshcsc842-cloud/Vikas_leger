# Vikas ka Hisab Kitab — fresh Android project

This project is a starter Android app for locally detecting incoming payment SMS alerts and displaying them as:

- `UPI Money Received` — spoken phrase: `पैसा मिल गया`
- `Money Transfer Success` — spoken phrase: `Money transfer success`

The app intentionally does not use the labels `आय` or `व्यय`.

## Build an APK without Android Studio

1. Create a new GitHub repository with branch `main`.
2. Extract this ZIP on your computer.
3. Upload the *contents* of this folder to the repository root, including `.github`, `app`, `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties`. Do not upload the ZIP itself.
4. Commit the files.
5. Open **Actions** → **Build Vikas Hisab Kitab APK**. Wait for a green check.
6. Open the completed workflow run and download the artifact named `Vikas-ka-Hisab-Kitab-debug-APK`. Extract that downloaded ZIP to get `app-debug.apk`.

This workflow installs Gradle in GitHub's cloud runner and does not depend on a local `gradlew` file, avoiding the previous `gradlew: No such file or directory` failure.

## Important limitations and safety notes

- Install the APK on an Android phone and explicitly grant SMS permission. SMS contents stay on-device in this starter implementation.
- Android may not deliver SMS broadcasts after the app is force-stopped, and battery/vendor restrictions can delay background work. Test with the phone you will use.
- Bank SMS templates vary. This initial parser only handles common English credit/debit wording and intentionally ignores ambiguous and failed messages. Test using redacted examples from your bank before trusting the totals.
- Some UPI apps do not send a bank SMS for every payment. This version does not read other apps' notifications.
- Android's installed text-to-speech engine controls voice availability. It tries to select a Hindi voice marked male if the engine exposes one, but male voice cannot be guaranteed on every phone. You can change/install a Hindi TTS voice in Android settings.
- This is a starter project; test before relying on it for financial records. It is for personal sideloading, not prepared for Google Play SMS-permission review.
