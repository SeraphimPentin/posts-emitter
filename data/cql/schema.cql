CREATE  KEYSPACE IF NOT EXISTS posts_emitter
    WITH REPLICATION = {
        'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS posts_emitter.accounts (
    chat_id        bigint,
    social_media   varchar,
    account_id     bigint,
    user_full_name varchar,
    access_token   varchar,
    refresh_token  varchar,
    PRIMARY KEY ((chat_id), social_media, account_id)
);

CREATE TABLE IF NOT EXISTS posts_emitter.channel_groups (
    channel_id       bigint,
    social_media     varchar,
    group_id         bigint,
    group_name       varchar,
    access_token     varchar,
    chat_id          bigint,
    account_id       bigint,
    channel_username varchar,
    PRIMARY KEY ((channel_id), social_media, group_id)
);

CREATE TABLE IF NOT EXISTS posts_emitter.current_account (
    chat_id        bigint,
    social_media   varchar,
    account_id     bigint,
    user_full_name varchar,
    access_token   varchar,
    refresh_token  varchar,
    PRIMARY KEY ((chat_id))
);

CREATE TABLE IF NOT EXISTS posts_emitter.current_channel (
    chat_id          bigint,
    channel_id       bigint,
    channel_username varchar,
    PRIMARY KEY ((chat_id))
);

CREATE TABLE IF NOT EXISTS posts_emitter.current_group (
    chat_id      bigint,
    social_media varchar,
    group_id     bigint,
    group_name   varchar,
    account_id   bigint,
    access_token varchar,
    PRIMARY KEY ((chat_id))
);

CREATE TABLE IF NOT EXISTS posts_emitter.current_state (
    chat_id bigint,
    state   varchar,
    PRIMARY KEY ((chat_id))
);

CREATE TABLE IF NOT EXISTS posts_emitter.user_channels (
    channel_id       bigint,
    chat_id          bigint,
    channel_username varchar,
    is_autoposting   boolean,
    is_notification  boolean,
    PRIMARY KEY ((channel_id), chat_id)
);
