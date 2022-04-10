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
    id              bigserial not null primary key,
    ordinal         int       not null,
    message_id_from bigint    not null,
    message_id_to   bigint    not null
);

--changeset overwave:add_message_id_to_posts
alter table post
    add column if not exists message_id bigint not null default 0,
    add column if not exists type       text   not null default '-';
