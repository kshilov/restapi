begin;

alter table offer_stat
add column action_count int not null default 0;

alter table offer_stat
add column confirmed_action_count int not null default 0;

alter table offer_stat
add column canceled_action_count int not null default 0;

end;

begin;

update offer_stat set action_count = leads_count + sales_count;

update offer_stat
set canceled_action_count = (select count(*)
                             from offer_action
                             where stat_id = offer_stat.id
                             and state = 2)
where coalesce(canceled_revenue, 0.0) > 0;

update offer_stat
set confirmed_action_count = (select count(*)
                             from offer_action
                             where stat_id = offer_stat.id
                             and state = 1)
where coalesce(confirmed_revenue, 0.0) > 0;

end;
