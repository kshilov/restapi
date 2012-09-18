select

${if groupByUser}
  user_id as "user-id",
  usr.email as "user-email",
  withdrawal.basis as "basis",
${end}

${if groupByOffer}
  source_id as "offer-id",
  offer.name as "offer-name",
  withdrawal.basis as "basis",
${end}

  coalesce(sum(payed.amount),       0.00) as "payed-out-amount",
  coalesce(sum(withdrawal.amount),  0.00) -
  coalesce(sum(payed.amount),       0.00) as "debt-amount",
  coalesce(sum(withdrawal.amount),  0.00) as "income-amount",
  coalesce(sum(case when withdrawal.order_time is null
      then 0.00
      else withdrawal.amount
      end), 0.00) as "ordered-amount",
  coalesce(sum(case when withdrawal.order_time is null
      then 0.00
      else withdrawal.amount
      end), 0.00) - coalesce(sum(payed.amount), 0.00) as "pending-amount",
  coalesce(sum(case when withdrawal.order_time is null
      then withdrawal.amount
      else 0.00
      end), 0.00) as "available-for-order-amount"

from withdrawal

${if groupByUser}
join user_profile usr
on usr.id = user_id
${end}

${if groupByOffer}
join offer
on offer.id = withdrawal.source_id
${end}

left join withdrawal_payment payed
on payed.withdrawal_id = withdrawal.id

where
  withdrawal.creation_time between :from and :to

${if filterByOffer}
  and withdrawal.source_id = :offer_id
${end}

${if filterByAffiliate}
  and withdrawal.user_id = :aff_id
${end}

${if groupByUser}
group by user_id, usr.email, withdrawal.basis
${end}

${if groupByOffer}
group by source_id, offer.name, withdrawal.basis
${end}

order by
${if ordering}
  "${ordering}" ${direction},
${end}
"debt-amount" desc
