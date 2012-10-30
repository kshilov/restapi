begin;
create function set_action_product(action_id bigint, product_id bigint)
  returns bigint
  language sql
  as $_$
    update offer_action action
    set product_id = $2, offer_id = (select offer_id from product
                                     where product.id = $2)
    where action.id = $1
    returning action.id;
  $_$;

create function set_stat_product(stat_id bigint, product_id bigint)
  returns bigint
  language sql
  as $_$
    update offer_stat stat
    set product_id = $2, offer_id = master
    where stat.id = $1
    returning stat.id;
  $_$;

select set_action_product(action.id, product.id)
from offer_action action

join offer
on offer.id = action.offer_id

join offer parent_offer
on parent_offer.id = offer.parent_id
and parent_offer.is_product_offer = true

join product
on product.offer_id = parent_offer.id
and product.original_id = offer.code;

select set_stat_product(stat.id, product.id)
from offer_stat stat

join offer
on offer.id = stat.offer_id

join offer parent_offer
on parent_offer.id = stat.master

join product
on product.original_id = offer.code
and product.offer_id = parent_offer.id;

drop function set_action_product(bigint, bigint);
drop function set_stat_product(bigint, bigint);

end;
