CurrencyBG
=========================

[CurrencyBG](https://play.google.com/store/apps/details?id=net.vexelon.currencybg.app) is an Android application that delivers up-to-date currency exchange rates for Bulgarian Lev (BGN). Exchange rate information is updated from the web site of the Bulgarian National Bank - http://www.bnb.bg

The exchange rates of currencies are calculated on the basis of the fixed exchange rate of the Bulgarian Lev to the Euro, and the information source is the exchange rates of the Euro against these currencies published by the European Central Bank

Please read [HISTORY](HISTORY) for a list of changes.

# Requirements

  * Requires Android 4.0.3 (API Level 15+)
  * Requires Internet connection
  * Supported Localization - EN (default), bg_BG

# Development 

To generate Eclipse project files run:

    gradle eclipse
    
To build the project run:

    gradle clean build

To install a debug build run:

    gradle installDebug

To check what other tasks are available run:

    gradle tasks

# License
[GPL](LICENSE)
