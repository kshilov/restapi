select
${if grouped}
  withdrawal.user_id    as "user-id",
  usr.email             as "user-email",
  usr.wmr               as "user-wmr",

${if forAffiliate}
  case withdrawal.basis
  when 'AFFILIATE_REVENUE'  then withdrawal.source_id
  when 'MLM'                  then null
  end                   as "offer-id",
  case withdrawal.basis
  when 'AFFILIATE_REVENUE'  then offer.name
  when 'MLM'                  then null
  end                   as "offer-name",
${else}
  withdrawal.source_id  as "offer-id",
  offer.name            as "offer-name",
${end}

  withdrawal.basis      as "basis",
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

join user_profile usr
on usr.id = user_id

join offer
on offer.id = withdrawal.source_id

left join (select withdrawal_id, coalesce(sum(amount), 0.00) amount
            from withdrawal_payment
            group by withdrawal_id) as payed
on payed.withdrawal_id = withdrawal.id

where
  withdrawal.creation_time between :from and :to

${if filterByOffer}
  and withdrawal.source_id = :offer_id
${end}

${if filterByAffiliate}
  and withdrawal.user_id = :aff_id
${end}

${if grouped}
group by withdrawal.user_id, usr.email, usr.wmr,
"offer-id", "offer-name", withdrawal.basis
${end}

order by
${if ordering}
  "${ordering}" ${direction},
${end}
"debt-amount" desc
