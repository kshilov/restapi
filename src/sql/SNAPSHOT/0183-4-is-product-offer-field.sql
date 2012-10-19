begin;
alter table offer
add column is_product_offer boolean not null default false;

update offer
set is_product_offer = true where exclusive = true;

end;
