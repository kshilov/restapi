begin;

insert into site(type, aff_id, name, admin_state)

select 'GRANT', aff.id, 'Моя площадка', 'APPROVED' from
user_profile aff
where affiliate_account_id is not null
and exists (select * from offer_grant where offer_grant.aff_id = aff.id);

insert into placement
(offer_id, site_id, admin_state, admin_comment, back_url, postback_url)
select
offer_grant.offer_id  offer_id,
site.id         site_id,
case (offer_grant.blocked) when true then 'BLOCKED' else 'APPROVED' end admin_state,
offer_grant.block_reason  admin_comment,
offer_grant.back_url      back_url,
offer_grant.postback_url  postback_url

from offer_grant

join site
on site.aff_id = offer_grant.aff_id
and site.type = 'GRANT'
and offer_grant.state = 'APPROVED';

update offer_stat
set site_id = (select id
               from site
               where aff_id = offer_stat.aff_id and type = 'GRANT')
where site_id is null;

update offer_action
set site_id = (select id
               from site
               where aff_id = offer_action.aff_id and type = 'GRANT')
where site_id is null;

end;
