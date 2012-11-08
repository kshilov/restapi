begin;

alter table offer_stat
add column action_count int not null default 0;

alter table offer_stat
add column confirmed_action_count int not null default 0;

alter table offer_stat
add column canceled_action_count int not null default 0;

end;
