select
  affiliate_id,
  affiliate.email affiliate_email,
  sum(approved)   approved,
  sum(canceled)   canceled,
  sum(not_confirmed) not_confirmed,
  case sum(approved + canceled)
    when 0 then 0.0
    else sum(canceled) * 100.0 / sum(approved + canceled)
  end rate,
  sum(clicks_count)   clicks_count,
  sum(actions_count)  actions_count,
  case sum(clicks_count)
    when 0 then 0.00
    else sum(actions_count) * 100.0 / sum(clicks_count)
  end as "conversion"

from (
  select
    aff_id    affiliate_id,
    count(offer_action.id) approved,
    0          canceled,
    0          not_confirmed,
    0          clicks_count,
    0          actions_count
  from offer_action
  ${if filterByOffer}
  join offer
  on  coalesce(offer.parent_id, offer.id) = offer_action.offer_id
  and coalesce(offer.parent_id, offer.id) = :offer_id
  ${end}
  where
    state = 1 /* APPROVED */
    and offer_action.creation_time between :from and :to
  group by aff_id

  union

  select
    aff_id    affiliate_id,
    0         approved,
    count(offer_action.id) canceled,
    0          not_confirmed,
    0         clicks_count,
    0         actions_count
  from offer_action
  ${if filterByOffer}
  join offer
  on  coalesce(offer.parent_id, offer.id) = offer_action.offer_id
  and coalesce(offer.parent_id, offer.id) = :offer_id
  ${end}
  where
    state = 2 /* CANCELED */
    and offer_action.creation_time between :from and :to
  group by aff_id

  union

  select
    aff_id    affiliate_id,
    0         approved,
    0         canceled,
    count(offer_action.id) not_confirmed,
    0         clicks_count,
    0         actions_count
  from offer_action
  ${if filterByOffer}
  join offer
  on  coalesce(offer.parent_id, offer.id) = offer_action.offer_id
  and coalesce(offer.parent_id, offer.id) = :offer_id
  ${end}
  where
    state = 0 /* CREATED */
    and offer_action.creation_time between :from and :to
  group by aff_id

  union

  select
    aff_id  affiliate_id,
    0       approved,
    0       canceled,
    0       not_confirmed,
    coalesce(sum(click_count), 0)              clicks_count,
    coalesce(sum(sales_count + leads_count), 0) actions_count
  from offer_stat
  ${if filterByOffer}
  join offer
  on  offer.id = offer_stat.master
  and offer.id = :offer_id
  ${end}
  where offer_stat.creation_time between :from and :to
  group by aff_id

) a

join user_profile affiliate
on affiliate.id = affiliate_id
${if activeOnly}
and affiliate.blocked = false
${end}

group by affiliate_id, affiliate_email
having sum(actions_count) > 5
order by ${ordering} ${direction}, canceled desc, approved asc, affiliate_id
