--liquibase formatted sql

--changeset overwave:initial
create table test1
(
    id   int primary key,
    name varchar(255)
);