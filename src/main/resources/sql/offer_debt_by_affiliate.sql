select
  user_id,
  usr.email as user_email,
  sum(withdrawal.amount) - sum(payed_out) as debt_amount,
  sum(payed_out) as payed_out_amount,
  sum(case when withdrawal.order_time is null
      then 0.0
      else withdrawal.amount
      end) as ordered_amount

from withdrawal

join user_profile usr
on usr.id = user_id

where
  source_id = :offer_id
  and withdrawal.creation_time between :from and :to

group by user_id, user_email
order by debt_amount desc
