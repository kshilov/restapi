alter table offer
add column fee_type varchar(255) NOT NULL DEFAULT 'PERCENT';

alter table offer
add column fee numeric(19, 2) NOT NULL DEFAULT 30.0;

alter table user_profile
drop column fee;
