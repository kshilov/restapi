select
${if groupByUser}
  usr.id                as "user-id",
  usr.email             as "user-email",
  withdrawal.order_time as "order-time",
${end}
${if groupByOffer}
  offer.id              as "offer-id",
  offer.name            as "offer-name",
${end}
  coalesce(sum(withdrawal.amount),  0.00) as "ordered-amount",
  coalesce(sum(payed.amount),       0.00) as "payed-out-amount",
  coalesce(sum(withdrawal.amount),  0.00) -
  coalesce(sum(payed.amount),       0.00) as "pending-amount"

from withdrawal

${if groupByUser}
join user_profile usr
on usr.id = user_id
${end}

${if groupByOffer}
join offer
on offer.id = withdrawal.source_id
${end}

left join (
  select withdrawal_id, coalesce(sum(amount), 0.00) amount
  from withdrawal_payment
  group by withdrawal_id
) as payed
on payed.withdrawal_id = withdrawal.id

where
  withdrawal.order_time is not null
  and withdrawal.basis <> 'FEE'
  ${if filterByUser}
  and withdrawal.user_id = :user_id
  ${end}
  ${if filterByOrderTime}
  and withdrawal.order_time between :from and :to
  ${end}

${if groupByUser}
group by usr.id, usr.email, withdrawal.order_time
order by withdrawal.order_time desc
${end}

${if groupByOffer}
group by "offer-id", "offer-name"
order by "pending-amount" desc
${end}
