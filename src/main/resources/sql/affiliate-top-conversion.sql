select
  id,
  email,
  ((leads_count + sales_count) * 100.0 / clicks_count) conversion_rate
from (
  select
    affiliate.id    id,
    affiliate.email email,
    coalesce(sum(click_count),  0) clicks_count,
    coalesce(sum(leads_count),  0) leads_count,
    coalesce(sum(sales_count),  0) sales_count,
    coalesce(sum(not_confirmed_revenue), 0)   not_confirmed,
    coalesce(sum(confirmed_revenue), 0)       confirmed,
    coalesce(sum(canceled_revenue), 0)        canceled

  from
    offer_stat,
    user_profile affiliate

  where
    affiliate.id = offer_stat.aff_id
    and affiliate.blocked = false
    and offer_stat.creation_time  between now() - interval '2 month'
                                  and now() - interval '1 month'

  group by affiliate.id, affiliate.email
  ) a

where
  leads_count + sales_count > 10
  and clicks_count > 1000
  and confirmed > canceled + not_confirmed
order by conversion_rate desc

