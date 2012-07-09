begin;

ALTER TABLE offer ADD COLUMN allow_deeplink boolean;
UPDATE offer SET allow_deeplink = false WHERE allow_deeplink is null AND parent_id is null;

commit;