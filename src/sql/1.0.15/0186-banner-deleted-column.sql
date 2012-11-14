begin;

alter table banner add column deleted boolean not null default false;
update banner set deleted = false;

commit;