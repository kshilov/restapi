select
  sum(canceled_revenue * (1 + p_aff.fee / 100.0))       canceled,
  sum(not_confirmed_revenue)                            not_confirmed_partner,
  sum(not_confirmed_revenue * (p_aff.fee / 100.0))      not_confirmed_fee,
  sum(not_confirmed_revenue * (1 + p_aff.fee / 100.0))  not_confirmed_sum,
  sum(confirmed_revenue)                                confirmed_partner,
  sum(confirmed_revenue * (p_aff.fee / 100.0))          confirmed_fee,
  sum(confirmed_revenue * (1 + p_aff.fee / 100.0))      confirmed_sum
from
  offer_stat
left join
  user_profile p_aff on offer_stat.aff_id = p_aff.id
where
  creation_time between :from and :to
