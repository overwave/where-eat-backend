--liquibase formatted sql

--changeset overwave:create_files_table
create table if not exists file
(
    id   serial not null primary key,
    path text   not null,
    size int    not null
);

--changeset overwave:create_posts_table
create table if not exists post
(
    id      serial  not null primary key,
    hidden  boolean not null default false,
    text    text    not null,
    file_id int references file (id)
);

--changeset overwave:create_scanned_ranges_table
create table if not exists scanned_range
(
    id              bigserial not null unique primary key,
    ordinal         int       not null,
    message_id_from bigint    not null,
    message_id_to   bigint    not null
);

--changeset overwave:add_columns_to_posts
alter table post
    add column if not exists message_id bigint not null default 0;
alter table post
    add column if not exists type text not null default '-';

--changeset overwave:replace_posts_table_with_messages
drop table if exists post cascade;
drop table if exists file cascade;
create table if not exists media
(
    id     text  not null unique primary key,
    "data" bytea not null
);

create table if not exists article
(
    id bigserial not null unique primary key
);

create table if not exists message
(
    id          serial    not null unique primary key,
    message_id  bigint    not null,
    "text"      text      not null,
    article_id  int references article (id),
    media_id    text references media (id),
    group_id    text      not null,
    "timestamp" timestamp not null,
    hidden      boolean   not null default false,
    "type"      text      not null
);
create unique index message_id_idx on message (message_id);

create table if not exists text_attachment
(
    id         bigserial not null unique primary key,
    message_id int       not null references message (id),
    "type"     text      not null,
    _offset    int       not null,
    "length"   int       not null,
    "value"    text
);
