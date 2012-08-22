select
  affiliate.id          id,
  affiliate.email       email,
  sum(withdraw.amount)  amount

from
  withdraw,
  user_profile affiliate

where
  withdraw.account_id = affiliate.affiliate_account_id
  and withdraw.done = true

group by affiliate.id, affiliate.email

order by amount desc
