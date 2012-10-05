select
  referral.id,
  referral.email,
  ${if !filterBySource}
  coalesce(referral.source, '') source,
  ${end}
  coalesce(sum(payed.amount), 0.0) amount

from user_profile referral

join offer_action action
on action.aff_id = referral.id
and action.state = 1 /* CONFIRMED */

join withdrawal withdrawal
on withdrawal.action_id = action.id

join withdrawal_payment payed
on payed.withdrawal_id = withdrawal.id

where referral.referrer = :aff_id
       ${if filterBySource}
       and referral.source = :source
       ${end}

group by referral.id, referral.email ${if !filterBySource} , source ${end}
order by ${ordering} ${direction}

