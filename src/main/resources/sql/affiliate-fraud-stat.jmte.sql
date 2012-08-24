select
  affiliate_id,
  affiliate.email affiliate_email,
  sum(approved)   approved,
  sum(canceled)   canceled,
  case sum(approved + canceled)
    when 0 then 0.0
    else sum(canceled) * 100.0 / sum(approved + canceled)
  end rate

from (
  select
    aff_id    affiliate_id,
    count(id) approved,
    0          canceled

  from offer_action

  where
    state = 1 /* APPROVED */
    and creation_time between :from and :to

  group by aff_id

  union

  select
    aff_id    affiliate_id,
    0         approved,
    count(id) canceled

  from offer_action

  where
    state = 2 /* CANCELED */
    and creation_time between :from and :to

  group by aff_id
) a

join user_profile affiliate
on affiliate.id = affiliate_id
${if activeOnly}
and affiliate.blocked = false
${end}

group by affiliate_id, affiliate_email
order by ${ordering} ${direction}, canceled desc, approved asc, affiliate_id

offset :offset limit :limit
