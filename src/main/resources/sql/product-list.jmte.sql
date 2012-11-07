select
${if productInfo}
  product.id                product_id,
  product.name              product_name,
  product.url               product_url,
  product.tariff_id         product_tariff_id,
  product.original_id       product_original_id,
  product.price             product_price,
  product.extra_info        product_extra_info,
  product.active            product_active,
  product.creation_time     product_creation_time,
  product.last_change_time  product_last_change_time,
  offer.id                  offer_id,
  offer.name                offer_name,
  offer.yml_url             offer_yml_url,
  tariff.id                 tariff_id,
  tariff.cpa_policy         tariff_cpa_policy,
  tariff.cost               tariff_cost,
  tariff.first_action_cost  tariff_first_action_cost,
  tariff.other_action_cost  tariff_other_action_cost,
  tariff.percent            tariff_percent
${end}

${if categoryInfo}
  distinct shop_category.*
${end}

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

left join tariff
on tariff.id = product.tariff_id

${if categoryInfo}
join product_category
on product_category.product_id = product.id

join shop_category
on shop_category.id = product_category.shop_category_id
${end}

where
product.active = true
${if filterByName}
and lower(product.name) like '%:query_string%'
${end}
${if filterByOfferListAndCategoryList}
and (
exists (select * from product_category
            where product_id = product.id
            and shop_category_id in (:category_list))
or
product.offer_id in (:offer_list))
${end}
${if filterByCategoryList}
and exists (select * from product_category 
            where product_id = product.id
            and shop_category_id in (:category_list)
            )
${end}
${if filterByOfferList}
and product.offer_id in (:offer_list)
${end}
