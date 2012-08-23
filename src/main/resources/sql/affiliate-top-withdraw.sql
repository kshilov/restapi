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
  and affiliate.blocked = false
  and withdraw.timestamp  between now() - interval '2 month'
                          and now() - interval '1 month'

group by affiliate.id, affiliate.email

order by amount desc
