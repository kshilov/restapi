begin;
create sequence withdrawal_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table withdrawal(
  id bigint default nextval('withdrawal_seq'),
  user_id bigint not null,
  basis varchar(255) not null,
  source_id bigint not null,
  action_id bigint not null,
  amount numeric(19,2) not null,
  creation_time timestamp without time zone default now() not null,
  order_time timestamp without time zone
);


alter table only withdrawal add constraint withdrawal_pkey primary key (id);

create sequence withdrawal_payment_seq
  start with 1
  increment by 1
  no minvalue
  no maxvalue
  cache 1;

create table withdrawal_payment(
  id bigint default nextval('withdrawal_payment_seq'),
  withdrawal_id bigint not null,
  amount numeric(19,2) not null,
  creation_time timestamp without time zone default now() not null
);


alter table only withdrawal_payment add constraint withdrawal_payment_pkey primary key (id);

alter table only withdrawal_payment
    add constraint withdrawal_payment_withdrawal_id
    foreign key (withdrawal_id) references withdrawal(id);

create index withdrawal_payment_withdrawal_id_index on withdrawal_payment(withdrawal_id);

insert into withdrawal(user_id, basis, source_id,
                        action_id, amount, creation_time)
select
  case when entry.account_id in (select account_id from admin_account)
  then 1 /* ADMIN */
  else action.aff_id
  end user_id,
  case when entry.account_id in (select account_id from admin_account)
  then 'FEE'
  else 'AFFILIATE_REVENUE'
  end basis,
  coalesce(offer.parent_id, offer_id) source_id,
  entry.source_id action_id,
  entry.amount,
  entry.creation_time
from accounting_entry entry

join offer_action action
on action.id = entry.source_id

join offer
on offer.id = action.offer_id

where
  entry.event = 2 /* ACTION_APPROVED */
  and entry.amount > 0;


/* MONEY, PAYED OUT TO AFFILIATE */
insert into withdrawal_payment(withdrawal_id, amount, creation_time)
select
  withdrawal.id,
  withdrawal.amount,
  min(withdraw.timestamp)
from withdraw

join user_profile usr
on usr.affiliate_account_id = withdraw.account_id

join withdrawal
on withdrawal.creation_time < withdraw.timestamp
and withdrawal.user_id =  usr.id

where withdraw.done = true

group by  withdrawal.id, withdrawal.amount;

/* MONEY, PAYED OUT TO ADMIN */
insert into withdrawal_payment(withdrawal_id, amount, creation_time)
select
  withdrawal.id,
  withdrawal.amount,
  min(withdraw.timestamp)
from withdraw

join withdrawal
on withdrawal.creation_time < withdraw.timestamp
and withdrawal.user_id = 1

join offer_action action
on action.id = withdrawal.action_id

join user_profile usr
on usr.id = action.aff_id
and usr.affiliate_account_id = withdraw.account_id

where withdraw.done = true

group by  withdrawal.id, withdrawal.amount;


create function order_withdrawal(action_id bigint,
  t timestamp without time zone) returns bigint language sql
  as $_$

    update withdrawal
    set order_time = $2
    where action_id = $1; $_$;


update withdrawal set
order_time = (select min(timestamp) from withdraw
              join offer_action action
              on action.id = withdrawals.action_id
              join accounting_entry entry
              on entry.source_id = withdrawal.action_id
              and entry.event = 2 /* ACTION_APPROVED */
              and entry.creation_time < withdraw.timestamp)
where basis = 'AFFILIATE_REVENUE';

update withdrawal set
order_time = creation_time
where basis = 'FEE';
end;
