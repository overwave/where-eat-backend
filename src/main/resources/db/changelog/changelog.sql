--liquibase formatted sql

--changeset overwave:create_files_table
create table if not exists file
(
    id   int  not null primary key,
    path text not null,
    size int  not null
);