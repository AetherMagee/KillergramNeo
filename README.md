<p align="center">
  <img src="art/icon.svg" width="150" alt="KillergramNeo">
</p>

# Killergram Neo
Make Telegram usable again!

A successor to [Killergram](https://web.archive.org/web/20240320064655/https://github.com/shatyuka/Killergram) by shatyuka, the Xposed module aimed to enhance Telegram without needing to modify the original app.

🇺🇸 Currently reading in English | 🇷🇺 [Переключиться на русский язык](/README.ru.md)

## ⬇️ Downloads
Download latest release [here.](https://github.com/AetherMagee/KillergramNeo/releases/latest)

## Installation requirements
To use Killergram Neo, you need:

* A rooted Android device
* An **Xposed API provider** - we recommend [Vector](https://github.com/JingMatrix/Vector) 
## Features
#### Appearance
* Replace in-app icons with the **Solar pack** by [@Design480](https://t.me/Design480)
* Add adaptive **Monet Light** and **Monet Dark** themes to Telegram's theme picker
* Force system fonts
* Replace the `edited` label with a monochrome pencil icon
* Show seconds in timestamps (`HH:mm:ss`)
* Disable subscriber/member count rounding
* Replace the app title with your account first name or custom text, with optional centered title

#### Navigation
* Hide stories
* Hide the floating create button in the chat list
* Hide selected entries from the hamburger menu
* Hide the channel action bar (mute/discussion/gifts/join)
* Hide the side share button on channel posts
* Hide the keyboard when scrolling chats
* Add folder icons and choose how folder tabs are displayed

#### Camera
* Increase video note bitrate
* Increase video note resolution
* Default camera and video notes to the rear camera
* Keep video note zoom after lifting your fingers

#### Chats & media
* Default media sending to **HD** in the photo picker
* Keep the attach camera tile blurred until tapped
* Disable audio playback triggered by volume buttons
* Disable Telegram's built-in notification delay
* Raise recent stickers and emoji limits

#### Privacy & profile
* Hide your phone number in Telegram UI
* Show a copyable user ID on profile screens
* Allow forwarding from anywhere*

#### Restrictions & cleanup
* Remove sponsored messages*
* Hide paid **Stars** reactions
* Hide in-app update prompts

#### Experimental
* Force local Premium* for local UI checks only; server-side Premium features still do not unlock
* Anti-recall deleted messages* by keeping deleted content in chat history locally

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

