create table if not exists tags(
    id bigserial primary key,
    name varchar(255) not null unique
);