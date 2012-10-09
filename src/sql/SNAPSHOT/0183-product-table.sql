begin;
drop table if exists product;
drop table if exists shop_category;
drop table if exists product_attribute;

drop sequence if exists product_seq;
drop sequence if exists shop_category_seq;
drop sequence if exists product_attribute_seq;

create sequence product_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table product(
  id bigint default nextval('product_seq'),
  category_original_id varchar(32),
  offer_id bigint not null,
  name varchar(255) not null,
  url varchar(255) not null,
  original_id varchar(32) not null,
  price numeric(19,2),
  creation_time timestamp without time zone default now() not null,
  last_change_time timestamp without time zone default now() not null);

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
  value varchar(1000) not null);

create sequence shop_category_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table shop_category(
  id bigint default nextval('product_category_seq'),
  offer_id bigint not null,
  original_id varchar(32) not null,
  name varchar(255) not null,
  parent_original_id varchar(32));

end;
