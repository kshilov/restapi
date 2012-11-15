select distinct

  action.last_change_time as "date",
  cashback.target_id      as "client",
  cashback.referer        as "referer"


from cashback

join offer_action action
on action.id = cashback.id

where cashback.aff_id = :aff_id
and cashback.referer is not null
