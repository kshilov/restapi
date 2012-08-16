select
  coalesce(sum(leads_count),  0) leads_count,
  coalesce(sum(sales_count),  0) sales_count,
${if addFee}
  coalesce(sum(confirmed_revenue      + confirmed_fee),     0.00) confirmed_revenue,
  coalesce(sum(not_confirmed_revenue  + not_confirmed_fee), 0.00) not_confirmed_revenue,
  coalesce(sum(canceled_revenue       + canceled_fee),      0.00) canceled_revenue,
${else}
  coalesce(sum(confirmed_revenue),      0.00) confirmed_revenue,
  coalesce(sum(not_confirmed_revenue),  0.00) not_confirmed_revenue,
  coalesce(sum(canceled_revenue),       0.00) canceled_revenue,
${end}
  o.id id, coalesce(parent.name, o.name) || ' — ' || o.title descr, o.exclusive

from offer o

join offer_stat
on offer_stat.creation_time between :from and :to
and o.id = offer_stat.offer_id

left join offer parent
on parent.id = o.parent_id

where
  offer_stat.leads_count + offer_stat.sales_count > 0
${if filterByAffId}
  and offer_stat.aff_id = :aff_id
${end}
${if filterBySourceId}
  and coalesce(offer_stat.source_id, '') = coalesce(:source_id, '')
${end}
${if filterByParentId}
  and (o.parent_id = :parent_id or o.id = :parent_id)
  and offer_stat.master = :parent_id
${end}
${if filterByAdvId}
  and coalesce(parent.user_id, o.user_id) = :adv_id
${end}
${foreach filterBySubId sub}
  and coalesce(offer_stat.${sub}, '') = coalesce(:${sub}, '')
${end}
${if emptySubIdsOnly}
  and offer_stat.sub_id   is null
  and offer_stat.sub_id1  is null
  and offer_stat.sub_id2  is null
  and offer_stat.sub_id3  is null
  and offer_stat.sub_id4  is null
${end}
${if filterByReferer}
  and coalesce(offer_stat.referer, '') = coalesce(:referer, '')
${end}
${if filterByKeywords}
  and coalesce(offer_stat.keywords, '') = coalesce(:keywords, '')
${end}

group by o.id, o.title, parent.name, o.name, o.exclusive

order by ${ordering} ${direction}
offset :offset limit :limit
