begin;

drop table if exists cashback;
drop sequence if exists cashback_seq;

alter table offer_stat
add column cashback_target_id varchar(255);

alter table offer_stat
add column cashback_referrer varchar(255);

alter table offer
add column allow_cashback boolean not null default false;

create sequence cashback_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table cashback(
  id bigint not null default nextval('cashback_seq'),
  target_id varchar(255) not null,
  referrer varchar(255),
  aff_id bigint not null,
  offer_action_id bigint not null);

alter table cashback add constraint cashback_pk primary key (id);

alter table cashback add constraint cashback_aff_id_fk
foreign key (aff_id) references user_profile(id);

alter table cashback add constraint cashback_offer_action_fk
foreign key (offer_action_id) references offer_action(id);

end;
