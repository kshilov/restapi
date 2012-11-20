select
  offer_site.id       id,
  offer_site.approved approved,
  coalesce(offer_site.back_url, '')     back_url,
  coalesce(offer_site.postback_url, '') postback_url,
  site.id             site_id,
  site.description    site_description,
  site.type           site_type,
  offer.id            offer_id,
  offer.title         offer_title,
  affiliate.id        affiliate_id,
  affiliate.email     affiliate_email

from offer_site

join site
on site.id = offer_site.site_id

join offer
on offer.id = offer_site.offer_id

join user_profile affiliate
on affiliate.id = site.aff_id

where site.approved = true

