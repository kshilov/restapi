select
 withdraw.id, withdraw.amount, withdraw.timestamp, withdraw.done,
 user_profile.id as aff_id,
 user_profile.email as aff_email
from withdraw
join user_profile
  on withdraw.account_id=user_profile.affiliate_account_id
order by withdraw.timestamp desc
offset :offset
limit :limit