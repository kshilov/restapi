select
${if grouped}
  usr.id                as "user-id",
  usr.email             as "user-email",
  withdrawal.order_time as "order-time",
${end}
  coalesce(sum(withdrawal.amount),  0.00) as "ordered-amount",
  coalesce(sum(payed.amount),       0.00) as "payed-out-amount",
  coalesce(sum(withdrawal.amount),  0.00) -
  coalesce(sum(payed.amount),       0.00) as "pending-amount"

from withdrawal

join user_profile usr
on usr.id = user_id

left join (
  select withdrawal_id, coalesce(sum(amount), 0.00) amount
  from withdrawal_payment
  group by withdrawal_id
) as payed
on payed.withdrawal_id = withdrawal.id

where
  withdrawal.order_time is not null
  and withdrawal.basis <> 'FEE'

${if grouped}
group by usr.id, usr.email, withdrawal.order_time
order by withdrawal.order_time desc
${end}
