select
  affiliate.id    as affiliate_id,
  affiliate.email as affiliate_email,
  coalesce(first_period.referer, second_period.referer) as referer,
  coalesce(first_period.show_count, 0)  as first_period_show_count,
  coalesce(second_period.show_count, 0) as second_period_show_count,
  coalesce(second_period.show_count, 0) - coalesce(first_period.show_count, 0)    as show_count_diff,
  coalesce(first_period.click_count, 0)  as first_period_click_count,
  coalesce(second_period.click_count, 0) as second_period_click_count,
  coalesce(second_period.click_count, 0) - coalesce(first_period.click_count, 0)  as click_count_diff

from (select 
    aff_id,
    referer,
    sum(show_count)   as show_count,
    sum(click_count)  as click_count
  from offer_stat 
  where creation_time between :first_period_from and :first_period_to
  group by aff_id, referer) as first_period

full join (select 
    aff_id,
    referer,
    sum(show_count)   as show_count,
    sum(click_count)  as click_count
  from offer_stat 
  where creation_time between :second_period_from and :second_period_to
  group by aff_id, referer) as second_period
on second_period.aff_id = first_period.aff_id
and second_period.referer = first_period.referer

join user_profile affiliate
on affiliate.id = coalesce(first_period.aff_id, second_period.aff_id)

order by ${ordering} ${direction},
click_count_diff asc, show_count_diff asc, first_period_click_count desc
