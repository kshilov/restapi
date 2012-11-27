alter table offer_stat
add column site_id bigint;

alter table offer_stat
add constraint offer_site_site_fk foreign key (site_id) references site(id);

alter table offer_action
add column site_id bigint;

alter table offer_action
add constraint offer_action_site_fk foreign key (site_id) references site(id);
