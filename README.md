# DiscordProximity

DiscordProximity is a client side proximity chat plugin for Vencord, currently supports:
- Forge 1.8.9

## Installation & Setup

### [Video Setup Guide](https://youtu.be/Beje49896RU)
[![Video Thumbnail](https://img.youtube.com/vi/Beje49896RU/mqdefault.jpg)](https://youtu.be/Beje49896RU)

### Setup Checklist

1. Install [Discord Proximity for Forge 1.8.9](<https://github.com/Siriusmart/DiscordProximity/releases/latest/download/discordproximity.jar>) and restart Minecraft.
2. Install this custom build of Vencord and restart Discord. ([Windows](<https://github.com/Siriusmart/Equilotl/releases/latest/download/Equilotl.exe>)) ([MacOS](<https://github.com/Siriusmart/Equilotl/releases/latest/download/Equilotl.MacOS.zip>)) ([Linux](<https://github.com/Siriusmart/Equilotl/releases/latest/download/ EquilotlCli-linux >)) ([Userscript/Browser](<https://github.com/Siriusmart/Equicord/releases/latest/download/Vencord.user.js>))
3. Check if Vencord is installed - in the user settings shall you see a Vencord section to your left.
4. Go to the plugins tab and turn on **DiscordProximity**.
5. Run the command `.iam [Minecraft IGN]` anywhere in [this server](https://discord.gg/N8HQ2HsDNH), you must use the __exact upper/lowercase as your IGN__ in the command.
Now that it's set up you won't have to do this again, join a VC with others who also have the mod and it should just work.

## FAQ

***Q: This sounds like beaming. Is my account safe?***
A: Beaming asks for your email and verification code, the setup only asks for your IGN which everyone else in lobby 1 can also see. However.

***Q: Why do you need my IGN? My parents told me I shouldn't tell strangers my IGN.***
A: You need the IGN of everyone else in VC to determine how loud they sound based on their in-game distance from you. Setting your IGN to setup is the most convenient way of doing this.

***Q: How does this work?***
A: The Minecraft mod starts a server which the Vencord plugin connects to, the mod tells the plugin how loud to make the people in VC sound.

***Q: What if I have multiple accounts? What if I mistyped my IGN?***
A: The commands `.whoami`, `.iam` and `iamnot` should answer your question.

***Q: Why should I trust you that this is not a virus?***
A: You don't have to, __trust the code__ instead. Which you can look at here:
-  [DiscordProximity - Vencord plugin](<https://github.com/Siriusmart/equicord/tree/main/src/plugins/discordProximity>)
- [DiscordProximity - Forge mod](<https://github.com/Siriusmart/DiscordProximity>)
If you vaguely know anything about programming, I am more than happy to explain to you what each line of code does in a VC.

***Q: Does it support Lunar client?***
A: I know a guy who works with injection clients and he said he would help me with this, although launching the game would be more complicated than using Forge.

I would also point out that modloaders are invented specifically so that you don't have to inject mods to clients manually, and all-in-one clients are a dumb idea.
