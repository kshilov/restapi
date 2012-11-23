select
  offer.id,
  min(offer_site.creation_time) d

from offer

join offer_site
on offer_site.offer_id = offer.id

join site
on site.aff_id = :aff_id
and site.id = offer_site.site_id

group by offer.id
order by d
