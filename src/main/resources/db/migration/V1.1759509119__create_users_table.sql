create table if not exists users
(
    id         bigserial primary key,
    login   varchar(255) not null unique,
    password   varchar(255) not null,
    registered timestamp default current_timestamp,
    usertype   varchar(255) not null
);