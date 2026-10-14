create table if not exists post_and_tags(
    post_id bigint not null references posts (post_id),
    tag_id bigint not null references  tags (id)
);