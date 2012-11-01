select distinct
  shop_category.id id,
  offer_id,
  name,
  original_id,
  parent_id,
  is_direct
from shop_category

join product_category mapping
on mapping.product_id = :product_id
and mapping.shop_category_id = shop_category.id
