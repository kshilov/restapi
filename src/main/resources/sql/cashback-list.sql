select
  cashback.target_id,
  offer.id            as offer_id,
  offer.name          as offer_name,
  action.last_change_time as "date",
  stat.confirmed_revenue  as "affiliate_revenue"

from cashback

join offer_action action
on action.id = cashback.offer_action_id

join offer sub_offer
on sub_offer.id = action.offer_id

join offer
on offer.id = coalesce(sub_offer.parent_id, sub_offer.id)

join offer_stat stat
on stat.id = action.stat_id

where action.state = 1  /* CONFIRMED */
and cashback.aff_id = :aff_id

order by cashback.target_id asc
