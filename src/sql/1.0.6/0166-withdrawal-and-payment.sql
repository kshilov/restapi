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
  action.creation_time
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

/* ORDER TIME FOR AFFILIATES */
update withdrawal
set order_time = (select min(timestamp) from withdraw
                  where timestamp > withdrawal.creation_time
                  and account_id = (select affiliate_account_id
                                    from user_profile
                                    where id = withdrawal.user_id))
where basis = 'AFFILIATE_REVENUE';

/* ORDER TIME FOR ADMIN */
update withdrawal set
order_time = creation_time
where basis = 'FEE';


/* RETURN NOT PAYED OUT OFFER MONEY BACK TO ADVERTISER */
select
  transfer_money(
  coalesce(sum(offer_entry.amount), 0.0) -
    (select coalesce(sum(withdrawal_payment.amount), 0.0)
    from withdrawal, withdrawal_payment
    where withdrawal_payment.withdrawal_id = withdrawal.id
    and withdrawal.source_id = offer.id), /* amount */
  'Returning offer money to advertiser due to migration 0166, v1.0.6',
  null,                       /* event */
  null,                       /* source_id */
  offer_entry.account_id,     /* from */
  adv.advertiser_account_id)   /* to */
from accounting_entry offer_entry

join offer
on offer.account_id = offer_entry.account_id

join user_profile adv
on adv.id = offer.user_id

where offer_entry.event = 4 /* OFFER_ACC_ADD */
and offer_entry.amount > 0

group by offer.id, offer_entry.account_id, adv.advertiser_account_id;
end;
