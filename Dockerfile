FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY . .
# build gradle project
RUN ./gradlew build
RUN mkdir -p /app/build/libs
RUN find . -path "*/build/libs/*-all.jar" -exec cp {} /app/build/libs \;
RUN find /app/build/libs -name "*-all.jar" -exec bash -c 'mv "$1" "${1//-all/}"' _ {} \;


FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/
RUN cat > /entrypoint.sh <<EOF
#!/bin/bash

app="\$1"
shift

case "\$app" in
  lobby)
    exec java -jar /app/lobby.jar "\$@"
    ;;
  *)
    echo "Unknown app: \$app, trying to run it as a command"
    exec "\$app" "\$@"
    ;;
esac
EOF
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]