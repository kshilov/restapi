create index product_offer_id_idx
on product(offer_id);

create index product_attribute_product_id_idx 
on product_attribute(product_id);

create index shop_category_offer_id_idx
on shop_category(offer_id);

create index product_category_product_id_idx
on product_category(product_id);
