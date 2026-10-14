create table if not exists posts
(
    post_id bigserial primary key ,
    author_id               bigint not null references users (id) on DELETE cascade,
    topic            varchar(255) not null,
    text   varchar(255) not null,
    created_at timestamp without time zone not null
);