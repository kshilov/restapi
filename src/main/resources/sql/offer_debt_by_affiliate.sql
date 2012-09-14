select
  user_id,
  usr.email as user_email,
  coalesce(sum(payed.amount), 0.0) as payed_out_amount,
  sum(withdrawal.amount) - coalesce(sum(payed.amount), 0.0) as debt_amount,
  sum(withdrawal.amount) as income_amount,
  sum(case when withdrawal.order_time is null
      then 0.0
      else withdrawal.amount
      end) as ordered_amount

from withdrawal

join user_profile usr
on usr.id = user_id

left join withdrawal_payment payed
on payed.withdrawal_id = withdrawal.id

where
  source_id = :offer_id
  and withdrawal.creation_time between :from and :to

group by user_id, user_email
order by debt_amount desc
