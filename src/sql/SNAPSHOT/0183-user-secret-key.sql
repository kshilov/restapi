alter table user_profile
add column secret_key varchar(32) not null default md5(random() :: text);

alter table user_profile
add constraint secret_key_unique unique (secret_key);

create unique index secret_key_idx on user_profile (secret_key);
