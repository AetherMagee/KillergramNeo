<p align="center">
  <img src="art/icon.svg" width="150" alt="KillergramNeo">
</p>

# Killergram Neo
Make Telegram usable again!

A successor to [Killergram](https://web.archive.org/web/20240320064655/https://github.com/shatyuka/Killergram) by shatyuka, the Xposed module aimed to enhance Telegram without needing to modify the original app.

🇺🇸 Currently reading in English | 🇷🇺 [Переключиться на русский язык](/README.ru.md)

## ⬇️ Downloads
Download latest release [here.](https://github.com/AetherMagee/KillergramNeo/releases/latest)

## Features
* Replace in-app icons with the **Solar pack** by [@Design480](https://t.me/Design480)
* **Hide stories**
* Hide **channel action bar** (mute/discussion/gifts/join)
* Hide **floating create button** in the chat list (compose/story)
* Show **seconds in timestamps** (`HH:mm:ss`)
* Default media sending to **HD** in photo picker (not send-as-file)
* Keep attach camera tile blurred until tapped
* **Hide keyboard when scrolling** in chats
* Force system fonts
* Hide **in-app update prompts**
* Disable **audio autoplay** on volume button press
* Disable subscriber count rounding
* Hide paid **Stars reactions** in channels
* Remove sponsored messages*
* Allow message forwarding from anywhere*
* Override account limit*
* Force local 'Premium'* (for experiments only, doesn't do anything for server-side features)
* Keep all deleted messages* (extremely buggy, not supposed to be used like AyuGram or others)

*(this list might lag behind the actual feature set sometimes as we're constantly improving the module)*

\* - Potentially breaking Telegram's ToS, be careful!

## Supported clients
In theory: Any client that is a fork of original [Telegram](https://github.com/DrKLO/Telegram) app.

Has been tested on:
* **Telegram** - org.telegram.messenger(.web), versions `12.1.1` (recommended) and `12.6.3`
* exteraGram* - com.exteragram.messenger
* Cherrygram* - uz.unnarsx.cherrygram
* Octogram* - it.octogram.android

\* - It is strongly advised to **NOT** use this module on a custom client. 
Most custom clients and KG Neo have a lot of feature overlap already, and if the module causes an error, a nonsensical crash report gets sent to the custom app's developer with **no information about the module whatsoever.**
That means that: 1) you may have unexpected behavior while using the app, and 2) the devs won't be able to help you.
Save all of us some time.

This is actually the reason why the module **does not work** on Nekogram -
the devs have implemented obfuscation to prevent blatant injection like this.
If you still want to inject something into Neko, check out [Re:Telegram.](https://github.com/Sakion-Team/Re-Telegram)

## Unsupported clients

* **Nekogram**
* **Telegram X** and any of its derivatives

## FAQ
* **'Chat translation spoof for non-premium?'** - Impossible, as Telegram apparently rate-limits users based on some serverside Premium check.
* **'Auto-update?'** - Use [Obtainium.](https://github.com/ImranR98/Obtainium)
* **'LSPatch support?'** - There is no support now.
