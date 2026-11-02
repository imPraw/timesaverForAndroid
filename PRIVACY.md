# Privacy Policy — One Pause

**Effective date: November 2, 2026**

One Pause is a local-first Android app that helps you pause before opening distracting apps. It is designed so that your attention data never leaves your phone.

## The short version

- One Pause collects **no data** and has **no internet permission**.
- There is no account, advertising, analytics, crash reporting, or telemetry.
- The Accessibility service sees only which app is in the foreground. It cannot read screen content.
- Your protected apps and pause history are stored locally on your device.
- Android backup is disabled, so this data is not copied to Google Drive or another cloud backup.

## What One Pause stores on your device

The app keeps the following in private app storage using Android `SharedPreferences`:

| Data | Purpose |
| --- | --- |
| Package names of apps you protect | To know when to show the pause |
| Pause length, reclaimed-time estimate, haptics preference | To apply your settings |
| Onboarding completion flag | To skip onboarding on later launches |
| Pause history: timestamp, app package name, and decision (left or continued) | To show insights and estimates |

Pause history is capped at 4,000 events. There is no free-text field, contact data, location data, or device identifier stored by One Pause.

You can erase pause history at any time from **Settings → Erase pause history**. Uninstalling the app, or clearing its storage in Android settings, removes all of the data above.

## Accessibility permission

One Pause uses Android's Accessibility service to notice when a protected app comes to the foreground.

- The service is configured for `typeWindowStateChanged` events only.
- It requests `canRetrieveWindowContent="false"`.
- It does not read the text on your screen, your messages, your passwords, your keystrokes, or your taps.
- It compares the foreground app's package name with the list you chose.
- The pause is drawn as an accessibility overlay. One Pause does not request the "display over other apps" permission.

Android devices require you to enable Accessibility manually. You can turn it off at any time in **Android Settings → Accessibility → One Pause app protection**. Disabling it stops app protection immediately.

## What One Pause does not do

- No internet permission, so it cannot upload or transmit information.
- No user accounts or sign-in.
- No advertising or ad identifiers.
- No third-party analytics, attribution, or crash-reporting SDKs.
- No reading of screen content, notifications, messages, passwords, or input.
- No selling, renting, or sharing of personal information.
- No access to contacts, photos, location, microphone, or camera.

## Backup and data extraction

`android:allowBackup="false"` is set on the application. The app's backup rules and data-extraction rules also exclude all app data. One Pause data is not included in Android cloud backups or device-to-device transfers.

## Security

All app data is kept in Android's private app sandbox. The app has no network permission and no exported services or secondary activities. The only exported component is the main launcher activity. The Accessibility service is protected by the system's `BIND_ACCESSIBILITY_SERVICE` permission.

No method of storage is perfectly secure, but One Pause minimizes risk by collecting very little and never transmitting it.

## Children

One Pause is not directed at children and collects no personal information from anyone, including children.

## Third-party components

One Pause includes open-source libraries and a bundled typeface. They run inside the app and do not receive your pause history. The app's lack of internet permission also prevents them from transmitting data. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for licenses and attribution.

## Changes to this policy

If this policy changes, the updated version will be included with the app source and will show a new effective date. Because the app has no network permission, it cannot update this policy remotely.

## Contact

This is a local-only app with no developer server. For questions about a build you received, contact the person or organization that provided that build. If you build One Pause yourself, you control the app and its data entirely.
