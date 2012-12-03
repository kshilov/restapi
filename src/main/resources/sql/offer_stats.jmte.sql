select
  *,
  case shows_count
    when 0 then 0.00
    else clicks_count * 100.0 / shows_count
  end ctr,
  case clicks_count
    when 0 then 0.00
    else leads_count * 100.0 / clicks_count
  end cr,
  case clicks_count
    when 0 then 0.00
    else confirmed_revenue / clicks_count
  end ecpc,
  case shows_count
    when 0 then 0.00
    else confirmed_revenue * 1000.0 / shows_count
  end ecpm
from
  (
  select
    coalesce(sum(show_count),   0) shows_count,
    coalesce(sum(click_count),  0) clicks_count,
    coalesce(sum(action_count), 0)            action_count,
    coalesce(sum(canceled_action_count),  0)  canceled_action_count,
    coalesce(sum(confirmed_action_count), 0)  confirmed_action_count,
    sum(action_count - canceled_action_count) leads_count,
    0                                         sales_count,
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

    ${if groupByAffiliateId}
      offer_stat.aff_id id, '' || offer_stat.aff_id descr
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

    ${if groupByCashback}
      0 id, offer_stat.cashback_target_id descr
    ${end}

    ${if groupBySite}
      site.id id, site.name descr
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

  ${if groupBySite}
    left join site
    on site.id = offer_stat.site_id
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

    ${if groupByCashback}
      offer_stat.cashback_target_id
    ${end}

    ${if groupBySite}
      site.id, site.name
    ${end}

  ) as sums

where descr is not null

order by
  ${ordering} ${direction}, id, descr

