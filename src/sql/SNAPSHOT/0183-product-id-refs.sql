begin;
alter table offer_action
add column product_id bigint;

alter table offer_action
add constraint offer_action_product_fk
foreign key (product_id) references product(id);

alter table offer_stat
add column product_id bigint;

alter table offer_stat
add constraint offer_stat_product_fk
foreign key (product_id) references product(id);

end;

