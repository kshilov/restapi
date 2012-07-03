select
  *,
  case shows_count
    when 0 then null
    else clicks_count * 100.0 / shows_count
  end ctr,
  case clicks_count
    when 0 then null
    else (leads_count + sales_count) * 100.0 / clicks_count
  end cr,
  case clicks_count
    when 0 then null
    else (confirmed_revenue + not_confirmed_revenue) / clicks_count
  end ecpc,
  case shows_count
    when 0 then null
    else (confirmed_revenue + not_confirmed_revenue) * 1000.0 / shows_count
  end ecpm
from
  (
  select
    sum(show_count)               shows_count,
    sum(coalesce(click_count, 0)) clicks_count,
    sum(leads_count)              leads_count,
    sum(sales_count)              sales_count,
    sum(confirmed_revenue)        confirmed_revenue,
    sum(not_confirmed_revenue)    not_confirmed_revenue,
    sum(canceled_revenue)         canceled_revenue,
    /* + select + */
    o.id offer_id,
    o.name offer_name
  from
    offer o
  join
    offer_granted g
    on g.offer_id = o.id
  left join
    offer_stat
    on offer_stat.creation_time between :from and :to
    and o.id = offer_stat.master
    and g.aff_id = offer_stat.aff_id
  where
    o.parent_id is null
    and g.state = 'APPROVED'
  group by
    o.id, o.name
  ) as sums
order by
  clicks_count desc
offset :offset limit :limit
