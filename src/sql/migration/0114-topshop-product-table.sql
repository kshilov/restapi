create table topshop_product (
  id bigint not null,
  offer_id bigint references offer(id),
  price numeric(19,2) not null
);

alter table topshop_product
  add constraint topshop_product_pk primary key(id);
