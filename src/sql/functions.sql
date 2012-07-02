create or replace function transfer_money(
  amount numeric(19,2),
  description varchar(255),
  event integer,
  source_id bigint ,
  account_from_id bigint,
  account_to_id bigint) RETURNS bigint as $$
insert into
  accounting_transaction(id)
select
  nextval('accounting_transaction_seq');

insert into accounting_entry(
  id,
  creation_time,
  amount,
  descr,
  event,
  source_id,
  account_id,
  transaction)
select
  nextval('accounting_entry_seq'),
  now(),
  -$1,
  $2,
  $3,
  $4,
  $5,
  currval('accounting_transaction_seq')
union select
  nextval('accounting_entry_seq'),
  now(),
  $1,
  $2,
  $3,
  $4,
  $6,
  currval('accounting_transaction_seq');

update account
set balance = balance - $1
where id = $5;

update account
set balance = balance + $1
where id = $6;

select currval('accounting_transaction_seq');
$$ language sql;

