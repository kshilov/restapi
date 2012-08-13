select sum(coalesce(entry.amount, 0.00) +
            coalesce(canceled.amount, 0.00)) amount

from accounting_entry entry

left join offer_action action
on action.id = entry.source_id

left join accounting_entry canceled
on canceled.source_id = entry.id
and canceled.event = 8

${if affiliateConfirmedMoney}
join user_profile affiliate
on affiliate.id = action.aff_id
and affiliate.affiliate_account_id = entry.account_id
${end}

${if affiliateNotConfirmedMoney}
join user_profile affiliate
on affiliate.id = action.aff_id
and affiliate.affiliate_account_not_confirmed_id = entry.account_id
${end}

${if adminConfirmedMoney}
join admin_account admin
on admin.account_id = entry.account_id
${end}

${if adminNotConfirmedMoney}
join admin_account_not_confirmed admin
on admin.account_id = entry.account_id
${end}

${if onlyExpiredActions}
join offer
on offer.id = action.offer_id
${end}

where
  action.creation_time between :from and :to
${if onlyExpiredActions}
  and action.state = 0
  and entry.event = 1
  and cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
${else}
  and action.state = :action_state
  and entry.event = :entry_event
${end}
