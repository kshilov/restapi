begin;
create index product_offer_id_idx
on product(offer_id);

create index product_attribute_product_id_idx 
on product_attribute(product_id);
end;

