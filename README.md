Currency BG
=========================

[![Android CI](https://github.com/vexelon-dot-net/currencybg.app/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/vexelon-dot-net/currencybg.app/actions/workflows/android.yml)

<a href='https://play.google.com/store/apps/details?id=net.vexelon.currencybg.app&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='undefined' src='https://play.google.com/intl/en_us/badges/images/generic/bg_badge_web_generic.png' width="180px"/></a>

**Currency BG** е Android приложение, което предоставя информация относно обменните курсове на
валутите спрямо Българският Лев (BGN).

**Currency BG** is an Android application that delivers up-to-date currency exchange rates for
Bulgarian Lev (BGN).

**Currency BG** ist eine Android-App, die aktuelle Wechselkurse für den Bulgarischen Lew (BGN)
liefert.

* Runs on Android `4.4` or later

# Contributing

Докладвайте бъгове или добавете своите предложения и идеи
на [Issues](https://github.com/vexelon-dot-net/currencybg.app/issues) дъската.

Use the [Issues](https://github.com/vexelon-dot-net/currencybg.app/issues) board to report bugs or
open feature requests.

## Development

Get [Android Studio](https://developer.android.com/studio).

Install SDK `API 31` to be able to build the project.

Create a `gradle.properties` file and fill in the signing-certificate properties.
Use `tools/gradle.properties.tpl` as template.

Create an `api.properties` file and fill in the remote server connection parameters.
Use `tools/api.properties.tpl` as template.

To build the project run:

    ./gradlew clean build

To install a debug build run:

    ./gradlew installDebug

# License

[GPL](LICENSE)
