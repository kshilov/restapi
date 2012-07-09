begin;

delete from category where name = '';

create sequence category_group_seq
    start with 1
    increment by 1
    no maxvalue
    no minvalue
    cache 1;


create table category_group (
    id bigint not null default nextval('category_group_seq'::regclass),
    name character varying(255) not null
);


alter table public.category_group owner to postgres;


alter table only category_group
    add constraint category_group_pkey primary key (id);


alter table category
add column category_group_id bigint not null default 0;

insert into category_group(name)
select distinct grouping
from category;

update category
set category_group_id = (select grp.id
                        from category_group grp
                        where grp.name = category.grouping);

alter table category
drop column grouping;

alter table only category
    add constraint fk302bcfed0fcda6f foreign key (category_group_id) references category_group(id);

alter table category
alter column category_group_id
drop default;


end;


