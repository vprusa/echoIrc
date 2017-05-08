
# Echo IRC

## Sandbox project
[playing-reactjs](https://github.com/knoldus/playing-reactjs)

[play-with-scalajs-example](https://github.com/vmunier/play-with-scalajs-example/)

[ircLogBot](https://github.com/Jiri-Kremser/ircLogBot)

[akka-http-scala-js-websocket-chat](https://github.com/jrudolph/akka-http-scala-js-websocket-chat)

[play-scala-chatroom-example](https://github.com/playframework/play-scala-chatroom-example)

[play-rest-security](https://github.com/jamesward/play-rest-security)

[gist/strobe/8515423](https://gist.github.com/strobe/8515423)

## How to run

```
sbt run
```

For irc chat/bot go to [http://localhost:9000/ircChat](http://localhost:9000/ircChat)

or ReactJS on top of Scala.js [http://localhost:9000/react](http://localhost:9000/react)

### SecureSocial examples
https://github.com/jandro-es/demo-securesocial
https://github.com/search?utf8=%E2%9C%93&q=securesocial&type=

## TODO
- Add logging irc to files in dirs by userId
- and rotate logs using by parallel/Future task 
-- on server start, 
-- on cli command
- add cli (change this to multiproject)
- login template with menu

### to get size of project
```bash
du -sh --exclude=./client/target --exclude=./server/target --exclude=./target --exclude=./shared/.js --exclude=./shared/.jvm --exclude=./project/target --exclude=./project/project --exclude=./.idea --exclude=./server/public/images --exclude=./server/public/javascripts/flot ./
```

### Db evolutions inserts

```SQL
insert into user(userId,providerId,firstName,lastName,email,avatarUrl,authMethod,token,secret,accessToken,accessToken,expiresIn,refreshToken)
VALUES ("admin","db","admin","admin","admin@localhost","","","userpass","","","",3600,"")
;

insert into user(userId,providerId,firstName,lastName,email,avatarUrl,authMethod,token,secret,accessToken,accessToken,expiresIn,refreshToken) VALUES ("admin2","db","admin","admin","user1@demo.com","","","userpass","","","",3600,"");

insert into user(userId,providerId,firstName,lastName,email,avatarUrl,authMethod,token,secret,accessToken,accessToken,expiresIn,refreshToken) VALUES ("user1","db","admin","admin","user1@demo.com","","","userpass","","","",3600,"");
```