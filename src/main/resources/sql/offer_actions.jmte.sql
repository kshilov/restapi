select
  action.id,
  action.creation_time,
  action.last_change_time,
  action.state,
  action.transaction_id,
  coalesce(stat.not_confirmed_revenue,  0.00) +
  coalesce(stat.not_confirmed_fee,      0.00) +
  coalesce(stat.confirmed_revenue,      0.00) +
  coalesce(stat.confirmed_fee,          0.00) +
  coalesce(stat.canceled_revenue,       0.00) +
  coalesce(stat.canceled_fee,           0.00) amount,
  offer.id        offer_id,
  offer.title     offer_title,
  offer.code      offer_code,
  affiliate.id    affiliate_id,
  affiliate.email affiliate_email
from
  offer_action action

join offer
on offer.id = action.offer_id

left join user_profile affiliate
on affiliate.id = action.aff_id

left join offer_stat stat
on stat.id = action.stat_id

where
  (offer.id = :offer_id or offer.parent_id = :offer_id)
${if filterByCreationTime}
  and action.creation_time between :from and :to
${end}
${if filterByLastChangeTime}
  and action.last_change_time between :from and :to
${end}
${if filterByState}
  and action.state = :state
${end}

order by
  ${ordering} ${direction}
