# Killergram Neo
A successor to [Killergram](https://github.com/shatyuka/Killergram) by shatyuka.

An Xposed module aimed to enhance Telegram and it's forks without needing to modify the original app.

## Features
* Replace in-app icons with the Solar pack by [@Design480](https://t.me/Design480)
* Hide stories
* Disable "Thanos" deletion effect
* Disable audio autoplay on volume button press
* Remove sponsored messages*
* Allow message forwarding from anywhere*
* Override account limit*
* Force local 'Premium'* (for experiments only, doesn't do anything for server-side features)
* Force keep deleted messages* (extremely buggy, not supposed to be used like AyuGram or others)

\* - Potentially breaking Telegram's ToS, be careful! 

## Supported clients
In theory: Any client that is a fork of original [Telegram](https://github.com/DrKLO/Telegram) app.

Has been tested on:
* **Telegram** (org.telegram.messenger)
* **exteraGram** (com.exteragram.messenger)*
* **Cherrygram** (uz.unnarsx.cherrygram)*
* **Octogram** (it.octogram.android)*

\* - It is strongly advised to NOT use this module on a fork of the Telegram client. If the module causes an error, a nonsensical crash report gets sent to the app's developer with no information about the module whatsoever. That means that 1) you may have unexpected behaviour while using the app, and 2) the devs won't be able to help you. Save all of us some time.


**Note:** The module doesn't and and will NOT work on Telegram X and it's forks.

## FAQ
* **'Chat translation spoof for non-premium?'** - Impossible, as Telegram apparently rate-limits users based on some serverside Premium check.
* **'Auto-update?'** - Use [Obtainium.](https://github.com/ImranR98/Obtainium)

## Downloads
Download latest release [here](https://github.com/AetherMagee/KillergramNeo/releases/latest)