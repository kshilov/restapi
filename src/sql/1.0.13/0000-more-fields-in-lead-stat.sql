begin;

delete from lead_stat;

alter table lead_stat
add column master bigint not null;

alter table lead_stat
add column aff_id bigint not null;

alter table lead_stat add constraint lead_stat_master_fk
foreign key (master) references offer(id);

alter table lead_stat add constraint lead_stat_aff_id_fk
foreign key (aff_id) references user_profile(id);

end;
