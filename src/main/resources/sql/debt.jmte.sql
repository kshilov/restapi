select

${if groupByUser}
  user_id as "user-id",
  usr.email as "user-email",
${end}

${if groupByOffer}
  source_id as "offer-id",
  offer.name as "offer-name",
${end}

  withdrawal.basis as "basis",
  coalesce(sum(payed.amount), 0.0) as "payed-out-amount",
  sum(withdrawal.amount) - coalesce(sum(payed.amount), 0.0) as "debt-amount",
  sum(withdrawal.amount) as "income-amount",
  sum(case when withdrawal.order_time is null
      then 0.0
      else withdrawal.amount
      end) as "ordered-amount"

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

${if groupByUser}
  and withdrawal.source_id = :offer_id
${end}

${if groupByOffer}
  and withdrawal.user_id = :aff_id
${end}

group by withdrawal.basis,
${if groupByUser} user_id, usr.email ${end}
${if groupByOffer} source_id, offer.name ${end}

order by "debt-amount" desc
