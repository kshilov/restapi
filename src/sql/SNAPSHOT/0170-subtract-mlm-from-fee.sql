begin;
create index withdrawal_action_id on withdrawal(action_id);
update withdrawal fee
 set amount = amount - coalesce((select amount
                        from withdrawal mlm
                        where fee.action_id = mlm.action_id
                        and mlm.basis = 'MLM'), 0.0)
where fee.basis = 'FEE';
end;
