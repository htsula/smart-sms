# Smart SMS

A personal fork of [Fossify Messages](https://github.com/FossifyOrg/Messages), an open-source SMS/MMS
app for Android. Built to replace the stock messaging app on my own phone, with a few additions on top.

> **This is a modified version of Fossify Messages.** It is not affiliated with, endorsed by, or
> supported by the Fossify project. Do not report issues with this fork to them.
>
> Forked from upstream commit [`25971576`](https://github.com/FossifyOrg/Messages/commit/25971576)
> (v1.9.1) on **26 August 2026**. All credit for the app itself belongs to the Fossify project and,
> before it, Simple Mobile Tools.

## Changes from upstream

| Date | Change |
|---|---|
| 2026-08-26 | Rebranded — `applicationId` `org.htsul.smartsms`, launcher label "Smart SMS", so it installs alongside upstream builds |
| 2026-08-27 | Added a **"Copy code"** notification action that detects one-time passcodes in incoming SMS and copies them to the clipboard |

Everything else is upstream's work, unmodified.

## Copy code

When an incoming message contains a verification code, the notification gains a "Copy code" action
that copies the code and dismisses the notification. When no code is found the notification is
unchanged.

Detection is keyword-gated and shape-scored rather than proximity-based, so `code is 848291` wins
over `code expires in 2026`. Qualifiers like *tracking*, *discount*, *promo*, *coupon*, *area* and
*postal* veto the keyword outright, which keeps delivery and marketing texts silent — those being
among the most common texts anyone actually receives.

Note that Android 12+ already surfaces a similar copy chip through its own adaptive-notification
suggestions. This implementation is deterministic, works back to API 26, and does not depend on that
setting being enabled.

## Building

Requires JDK 17 and the Android SDK (compileSdk 36).

```bash
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="/path/to/Android/Sdk"
./gradlew assembleFossDebug
```

Release builds need signing credentials — copy `keystore.properties_sample` to `keystore.properties`
and fill it in, or set `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_FILE` and
`SIGNING_STORE_PASSWORD` in the environment. `keystore.properties` is gitignored; never commit it.

## Merging from upstream

`upstream` is kept as a remote so fixes — especially MMS ones — can be pulled in:

```bash
git fetch upstream
git merge upstream/main
```

The local diff is deliberately small and mostly confined to new files, to keep these merges cheap.
`gradle.properties` and `app/build.gradle.kts` are the expected conflict sites, since the rebrand
touches the same lines upstream bumps each release. That rebrand is isolated in its own commit so it
can be resolved without untangling feature code.

## License

GPL-3.0, inherited from Fossify Messages. See [LICENSE](LICENSE).
