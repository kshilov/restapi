alter table offer
add column item_price numeric(19,2);

update offer
set item_price = cost
where cpa_policy = 'PERCENT' and cost is not null;
