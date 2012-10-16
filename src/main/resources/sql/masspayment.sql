select
  usr.wmr           as "Destination",
  sum(payed.amount) as "Amount",
  'Выплата вознаграждения' ||
  ' партнёру ' || usr.email ||
  ' по офферу "' || offer.id || ' - ' || offer.name || '"'  as "Description",
  usr.id || '_' || offer.id || '_' || withdrawal.basis      as "Id"

from withdrawal

join withdrawal_payment payed
on payed.creation_time between :from and :to
and payed.withdrawal_id = withdrawal.id

join offer
on offer.id = withdrawal.source_id

join user_profile usr
on usr.id = withdrawal.user_id

where withdrawal.basis = 'AFFILIATE_REVENUE'

group by usr.wmr, usr.id, usr.email, offer.id, offer.name, withdrawal.basis

union

select
  usr.wmr           as "Destination",
  sum(payed.amount) as "Amount",
  'Выплата по реферальной программе' ||
  ' партнёру ' || usr.email         as "Description",
  usr.id || '_' || withdrawal.basis as "Id"

from withdrawal

join withdrawal_payment payed
on payed.creation_time between :from and :to
and payed.withdrawal_id = withdrawal.id

join user_profile usr
on usr.id = withdrawal.user_id

where withdrawal.basis = 'MLM'

group by usr.wmr, usr.id, usr.email, withdrawal.basis

