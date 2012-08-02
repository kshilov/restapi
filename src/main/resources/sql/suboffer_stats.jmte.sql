select
  coalesce(sum(leads_count),  0) leads_count,
  coalesce(sum(sales_count),  0) sales_count,
  coalesce(sum(confirmed_revenue),      0.00) confirmed_revenue,
  coalesce(sum(not_confirmed_revenue),  0.00) not_confirmed_revenue,
  coalesce(sum(canceled_revenue),       0.00) canceled_revenue,
  o.id id, o.title descr, o.exclusive

from offer o

join offer_stat
on offer_stat.creation_time between :from and :to
and o.id = offer_stat.offer_id

where
  offer_stat.aff_id = :aff_id
  and offer_stat.leads_count + offer_stat.sales_count > 0
${if filterBySourceId}
  and offer_stat.source_id = :source_id
${end}
${if filterByParentId}
  and (o.parent_id = :parent_id or o.id = :parent_id)
  and offer_stat.master = :parent_id
${end}
${foreach filterBySubId sub}
  and offer_stat.${sub} = :${sub}
${end}
${if filterByReferer}
  and offer_stat.referer = :referer
${end}

group by o.id, o.title, o.exclusive

order by ${ordering} ${direction}
offset :offset limit :limit
