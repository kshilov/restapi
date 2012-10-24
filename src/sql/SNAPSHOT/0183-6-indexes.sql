begin
create index if not exists product_offer_id_idx
on product(offer_id);

create index if not exists product_attribute_product_id_idx 
on product_attribute(product_id);
end

