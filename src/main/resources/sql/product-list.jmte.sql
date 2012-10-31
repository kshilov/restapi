select 
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
  tariff.id                 tariff_id,
  tariff.cpa_policy         tariff_cpa_policy,
  tariff.cost               tariff_cost,
  tariff.first_action_cost  tariff_first_action_cost,
  tariff.other_action_cost  tariff_other_action_cost,
  tariff.percent            tariff_percent

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
offset :offset limit :limit
