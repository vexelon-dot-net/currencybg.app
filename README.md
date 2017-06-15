Currency BG
=========================

[![Build Status](https://travis-ci.org/vexelon-dot-net/currencybg.app.svg?branch=master)](https://travis-ci.org/vexelon-dot-net/currencybg.app)

<a href='https://play.google.com/store/apps/details?id=net.vexelon.currencybg.app&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='undefined' src='https://play.google.com/intl/en_us/badges/images/generic/bg_badge_web_generic.png' width="180px"/></a>

**Currency BG** е Android приложение, което предоставя информация относно обменните курсове на валутите спрямо Българският Лев (BGN).

**Currency BG** is an Android application that delivers up-to-date currency exchange rates for Bulgarian Lev (BGN).

  * Requires Android `4.0.3+` (API `15+`)

# Contributing

Докладвайте бъгове или добавете своите предложения и идеи на дъската с [Issues](https://github.com/vexelon-dot-net/currencybg.app/issues). Ако сте кодер, просто направете fork на репото и отворете [pull requests](https://github.com/vexelon-dot-net/currencybg.app/pulls).

Please log feature requests or bug reports using the [Issues Board](https://github.com/vexelon-dot-net/currencybg.app/issues) or fork this repository and contribute back by sending [pull requests](https://github.com/vexelon-dot-net/currencybg.app/pulls).

## Development

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
