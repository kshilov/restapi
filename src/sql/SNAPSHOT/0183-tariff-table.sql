begin;

drop table if exists tariff;

drop sequence if exists tariff_seq;

create sequence tariff_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table tariff(
  id bigint not null default nextval('tariff_seq'),
  cpa_policy varchar(255) not null,
  cost numeric(19,2),
  percent numeric(19,2),
  first_action_cost numeric(19,2),
  other_action_cost numeric(19,2),
  fee_type varchar(255) not null,
  fee numeric(19,2) not null);

alter table tariff add constraint tariff_pk primary key (id);

create index tariff_all_idx on tariff
(cpa_policy, cost, percent, first_action_cost, other_action_cost, fee, fee_type);

end;
