select
  cashback.target_id    as "referral",
  cashback.referrer     as "referrer",
  action.last_change_time as "date"
from cashback

join offer_action action
on action.id = cashback.offer_action_id

where cashback.id in (
  select min(fst.id)
  from cashback fst

  join offer_action action
  on action.id = fst.offer_action_id
  and action.state = 1 /* CONFIRMED */

  where fst.aff_id = :aff_id
  group by fst.target_id)

and cashback.referrer is not null
