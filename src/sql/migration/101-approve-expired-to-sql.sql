/* transferMoney

*/
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


/*
 offer_stat_approve
 stat_id, amount
 */
create or replace function offer_stat_approve(
  stat_id bigint,
  amount numeric(19,2)) returns bigint as $$

  update offer_stat
  set confirmed_revenue = coalesce(confirmed_revenue, 0.0) + $2,
  not_confirmed_revenue = coalesce(not_confirmed_revenue, 0.0) - $2
  where id = $1
  returning id;

$$ language sql;



/*
 approve 2
 */

create or replace function approve(action_id bigint) returns bigint as $$

select
    transfer_money(
      dst.amount,
      null, /* description */
      2,    /* event = action_approved */
      $1,   /* source_id */
      affiliate.affiliate_account_not_confirmed_id, /* account_from_id */
      affiliate.affiliate_account_id),              /* account_to_id */
    offer_stat_approve(action.stat_id, dst.amount)
from offer_action action

left join user_profile affiliate
on affiliate.id = action.aff_id

left join accounting_entry dst
on
  dst.event = 1
  and dst.source_id = action.id
  and dst.account_id = affiliate.affiliate_account_not_confirmed_id
  and dst.amount > 0
where
  action.id = $1;


select
    transfer_money(
      dst.amount,
      null, /* description */
      2,    /* event = action_approved */
      $1,   /* source_id */
      admin_acc_nc.account_id,  /* account_from_id */
      admin_acc.account_id)     /* account_to_id */
from offer_action action

left join admin_account_not_confirmed admin_acc_nc
on admin_acc_nc.id is not null

left join admin_account admin_acc
on admin_acc.id is not null

left join accounting_entry dst
on
  dst.event = 1 /*action_created*/
  and dst.source_id = action.id
  and dst.amount > 0
  and dst.account_id = admin_acc_nc.account_id
where
  action.id = $1;

update offer_action
set state = 1
where id = $1
returning id;

$$ language sql;

/*
  approve_expired
*/
create or replace function approve_expired() returns setof bigint as $$
  select
    approve(action.id)
  from offer
  left join offer_action action
  on action.offer_id = offer.id
  where
    cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
    and action.state = 0;
$$ language sql;


/*
  approve_expired by offer id
*/
create or replace function approve_expired(offer_id bigint) returns setof bigint as $$
  select
    approve(action.id)
  from offer
  left join offer_action action
  on action.offer_id = offer.id
  where
    cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
    and action.state = 0
    and action.offer_id in (
      select $1
      union
      select id
      from offer
      where parent_id = $1
    );
$$ language sql;


/*
  index
*/
create index accounting_entry_source_id_event on accounting_entry(source_id, event);
