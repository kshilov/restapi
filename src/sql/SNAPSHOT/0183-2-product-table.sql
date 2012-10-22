begin;
drop table if exists product_attribute;
drop table if exists product cascade;
drop table if exists shop_category;

drop sequence if exists product_seq;
drop sequence if exists shop_category_seq;
drop sequence if exists product_attribute_seq;

create sequence shop_category_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table shop_category(
  id bigint default nextval('shop_category_seq'),
  offer_id bigint not null,
  original_id varchar(32) not null,
  name varchar(255) not null,
  parent_id bigint);

alter table shop_category add constraint shop_category_pk
primary key (id);

alter table shop_category add constraint shop_category_offer_fk
foreign key (offer_id) references offer(id) on update cascade;

alter table shop_category add constraint shop_category_parent_fk
foreign key (parent_id) references shop_category(id) on update cascade;

create sequence product_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table product(
  id bigint default nextval('product_seq'),
  shop_category_id bigint,
  offer_id bigint not null,
  tariff_id bigint,
  name varchar(255) not null,
  url varchar(255) not null,
  original_id varchar(32) not null,
  price numeric(19,2),
  exclusive boolean not null,
  creation_time timestamp without time zone default now() not null,
  last_change_time timestamp without time zone default now() not null);

alter table product add constraint product_pk
primary key (id);

alter table product add constraint product_offer_fk
foreign key (offer_id) references offer(id) on update cascade;

alter table product add constraint product_shop_category_fk
foreign key (shop_category_id) references shop_category(id)
on update cascade;

create sequence product_attribute_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table product_attribute(
  id bigint default nextval('product_attribute_seq'),
  product_id bigint not null,
  key varchar(255) not null,
  value varchar(1000) not null,
  extra_info varchar(255));

alter table product_attribute add constraint product_attribute_pk
primary key (id);

alter table product_attribute add constraint product_attribute_product_fk
foreign key (product_id) references product(id)
on update cascade on delete cascade;

end;
