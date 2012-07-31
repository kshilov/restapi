select
  *,
  case shows_count
    when 0 then 0.00
    else clicks_count * 100.0 / shows_count
  end ctr,
  case clicks_count
    when 0 then 0.00
    else (leads_count + sales_count) * 100.0 / clicks_count
  end cr,
  case clicks_count
    when 0 then 0.00
    else (confirmed_revenue + not_confirmed_revenue) / clicks_count
  end ecpc,
  case shows_count
    when 0 then 0.00
    else (confirmed_revenue + not_confirmed_revenue) * 1000.0 / shows_count
  end ecpm
from
  (
  select
    coalesce(sum(show_count),   0) shows_count,
    coalesce(sum(click_count),  0) clicks_count,
    coalesce(sum(leads_count),  0) leads_count,
    coalesce(sum(sales_count),  0) sales_count,
    ${if addFee}
      coalesce(sum(confirmed_revenue      + confirmed_fee),     0.00) confirmed_revenue,
      coalesce(sum(not_confirmed_revenue  + not_confirmed_fee), 0.00) not_confirmed_revenue,
      coalesce(sum(canceled_revenue       + canceled_fee),      0.00) canceled_revenue,
    ${else}
      coalesce(sum(confirmed_revenue),      0.00) confirmed_revenue,
      coalesce(sum(not_confirmed_revenue),  0.00) not_confirmed_revenue,
      coalesce(sum(canceled_revenue),       0.00) canceled_revenue,
    ${end}

    ${if groupByOffer}
      o.id id, o.name descr
    ${end}

    ${if groupByAffiliate}
      offer_stat.aff_id id, p.email descr
    ${end}

    ${if groupByAdvertiser}
      p.id id, p.email || ' (' || coalesce(p.organization, '--') || ')' descr
    ${end}

    ${if groupBySourceId}
      0 id, offer_stat.source_id descr
    ${end}

    ${if groupBySub}
      0 id,
      ${foreach groupBySub sub}
        ${if last_sub}
          coalesce(offer_stat.${sub}, '') descr
        ${else}
          coalesce(offer_stat.${sub}, '') || ' / ' ||
        ${end}
      ${end}
    ${end}

    ${if groupByReferer}
      0 id, offer_stat.referer descr
    ${end}

    ${if groupByKeywords}
      0 id, offer_stat.keywords descr
    ${end}

    ${if groupBySubOffer}
      o.id id, o.title descr
    ${end}

  from
    offer o

  ${if granted}
    join offer_grant g on g.offer_id = o.id
  ${end}

  join
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
    ${if filterBySub}
      ${foreach filterBySub sub}
        and offer_stat.${sub} = :${sub}
      ${end}
    ${end}
    ${if filterByParentOffer}
      and offer_stat.master = :parent_offer
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

    ${if groupBySourceId}
      offer_stat.source_id
    ${end}


    ${if groupBySub}
      ${foreach groupBySub sub}
        ${if last_sub}
          ${sub}
        ${else}
          ${sub},
        ${end}
      ${end}
    ${end}

    ${if groupByReferer}
      offer_stat.referer
    ${end}

    ${if groupByKeywords}
     offer_stat.keywords
    ${end}

    ${if groupBySubOffer}
      o.id, o.title
    ${end}

  ) as sums

order by
  ${ordering} ${direction}

offset :offset limit :limit
