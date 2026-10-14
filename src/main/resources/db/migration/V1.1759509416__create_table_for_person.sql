create table if not exists person
(
    id            bigint primary key ,
    name          varchar(255) not null,
    lastname      varchar(255) not null,
    date_of_birth date not null,
    constraint fk_person_user foreign key (id) references users (id) on delete cascade
);