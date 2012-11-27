select
  offer.id,
  min(placement.creation_time) d

from offer

join placement
on placement.offer_id = offer.id

join site
on site.aff_id = :aff_id
and site.id = placement.site_id

group by offer.id
order by d
