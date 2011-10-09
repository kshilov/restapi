create index app_deleted_id_idx on app(deleted, id);
create index account_tx_account_id_idx on account_tx(account_id);
create index offer_order_deleted_approved_idx on offer_order(deleted, approved);
create index action_deleted_performer_id_idx on action(deleted, performer_id);
create index offer_creation_time_idx on offer(creation_time);
