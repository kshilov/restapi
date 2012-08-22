alter table offer_action
add column last_change_time timestamp without time zone default now();
update offer_action set last_change_time = creation_time;
