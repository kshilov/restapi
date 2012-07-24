drop function approve(bigint);

CREATE FUNCTION approve(action_id bigint, mlm numeric) RETURNS bigint
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

select
  transfer_money(
    dst.amount * $2,
    null,
    7,    /* MLM */
    $1,
    admin_acc.account_id,
    referrer.affiliate_account_id)
from offer_action action

left join admin_account admin_acc
on admin_acc.id is not null

left join user_profile affiliate
on affiliate.id = action.aff_id

left join user_profile referrer
on referrer.id = affiliate.referrer

left join accounting_entry dst
on
  dst.event = 2 /*action_approved*/
  and dst.source_id = action.id
  and dst.amount > 0
  and dst.account_id = admin_acc.account_id

where
  action.id = $1
  and referrer.id is not null;

update offer_action
set state = 1
where id = $1
returning id;

$_$;

/* do all not created mlm */

select
  transfer_money(
    dst.amount * 0.07, /* MLM RATE */
    null,
    7,    /* MLM */
    action.id,
    admin_acc.account_id,
    referrer.affiliate_account_id)
from offer_action action

left join admin_account admin_acc
on admin_acc.id is not null

left join user_profile affiliate
on affiliate.id = action.aff_id

left join user_profile referrer
on referrer.id = affiliate.referrer

left join accounting_entry dst
on
  dst.event = 2 /*action_approved*/
  and dst.source_id = action.id
  and dst.amount > 0
  and dst.account_id = admin_acc.account_id

where
  action.state = 1 /* action approved */
  and referrer.id is not null
  and not exists (select *
                  from accounting_entry
                  where
                  source_id = action.id
                  and event = 7);

