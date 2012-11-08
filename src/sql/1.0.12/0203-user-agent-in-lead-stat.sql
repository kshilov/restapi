begin;

alter table lead_stat
add column user_agent varchar(255);

end;
