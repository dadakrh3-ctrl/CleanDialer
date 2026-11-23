# Clean Dialer

A minimal dark dialer for Android. Calls go out through your SIM (T-Mobile),
so the person you call sees your normal number and answers on their regular phone.

## Get the APK (no Android Studio needed)

1. Create a new empty repository on github.com.
2. Upload everything in this folder to it (drag and drop works; keep the `.github` folder).
3. Open the **Actions** tab and wait a few minutes for "Build APK" to finish.
4. Open the finished run, download **CleanDialer-apk**, unzip it, and you have `app-debug.apk`.
5. Copy it to your phone and install it (allow "install unknown apps" when asked).

If the build does not start by itself, open Actions and press **Run workflow**.

## Notes

- Keypad: long-press 0 for +, long-press the delete key to clear.
- Recents needs the call log permission; calling needs the phone permission.
- Without the phone permission, the app opens the system dialer instead of calling directly.
