create sequence topshop_product_seq
    start with 1
    increment by 1
    no maxvalue
    no minvalue
    cache 1;

create table topshop_product (
  id bigint not null default nextval('topshop_product_seq'::regclass),
  topshop_id character varying(255) not null,
  offer_id bigint references offer(id),
  price numeric(19,2)
);

alter table topshop_product
  add constraint topshop_product_pk primary key(id);
