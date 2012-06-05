BEGIN;

ALTER TABLE offer_stat ADD COLUMN sub_id1 character varying(255);
ALTER TABLE offer_stat ADD COLUMN sub_id2 character varying(255);
ALTER TABLE offer_stat ADD COLUMN sub_id3 character varying(255);
ALTER TABLE offer_stat ADD COLUMN sub_id4 character varying(255);

DROP INDEX offer_stat_all_idx;
CREATE INDEX offer_stat_all_idx ON offer_stat USING btree (
 offer_id, aff_id, banner_id, source_id COLLATE pg_catalog."default",
 sub_id COLLATE pg_catalog."default", sub_id1 COLLATE pg_catalog."default",
 sub_id2 COLLATE pg_catalog."default", sub_id3 COLLATE pg_catalog."default",
 sub_id4 COLLATE pg_catalog."default");

COMMIT;



