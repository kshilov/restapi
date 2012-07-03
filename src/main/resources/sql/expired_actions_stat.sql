select
  sum(aff_entry.amount)   as affiliate_sum,
  sum(admin_entry.amount) as fee_sum,
  sum(aff_entry.amount) + sum(admin_entry.amount) as total
from
  offer_action action

left join offer
on offer.id = action.offer_id

left join user_profile affiliate
on affiliate.id = action.aff_id

left join admin_account_not_confirmed admin_not_confirmed
on admin_not_confirmed.id is not null

left join accounting_entry aff_entry
on
  aff_entry.source_id = action.id
  and aff_entry.event = 1
  and aff_entry.account_id = affiliate.affiliate_account_not_confirmed_id

left join accounting_entry admin_entry
on
  admin_entry.source_id = action.id
  and admin_entry.event = 1
  and admin_entry.account_id = admin_not_confirmed.account_id

where
  action.state = 0
  and cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
  and action.creation_time between :from and :to
