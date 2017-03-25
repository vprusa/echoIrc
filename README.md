# Akka HTTP / Scala.js / Websocket Chat App / IRC log bot

Used project examples:
[akka-http-scala-js-websocket-chat](https://github.com/jrudolph/akka-http-scala-js-websocket-chat/)

[ircLogBot](https://github.com/Jiri-Kremser/ircLogBot)

## Installation notes:

### Notable issues

I have encountered some problems related to [this](https://groups.google.com/forum/#!topic/simple-build-tool/f6ngum3pMMs) and [this](https://issues.scala-lang.org/browse/SI-9199) when running ```sbt``` cmd

To solve this I removed ~/.m2 , ~/.ivy2 and ~/.sbt dirs (idk which one was causing dependency problems) and installed latest zip version of sbt provided [here](http://www.scala-sbt.org/download.html)

## To run:

```
sbt

> project backend
> run
```

Navigate to [http://localhost.localdomain:8080/](http://localhost.localdomain:8080/).

You can build a fully self-contained jar using `assembly` in the backend project.

## Configuration

You can set `app.interface` and `app.port` in `application.conf` to configure where the server
should listen to.

This also works on the command line using JVM properties, e.g. using `re-start`:

```
> re-start --- -Dapp.interface=0.0.0.0 -Dapp.port=8080
```

will start the server listening on all interfaces.

## CLI

The `cli` project contains a command line client for the chat to demonstrate the Websocket client and
how to deal with console input in a streaming way.

It runs best directly from a terminal.

Start the server as explained above. Then, to build a fat jar use

```
sbt

> project cli
> assembly
```

Run

```
java -jar cli/target/scala-2.11/cli-assembly-0.1-SNAPSHOT.jar
```

or 

```
./chat
```

## Known issues

### Handling of backpressure

The chat (actor) itself doesn't yet implement any meaningful backpressure logic.
  * On the incoming side you probably want to backpressure (rate-limit) each client itself and the total rate of messages maybe as well
  * On the ougoing side you don't want one slow chat participant to slow down the complete chat. Right now the outgoing side uses a `Source.actorRef` with an overflow strategy of fail: if a client doesn't keep up with receiving messages and the network send buffer on the chat server for that client fills up the client will be failed. (This is somewhat similar to [what Twitter does in its streaming APIs](https://dev.twitter.com/streaming/overview/connecting)). A better strategy may be to drop
messages (and leave a note for the user) until the client catches up with the action.

### Usage of stream combinators

Ideally, akka-stream would support dynamic merge/broadcast operations, so that you never need to break out of stream logic. Right now, collecting and broadcasting messages is done by the chat actor and for each user a manual stream pipeline needs to be setup.

### The "frontend"

There isn't more than absolutely necessary there right now.

### to get size of project
```
du -sh --exclude=./backend/target --exclude=./cli/target --exclude=./frontend/target --exclude=./target --exclude=./shared/.js --exclude=./shared/.jvm --exclude=./project/target --exclude=./project/project/target ./

du -sh --exclude=./backend/target --exclude=./cli/target --exclude=./frontend/target --exclude=./target --exclude=./shared/.js --exclude=./shared/.jvm --exclude=./project/target --exclude=./project/project/target --exclude=./.idea ./
```

### Docker

from https://hub.docker.com/r/hseeberger/scala-sbt/
```
FROM hseeberger/scala-sbt
```