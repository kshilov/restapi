alter table user_profile
add column source varchar(255);
alter table user_profile
drop column source_url;
