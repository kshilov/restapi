begin;

alter table user_profile alter column first_name drop not null;
alter table user_profile alter column last_name drop not null;

commit;