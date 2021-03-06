begin;

drop table if exists site_category;
drop table if exists site_region;

drop table if exists offer_site;
drop sequence if exists offer_site_seq;

drop table if exists placement cascade;
drop sequence if exists placement_seq;

drop table if exists site_attribute;
drop sequence if exists site_attribute_seq;

drop table if exists site cascade;
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
  name text not null,
  description text,
  admin_state varchar(20) not null,
  admin_comment text,
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
  value text not null,
  foreign key (site_id) references site (id)
);

create sequence placement_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table placement(
  id bigint default nextval('placement_seq') primary key,
  offer_id bigint not null,
  site_id bigint not null,
  admin_state varchar(20) not null,
  admin_comment text,
  back_url text,
  postback_url text,
  creation_time timestamp without time zone default now(),
  last_change_time timestamp without time zone default now(),
  foreign key (offer_id) references offer(id),
  foreign key (site_id) references site(id)
);

end;
begin;
create index site_aff_id_idx on site(aff_id);
create index site_attribute_site_id_idx on site_attribute(site_id);
create index placement_site_id_offer_id_idx on placement(site_id, offer_id);
end;

begin;
grant select on site to dumper;
grant select on site_seq to dumper;
grant select on site_attribute to dumper;
grant select on site_attribute_seq to dumper;
grant select on placement to dumper;
grant select on placement_seq to dumper;
end;
