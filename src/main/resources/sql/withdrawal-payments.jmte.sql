select * from (
select
  usr.id            as affiliate_id,
  usr.email         as affiliate_email,
  usr.wmr           as affiliate_wmr,
  offer.id          as offer_id,
  offer.name        as offer_name,
  withdrawal.basis  as basis,
  sum(payed.amount) as amount,
  case when sum(payed.amount) > 15
    then 'AUTO'
    else 'MANUAL'
  end               as pay_method

from withdrawal

join withdrawal_payment payed
on payed.creation_time between :from and :to
and payed.withdrawal_id = withdrawal.id

join offer
on offer.id = withdrawal.source_id

join user_profile usr
on usr.id = withdrawal.user_id

where withdrawal.basis = 'AFFILIATE_REVENUE'
and usr.wmr is not null

group by usr.wmr, usr.id, usr.email, offer.id, offer.name, withdrawal.basis

union

select
  usr.id            as affiliate_id,
  usr.email         as affiliate_email,
  usr.wmr           as affiliate_wmr,
  null              as offer_id,
  null              as offer_name,
  withdrawal.basis  as basis,
  sum(payed.amount) as amount,
  case when sum(payed.amount) > 15
    then 'AUTO'
    else 'MANUAL'
  end               as pay_method

from withdrawal

join withdrawal_payment payed
on payed.creation_time between :from and :to
and payed.withdrawal_id = withdrawal.id

join user_profile usr
on usr.id = withdrawal.user_id

where withdrawal.basis = 'MLM'
and usr.wmr is not null

group by usr.wmr, usr.id, usr.email, withdrawal.basis
) as t

${if filterByPayMethod}
where pay_method = :pay_method
${end}
order by ${ordering} ${direction}
