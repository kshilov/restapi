select * from product

join offer
on offer.id = product.offer_id
and offer.is_product_offer = true
and offer.active = true
and offer.approved = true

join offer_grant
on offer_grant.offer_id = product.offer_id
and offer_grant.blocked = false
and offer_grant.aff_id = :user_id

where 
1=1
${if filterByName}
and lower(product.name) like '%:query_string%'
${end}
${if categoryList}
and exists (select * from product_category 
            where product_id = product.id
            and category_id in (
              ${foreach categoryList cat}
                :category_id ${if !last_cat}, ${end}
              ${end}
              )
            )
${end}
${if offerList}
and product.offer_id in (
  ${foreach offerList offer}
    :offer_id ${if !last_offer}, ${end}
  ${end})
${end}
