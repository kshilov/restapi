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
    ${if groupByAdvertiser}
      sum(confirmed_revenue  * (1 + p_aff.fee / 100.0))        confirmed_revenue,
      sum(not_confirmed_revenue  * (1 + p_aff.fee / 100.0))    not_confirmed_revenue,
      sum(canceled_revenue  * (1 + p_aff.fee / 100.0))         canceled_revenue,
    ${else}
      sum(confirmed_revenue)        confirmed_revenue,
      sum(not_confirmed_revenue)    not_confirmed_revenue,
      sum(canceled_revenue)         canceled_revenue,
    ${end}

    ${if groupByOffer}
      o.id offer_id, o.name offer_name
    ${end}

    ${if groupByAffiliate}
      offer_stat.aff_id, p.email
    ${end}

    ${if groupByAdvertiser}
      p.id a8, p.email || ' (' || coalesce(p.organization, '--') || ')' a9
    ${end}

  from
    offer o

  ${if granted}
    join offer_grant g on g.offer_id = o.id
  ${end}

  left join
    offer_stat
    on offer_stat.creation_time between :from and :to
    and o.id = offer_stat.master
    ${if granted}
      and g.aff_id = offer_stat.aff_id
    ${end}

  ${if groupByAffiliate}
    left join user_profile p
    on p.id = offer_stat.aff_id
  ${end}

  ${if groupByAdvertiser}
    left join user_profile p on o.user_id = p.id
    left join user_profile p_aff on offer_stat.aff_id = p_aff.id
  ${end}


  where
    o.parent_id is null
    ${if granted}
      and g.state = 'APPROVED'
    ${end}
    ${if filterByAffiliate}
      and offer_stat.aff_id = :aff_id
    ${end}
    ${if filterByAdvertiser}
     and o.user_id = :adv_id
    ${end}
    ${if filterByOffer}
      and o.id = :offer_id
    ${end}

  group by
    ${if groupByOffer}
      o.id, o.name
    ${end}

    ${if groupByAffiliate}
      offer_stat.aff_id, p.email
    ${end}

    ${if groupByAdvertiser}
      p.id, p.email, p.organization
    ${end}

  ) as sums

order by
  clicks_count desc

offset :offset limit :limit
