alter table offer_stat
add column not_confirmed_fee numeric(19, 2);
alter table offer_stat
add column confirmed_fee numeric(19, 2);
alter table offer_stat
add column canceled_fee numeric(19, 2);


create function offer_stat_fee_approve(stat_id bigint, amount numeric) returns bigint
  language sql
  as $_$

    update offer_stat
    set confirmed_fee = coalesce(confirmed_fee, 0.0) + $2,
    not_confirmed_fee = coalesce(not_confirmed_fee, 0.0) - $2
    where id = $1
    returning id;
  $_$;

drop function approve(bigint);

CREATE FUNCTION approve(action_id bigint) RETURNS bigint
    LANGUAGE sql
    AS $_$

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
      admin_acc.account_id),    /* account_to_id */
      offer_stat_fee_approve(action.stat_id, dst.amount)
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

$_$;
