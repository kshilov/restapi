BEGIN;

CREATE TABLE keyword_pattern
(
  id bigserial NOT NULL,
  url_pattern character varying(255),
  keywords_parameter character varying(255),
  CONSTRAINT keyword_pattern_pkey PRIMARY KEY (id )
) WITH (OIDS=FALSE);
ALTER TABLE keyword_pattern OWNER TO postgres;

INSERT INTO keyword_pattern (url_pattern, keywords_parameter) VALUES ('yandex.ru', 'text');
INSERT INTO keyword_pattern (url_pattern, keywords_parameter) VALUES ('.google.', 'q');
INSERT INTO keyword_pattern (url_pattern, keywords_parameter) VALUES ('bing.com', 'q');
INSERT INTO keyword_pattern (url_pattern, keywords_parameter) VALUES ('rambler.ru/search', 'query');

ALTER TABLE offer_stat ADD COLUMN referer character varying(255);
ALTER TABLE offer_stat ADD COLUMN keywords character varying(255);

CREATE INDEX offer_stat_referer_idx ON offer_stat USING btree (referer ASC NULLS FIRST);
CREATE INDEX offer_stat_keywords_idx ON offer_stat USING btree (keywords ASC NULLS FIRST);

COMMIT;