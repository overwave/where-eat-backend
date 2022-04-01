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
