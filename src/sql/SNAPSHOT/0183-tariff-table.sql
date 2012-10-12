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
  offer_id bigint not null,
  cpa_policy varchar(255) not null,
  value numeric(19,2) not null,
  fee_type varchar(255) not null,
  fee numeric(19,2) not null);

alter table tariff add constraint tariff_pk primary key (id);

end;
