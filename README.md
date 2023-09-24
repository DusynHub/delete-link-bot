# Delete link telegrambot

This Telegram bot deletes all messages sent by users who are not chat administrators that contain links. 
The URL-Detector library from LinkedIn is used to recognize messages with links.

Link to library: [URL-Detector](https://github.com/linkedin/URL-Detector/)

_Note from library: Keep in mind that for security purposes, it's better to overdetect urls and check more against blacklists than to not detect a url that was submitted. As such, some things that we detect might not be urls but somewhat look like urls. Also, instead of complying with RFC 3986 (http://www.ietf.org/rfc/rfc3986.txt), we try to detect based on browser behavior, optimizing detection for urls that are visitable through the address bar of Chrome, Firefox, Internet Explorer, and Safari._

_Note to bot: Users can't send code snippets because dot-notation will be detected as url  

Following texts will be detected as valid url and messages containing them will be deleted:
* '''http://www.gilliman.com'''
* ''"http://www.gilliman.com"''
* @@http://www.gilliman.com"''
* www.google.com
* google.com
* dot.com
* dfsdfhttps://xn--90aivcdt6dxbc.xn--p1ai/fsdf
* https://xn--90aivcdt6dxbc.xn--p1ai/fsdf
* http://www.foufos.gr
* https://www.foufos.gr
* http://foufos.gr
* http://www.foufos.gr/kino
* http://werer.gr
* www.foufos.gr
* ####www.mp3.com
* www.t.co
* http://t.co
* http://www.t.co
* https://www.t.co
* www.aa.com
* http://aa.com
* http://www.aa.com
* https://www.aa.com
* www.foufos
* www.foufos-.gr
* www.-foufos.gr
* foufos.gr
* http://www.foufos
* www.mp3#.com
* 23422.com
* 111111.by
* fsdfsdfsdfsdf434234.net
* 0--09-0909-0.comcwWERGWDFWEF3323###4
* ------.com
* www.mp3.com
* jooom.molod.kol
* разъяснены новые на сайте объясняем.рф

## How to work
__1 This version of the bot is for running using docker-compose and your own Telegram bot data, namely the name and token.
To use docker compose you need JDK and Maven on machine__

__2 Get the bot name and bot token from botfather in the Telegram app.__
![img.png](img.png)

__3 Download the project from DockerHub masfujdocker/deletelinkbot:v1.1__

__4 In CLI run command. Insert your data instead of \<BOT NAME> and \<BOT TOKEN> into the command:__
```bash
docker run -e “BOT_NAME=<BOT NAME>” -e “BOT_TOKEN=<BOT TOKEN>” masfujdocker/deletelinkbot:v1.1 python console.py__
```
Run command in the directory with the unzipped project
```bash
example command:

docker run  -e "BOT_NAME=somebot" -e "BOT_TOKEN=6025044839:AAHlPLJzljjDQ2ggr1qhsBAldQCJe3SnhJI" masfujdocker/deletelinkbot:v1.1 python console.py
```
__5 Now bot is working. Add your bot by name in a supergroup in Telegram and give him administrator role with a permission to delete messages.__

__6 Delete link bot logs will be saved in file:__
```bash
<root>/var/log/dlb-log-win/dlb.log
```