# Tasks
Tasks is a java app which do some jobs. These jobs started by schedule. Also jobs more complicated than bash scripts.

In this time we support these things:

#### Daily Press
Download and send by email e-version of popular (nope) russian magazines and papers.
At this time we support:
- [Спорт-Экспресс](https://www.sport-express.ru)
- [Областная газета](https://www.oblgazeta.ru)

#### Torrent update
Periodic check torrents on popular (nope) trackers and download it if they changed.
At this time we support trackers:
- RuTor (address often chages)
- [Rutracker](https://rutracker.org)
- [LostFilm.TV](https://lostfilm.tv)

## Requirements
1. Java 8
2. MongoDB
3. SMTP server (optional)
4. Telegram bot (optional)

## How to run
1. Clone this repo on your machine
2. In the project dir run ```./gradlew build``` or ```gradlew.bat build``` prefers to your OS.
3. Find in ```build/libs``` dir  ```tasks-all.jar``` and copy it to desired location.
4. Place with jar properties file (see below) 
5. Run ```java -jar tasks-all.jar```
6. At this time we not support add task from admin web page. Honestly we doesnt have admin page. 
So you must create your tasks manully. To do this you must create record in mongodb.

#### Sample property file
```
# port of http server
http.port = 8080

# connection params to mongodb
mongo.host = 127.0.0.1
mongo.port = 27017
mongo.db   = tasks
mongo.user =
mongo.pass =

# mail send params
mail.user = 
mail.pass = 
mail.host = 
mail.port = 
mail.from = 

# trupd properties
trupd.default-dir = /opt/torrents

# curl extra params, e.g. socks proxy
curl.extra-opts = --socks5 user:pass@server:port
```

#### Sample record to daily-press
```json
{
    "job" : "sb.tasks.jobs.DailyPress", 
    "params" : {
        "mail_to" : "< ; separated recipients addresses >", 
        "subject" : "< Mail subject >", 
        "text" : "< Mail text >", 
        "url" : "http://www.sport-express.ru/"
    }, 
    "schedule" : [
        "0 0 0/2 * * ?", 
        "0 10/10 4,6 * * ?"
    ]
}
```

#### Sample record to torrent update
for rutracker.org params section must contains num field
```json
{
    "job" : "sb.tasks.jobs.Trupd", 
    "params" : {
        "num" : "< torrent num on rutracker.org >", 
        "mail_to" : "< ; recipients addresses >", 
        "admin_telegram" : "< chatId for notification >"
    },
    "schedule" : [
        "0 0 * * * ?"
    ]
}
```

for others - url field
```json
{
    "job" : "sb.tasks.jobs.Trupd", 
    "params" : {
        "url" : "https://www.lostfilm.tv/series/Riverdale/", 
        "download_dir" : "/opt/torrents", 
        "mail_to" : "< notification mail address >", 
        "telegram" : "< notification telegram chatId >"
    },
    "schedule" : [
        "0 0 * * * ?"
    ]
}

```

## Telegram
Tasks doesnt provide a telegram bot. 
To start using telegram notification you must create own telegram bot and set webhook for it in you host.
More on [Telegram Bot API](https://core.telegram.org/bots/api)

Supported telegram commands:
<table>
<tr>
    <td>1.</td>
    <td>/start</td>
    <td>Start using telegram bot. In answer you receive a chatId</td>
</tr>
<tr>
    <td>2.</td>
    <td>/admin</td>
    <td>Register new user with admin privileges</td>
</tr>
<tr>
    <td>2.</td>
    <td>/task</td>
    <td>Add torrent update tasks</td>
</tr>
<tr>
    <td>3.</td>
    <td>/ls</td>
    <td>List all tasks</td>
</tr>
<tr>
    <td>4.</td>
    <td>/info</td>
    <td>Info about task</td>
</tr>
<tr>
    <td>5.</td>
    <td>/rm</td>
    <td>Delete task</td>
</tr>
</table>

## How to contribute?
Just fork the repo and send me a pull request.
