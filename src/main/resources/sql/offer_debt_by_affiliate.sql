select
  user_id as "user-id",
  usr.email as "user-email",
  coalesce(sum(payed.amount), 0.0) as "payed-out-amount",
  sum(withdrawal.amount) - coalesce(sum(payed.amount), 0.0) as "debt-amount",
  sum(withdrawal.amount) as "income-amount",
  sum(case when withdrawal.order_time is null
      then 0.0
      else withdrawal.amount
      end) as "ordered-amount"

from withdrawal

join user_profile usr
on usr.id = user_id

left join withdrawal_payment payed
on payed.withdrawal_id = withdrawal.id

where
  source_id = :offer_id
  and withdrawal.creation_time between :from and :to

group by user_id, usr.email
order by "debt-amount" desc
