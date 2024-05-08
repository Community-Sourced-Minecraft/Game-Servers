# Game servers

## Developing

### Prerequisites

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- Java 21 (something like Amazon Corretto 21 or Eclipse Temurin 21)

### Running

```bash
# Run the NATS server from the proxy's repository if you don't have it running

# If your standard JVM is not Java 21, you can set the JAVA_HOME environment variable
export JAVA_HOME=/path/to/java-21

# Run the lobby server
./gradlew build

# Set the PORT environment variable to the port you want the server to listen to,
# make sure it's not the same as the proxy's port
export PORT=25566
# If you're using the proxy, you have to set the VELOCITY_SECRET environment variable
# to the value you set in the proxy's configuration, `csmc` is the default value
export PROXY_SECRET=csmc
$JAVA_HOME/bin/java -jar lobby/build/libs/lobby-all.jar
```

## Thanks

<div style="display: flex; flex-direction: column; width: fit-content; align-items: center">
  <a href="https://qaze.app">
    <img src="./.misc/qaze.svg" alt="Qaze - The NATS GUI" width="200"/>
  </a>
  <p>Qaze - providing us with <b>the</b> GUI for NATS</p>
</div>
