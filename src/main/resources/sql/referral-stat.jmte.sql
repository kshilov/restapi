select
  ${if !sumUp}
  referral.id,
  referral.email,
  referral.register_time,
  coalesce(referral.source, '') source,
  ${end}
  coalesce(sum(payed.amount), 0.0) amount

from user_profile referral

left join offer_action action
on action.aff_id = referral.id
and action.state = 1 /* CONFIRMED */

left join withdrawal withdrawal
on withdrawal.action_id = action.id

left join withdrawal_payment payed
on payed.withdrawal_id = withdrawal.id

where referral.referrer = :aff_id
       ${if filterBySource}
       and coalesce(referral.source, '') = :source
       ${end}

${if !sumUp}
group by referral.id, referral.email, referral.register_time, source
order by ${ordering} ${direction}
${end}

