begin;

drop table if exists black_list;
drop sequence if exists black_list_seq;

create sequence black_list_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table black_list(
  id bigint not null default nextval('black_list_seq'),
  host varchar(255) not null,
  sub_domain_mask varchar(255),
  path_mask varchar(255));

alter table black_list add constraint black_list_pk primary key (id);

create index black_list_host_idx on black_list(host);

end;
