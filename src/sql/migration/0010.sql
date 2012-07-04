begin;

with csi_codes as (
	select *
	from (
		values ('AZ'), ('AM'), ('BY'), ('KZ'), ('KG'),
		('MD'), ('RU'), ('TJ'), ('TM'), ('UZ'), ('UA')
	)
	as _t(code)
)
insert into
	offer_region(offer_id, region)
select
	offer_id,
	csi_codes.code
from offer_region
left join csi_codes on csi_codes.code is not null
where region = 'CSI';

delete from offer_region where region = 'CSI';

update offer_region set region = 'RU' where region = 'RUSSIA';
update offer_region set region = 'UA' where region = 'UKRAINE';
update offer_region set region = 'BY' where region = 'BELARUS';
update offer_region set region = 'PL' where region = 'POLAND';
update offer_region set region = 'LV' where region = 'LATVIA';
update offer_region set region = 'DE' where region = 'GERMANY';

commit;