select
  offer_site.id             id,
  offer_site.admin_state    admin_state,
  offer_site.admin_comment  admin_comment,
  offer_site.creation_time  creation_time,
  offer_site.last_change_time last_change_time,
  coalesce(offer_site.back_url, '')     back_url,
  coalesce(offer_site.postback_url, '') postback_url,
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

from offer_site

join site
on site.id = offer_site.site_id

join offer
on offer.id = offer_site.offer_id
${if filterByOffer}
and offer.id = :offer_id
${end}

join user_profile affiliate
on affiliate.id = site.aff_id
${if filterByAffiliate}
and affiliate.id = :aff_id
${end}

${if filterById}
where offer_site.id = :id
${else}
where site.admin_state = 'APPROVED'
${end}

