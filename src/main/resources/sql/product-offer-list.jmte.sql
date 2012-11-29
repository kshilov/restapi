select distinct
  offer.id,
  offer.name,
  offer.yml_url

from offer

join placement
on placement.offer_id = offer.id
and placement.admin_state = 'APPROVED'

join site
on site.id = placement.site_id
and site.admin_state = 'APPROVED'
and site.aff_id = :aff_id
${if filterBySite}
and site.id = :site_id
${end}

where offer.is_product_offer = true
and offer.approved = true
and offer.active = true
