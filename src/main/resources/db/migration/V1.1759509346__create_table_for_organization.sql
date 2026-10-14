create table if not exists organization
(
    id               bigint primary key ,
    title            varchar(255) not null,
    specialization   varchar(255) not null,
    date_of_foundation date not null,
    constraint fk_organization_user foreign key (id) references users (id) on delete cascade
);