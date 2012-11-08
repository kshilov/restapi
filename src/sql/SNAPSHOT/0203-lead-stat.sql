begin;
drop table if exists lead_stat;
drop sequence if exists lead_stat_seq;

create sequence lead_stat_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table lead_stat(
  id bigint default nextval('lead_stat_seq'),
  lead_key varchar(32) not null,
  token_id bigint not null,
  referrer varchar(255),
  ip varchar(15),
  creation_time timestamp without time zone default now() not null);

alter table lead_stat
add constraint lead_stat_pk primary key (id);

alter table lead_stat
add constraint lead_stat_token_id_fk
foreign key (token_id) references token(id);

end;
