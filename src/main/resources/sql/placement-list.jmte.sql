select
  placement.id              id,
  placement.admin_state     admin_state,
  placement.admin_comment   admin_comment,
  placement.creation_time   creation_time,
  placement.last_change_time           last_change_time,
  coalesce(placement.back_url, '')     back_url,
  coalesce(placement.postback_url, '') postback_url,
  site.id             site_id,
  site.name           site_name,
  site.type           site_type,
  site.admin_state    site_admin_state,
  site.admin_comment  site_admin_comment,
  offer.id            offer_id,
  offer.name          offer_name,
  affiliate.id        affiliate_id,
  affiliate.email     affiliate_email,
  affiliate.blocked   affiliate_blocked

from placement

join site
on site.id = placement.site_id

join offer
on offer.id = placement.offer_id
${if filterByOffer}
and offer.id = :offer_id
${end}

join user_profile affiliate
on affiliate.id = site.aff_id
${if filterByAffiliate}
and affiliate.id = :aff_id
${end}

${if filterById}
where placement.id = :id
${else}
where site.admin_state = 'APPROVED'
${end}

order by last_change_time desc
