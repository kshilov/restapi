select
  coalesce(sum(entry_fee.amount), 0.00) fee,
  coalesce(sum(entry_aff.amount), 0.00) affiliate,
  coalesce(sum(entry_fee.amount), 0.00) + coalesce(sum(entry_aff.amount), 0.00) total
from offer_action action

left join admin_account_not_confirmed fee_acc
on fee_acc.account_id is not null

left join user_profile affiliate
on affiliate.id = action.aff_id

left join accounting_entry entry_fee
on entry_fee.source_id = action.id
and entry_fee.event = :entry_event
and entry_fee.account_id = fee_acc.account_id

left join accounting_entry canceled_fee
on canceled_fee.source_id = entry_fee.id
and canceled_fee.account_id = affiliate.affiliate_account_not_confirmed_id
and canceled_fee.event = 8 /* CANCELED TRANSACTIONS */

left join accounting_entry entry_aff
on entry_aff.source_id = action.id
and entry_aff.event = :entry_event
and entry_aff.account_id = affiliate.affiliate_account_not_confirmed_id

left join accounting_entry canceled_aff
on canceled_aff.source_id = entry_aff.id
and canceled_aff.account_id = affiliate.affiliate_account_not_confirmed_id
and canceled_aff.event = 8 /* CANCELED TRANSACTIONS */

where
  action.creation_time between :from and :to
  and action.state = :action_state
  and canceled_fee.source_id is null
  and canceled_aff.source_id is null

