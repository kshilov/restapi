select
  *,
  case shows
    when 0 then 0.00
    else clicks * 100.0 / shows
  end ctr,
  case clicks
    when 0 then 0.00
    else (leads + sales) * 100.0 / clicks
  end cr,
  case clicks
    when 0 then 0.00
    else ("confirmed-revenue" + "not-confirmed-revenue") / clicks
  end ecpc,
  case shows
    when 0 then 0.00
    else ("confirmed-revenue" + "not-confirmed-revenue") * 1000.0 / shows
  end ecpm
from
  (
  select
    coalesce(sum(show_count),   0) shows,
    coalesce(sum(click_count),  0) clicks,
    coalesce(sum(leads_count),  0) leads,
    coalesce(sum(sales_count),  0) sales,
    ${if addFee}
      coalesce(sum(confirmed_revenue      + confirmed_fee),     0.00) as "confirmed-revenue",
      coalesce(sum(not_confirmed_revenue  + not_confirmed_fee), 0.00) as "not-confirmed-revenue",
      coalesce(sum(canceled_revenue       + canceled_fee),      0.00) as "canceled-revenue",
    ${else}
      coalesce(sum(confirmed_revenue),      0.00) as "confirmed-revenue",
      coalesce(sum(not_confirmed_revenue),  0.00) as "not-confirmed-revenue",
      coalesce(sum(canceled_revenue),       0.00) as "canceled-revenue",
    ${end}

    ${if groupByOffer}
      o.id id, o.name as "name"
    ${end}

    ${if groupByAffiliate}
      offer_stat.aff_id id, p.email as "name"
    ${end}

    ${if groupByAffiliateId}
      offer_stat.aff_id id, '' || offer_stat.aff_id as "name"
    ${end}

    ${if groupByAdvertiser}
      p.id id, p.email || ' (' || coalesce(p.organization, '--') || ')' as "name"
    ${end}

    ${if groupBySourceId}
      0 id, offer_stat.source_id as "name"
    ${end}

    ${if groupBySub}
      0 id,
      ${foreach groupBySub sub}
        ${if last_sub}
          coalesce(offer_stat.${sub}, '') as "name"
        ${else}
          coalesce(offer_stat.${sub}, '') || ' / ' ||
        ${end}
      ${end}
    ${end}

    ${if groupByReferer}
      0 id, offer_stat.referer as "name"
    ${end}

    ${if groupByKeywords}
      0 id, offer_stat.keywords as "name"
    ${end}

  from
    offer o

  join
    offer_stat
    on offer_stat.creation_time between :from and :to
    and o.id = offer_stat.master

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
      and exists (
        select 1
        from offer_grant
        where aff_id = offer_stat.aff_id
        and offer_id = o.id
        and state = 'APPROVED'
      )
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

  group by
    ${if groupByOffer}
      o.id, o.name
    ${end}

    ${if groupByAffiliate}
      offer_stat.aff_id, p.email
    ${end}

    ${if groupByAffiliateId}
      offer_stat.aff_id
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

  ) as sums

order by
  "${ordering}" ${direction}, id, "name"
