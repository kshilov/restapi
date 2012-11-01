select distinct shop_category.*
from product

join offer
on offer.id = product.offer_id
and offer.is_product_offer = true
and offer.active = true
and offer.approved = true

join offer_grant
on offer_grant.offer_id = product.offer_id
and offer_grant.blocked = false
and offer_grant.aff_id = :user_id

join product_category
on product_category.product_id = product.id

join shop_category
on shop_category.id = product_category.shop_category_id

where
product.active = true
${if filterByName}
and lower(product.name) like '%:query_string%'
${end}
${if filterByCategoryList}
and exists (select * from product_category
            where product_id = product.id
            and category_id in (:category_list)
            )
${end}
${if filterByOfferList}
and product.offer_id in (:offer_list)
${end}

