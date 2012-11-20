begin;

drop table if exists site_category;
drop table if exists site_region;

drop table if exists offer_site;
drop sequence if exists offer_site_seq;

drop table if exists site_attribute;
drop sequence if exists site_attribute_seq;

drop table if exists site;
drop sequence if exists site_seq;


create sequence site_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table site(
  id bigint default nextval('site_seq') primary key,
  aff_id bigint not null,
  type varchar(20) not null,
  description text not null,
  approved boolean not null default false,
  creation_time timestamp without time zone default now(),
  last_change_time timestamp without time zone default now(),
  foreign key (aff_id) references user_profile(id)
);


create sequence site_attribute_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table site_attribute(
  id bigint default nextval('site_attribute_seq') primary key,
  site_id bigint not null,
  key varchar(255) not null,
  value varchar(255) not null,
  foreign key (site_id) references site (id)
);

create sequence offer_site_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table offer_site(
  id bigint default nextval('offer_site_seq') primary key,
  offer_id bigint not null,
  site_id bigint not null,
  approved boolean not null default false,
  creation_time timestamp without time zone default now(),
  last_change_time timestamp without time zone default now(),
  foreign key (offer_id) references offer(id),
  foreign key (site_id) references site(id)
);

end;
