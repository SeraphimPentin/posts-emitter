FROM gradle:jdk17

WORKDIR /posts-emitter-app
COPY . .

RUN gradle build

CMD gradle :tg-bot:run
