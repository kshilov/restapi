--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: approve(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION approve(action_id bigint) RETURNS bigint
    LANGUAGE sql
    AS $_$

select
    transfer_money(
      dst.amount,
      null, /* description */
      2,    /* event = action_approved */
      $1,   /* source_id */
      affiliate.affiliate_account_not_confirmed_id, /* account_from_id */
      affiliate.affiliate_account_id),              /* account_to_id */
    offer_stat_approve(action.stat_id, dst.amount)
from offer_action action

left join user_profile affiliate
on affiliate.id = action.aff_id

left join accounting_entry dst
on
  dst.event = 1
  and dst.source_id = action.id
  and dst.account_id = affiliate.affiliate_account_not_confirmed_id
  and dst.amount > 0
where
  action.id = $1;


select
    transfer_money(
      dst.amount,
      null, /* description */
      2,    /* event = action_approved */
      $1,   /* source_id */
      admin_acc_nc.account_id,  /* account_from_id */
      admin_acc.account_id)     /* account_to_id */
from offer_action action

left join admin_account_not_confirmed admin_acc_nc
on admin_acc_nc.id is not null

left join admin_account admin_acc
on admin_acc.id is not null

left join accounting_entry dst
on
  dst.event = 1 /*action_created*/
  and dst.source_id = action.id
  and dst.amount > 0
  and dst.account_id = admin_acc_nc.account_id
where
  action.id = $1;

update offer_action
set state = 1
where id = $1
returning id;

$_$;


--
-- Name: approve_expired(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION approve_expired() RETURNS SETOF bigint
    LANGUAGE sql
    AS $$
  select
    approve(action.id)
  from offer
  left join offer_action action
  on action.offer_id = offer.id
  where
    cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
    and action.state = 0;
$$;


--
-- Name: approve_expired(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION approve_expired(offer_id bigint) RETURNS SETOF bigint
    LANGUAGE sql
    AS $_$
  select
    approve(action.id)
  from offer
  left join offer_action action
  on action.offer_id = offer.id
  where
    cast(now() as date) - cast(action.creation_time as date) > offer.hold_days
    and action.state = 0
    and action.offer_id in (
      select $1
      union
      select id
      from offer
      where parent_id = $1
    );
$_$;


--
-- Name: offer_stat_approve(bigint, numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION offer_stat_approve(stat_id bigint, amount numeric) RETURNS bigint
    LANGUAGE sql
    AS $_$

  update offer_stat
  set confirmed_revenue = coalesce(confirmed_revenue, 0.0) + $2,
  not_confirmed_revenue = coalesce(not_confirmed_revenue, 0.0) - $2
  where id = $1
  returning id;

$_$;


--
-- Name: transfer_money(numeric, character varying, integer, bigint, bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION transfer_money(amount numeric, description character varying, event integer, source_id bigint, account_from_id bigint, account_to_id bigint) RETURNS bigint
    LANGUAGE sql
    AS $_$
insert into
  accounting_transaction(id)
select
  nextval('accounting_transaction_seq');

insert into accounting_entry(
  id,
  creation_time,
  amount,
  descr,
  event,
  source_id,
  account_id,
  transaction)
select
  nextval('accounting_entry_seq'),
  now(),
  -$1,
  $2,
  $3,
  $4,
  $5,
  currval('accounting_transaction_seq')
union select
  nextval('accounting_entry_seq'),
  now(),
  $1,
  $2,
  $3,
  $4,
  $6,
  currval('accounting_transaction_seq');

update account
set balance = balance - $1
where id = $5;

update account
set balance = balance + $1
where id = $6;

select currval('accounting_transaction_seq');
$_$;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE account (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    last_change_time timestamp without time zone,
    allow_negative_balance boolean,
    balance numeric(19,2) NOT NULL,
    version integer
);


--
-- Name: account_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE account_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: accounting_entry; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE accounting_entry (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    last_change_time timestamp without time zone,
    amount numeric(19,2) NOT NULL,
    descr character varying(255),
    event integer,
    source_id bigint,
    account_id bigint NOT NULL,
    transaction bigint
);


--
-- Name: accounting_entry_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE accounting_entry_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: accounting_transaction; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE accounting_transaction (
    id bigint NOT NULL
);


--
-- Name: accounting_transaction_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE accounting_transaction_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admin_account; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE admin_account (
    id bigint NOT NULL,
    account_id bigint NOT NULL
);


--
-- Name: admin_account_not_confirmed; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE admin_account_not_confirmed (
    id bigint NOT NULL,
    account_id bigint NOT NULL
);


--
-- Name: admin_account_not_confirmed_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE admin_account_not_confirmed_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admin_account_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE admin_account_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: banner; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE banner (
    id bigint NOT NULL,
    height integer,
    mimetype character varying(255),
    width integer,
    offer_id bigint NOT NULL,
    url character varying(255)
);


--
-- Name: banner_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE banner_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE category (
    id bigint DEFAULT nextval('category_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL,
    category_group_id bigint NOT NULL
);


--
-- Name: category_group_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE category_group_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE category_group (
    id bigint DEFAULT nextval('category_group_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: error_info; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE error_info (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    last_occurred timestamp without time zone NOT NULL,
    occurrence_count bigint NOT NULL,
    stack_trace character varying(10000),
    uri character varying(255) NOT NULL
);


--
-- Name: error_info_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE error_info_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ip_segment_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE ip_segment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ip_segment; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ip_segment (
    id bigint DEFAULT nextval('ip_segment_seq'::regclass) NOT NULL,
    country_code character varying(255),
    country_name character varying(255),
    end_ip_addr character varying(255),
    end_ip_num bigint,
    start_ip_addr character varying(255),
    start_ip_num bigint
);


--
-- Name: keyword_pattern; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE keyword_pattern (
    id bigint NOT NULL,
    url_pattern character varying(255),
    keywords_parameter character varying(255)
);


--
-- Name: keyword_pattern_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE keyword_pattern_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: keyword_pattern_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE keyword_pattern_id_seq OWNED BY keyword_pattern.id;


--
-- Name: mlm_execution; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mlm_execution (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL
);


--
-- Name: mlm_execution_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mlm_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer (
    type integer NOT NULL,
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    auto_approve boolean NOT NULL,
    code character varying(255) NOT NULL,
    cost numeric(19,2),
    cpa_policy character varying(255),
    hold_days integer,
    pay_method character varying(255),
    percent numeric(19,2),
    reentrant boolean NOT NULL,
    title character varying(255) NOT NULL,
    approved boolean,
    block_reason text,
    cookie_ttl integer,
    description text,
    logo_file_name character varying(255),
    name character varying(255),
    url character varying(255),
    parent_id bigint,
    account_id bigint,
    user_id bigint,
    token_param_name character varying(255),
    launch_time timestamp without time zone,
    site_url character varying(255),
    cost2 numeric(19,2),
    cr numeric(19,2),
    short_description character varying(255),
    showcase boolean,
    allow_deeplink boolean
);


--
-- Name: offer_action; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer_action (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    state integer,
    transaction_id character varying(255) NOT NULL,
    offer_id bigint NOT NULL,
    stat_id bigint NOT NULL,
    token_id bigint,
    aff_id bigint,
    source_id bigint NOT NULL
);


--
-- Name: offer_action_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE offer_action_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer_category; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer_category (
    offer_id bigint NOT NULL,
    category_id bigint NOT NULL
);


--
-- Name: offer_grant; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer_grant (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    aff_id bigint NOT NULL,
    back_url character varying(255),
    block_reason text,
    blocked boolean,
    message text NOT NULL,
    offer_id bigint NOT NULL,
    postback_url character varying(255),
    reject_reason text,
    state character varying(255)
);


--
-- Name: offer_grant_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE offer_grant_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer_region; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer_region (
    offer_id bigint NOT NULL,
    region character varying(255)
);


--
-- Name: offer_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE offer_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer_stat; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE offer_stat (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    aff_id bigint,
    banner_id bigint,
    click_count bigint NOT NULL,
    offer_id bigint,
    show_count bigint NOT NULL,
    source_id text,
    sub_id text,
    canceled_revenue numeric(19,2),
    confirmed_revenue numeric(19,2),
    leads_count bigint,
    not_confirmed_revenue numeric(19,2),
    sales_count bigint,
    master bigint NOT NULL,
    sub_id1 text,
    sub_id2 text,
    sub_id3 text,
    sub_id4 text,
    referer text,
    keywords text
);


--
-- Name: offer_stat_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE offer_stat_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: setting; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE setting (
    name character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


--
-- Name: site; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE site (
    id bigint NOT NULL,
    comment character varying(255) NOT NULL,
    domain character varying(255) NOT NULL,
    lang character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: site_category; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE site_category (
    offer_id bigint NOT NULL,
    category_id bigint NOT NULL
);


--
-- Name: site_region; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE site_region (
    site_id bigint NOT NULL,
    region character varying(255)
);


--
-- Name: site_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE site_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: token; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE token (
    id bigint NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    value character varying(255) NOT NULL,
    stat_id bigint NOT NULL,
    aff_params text
);


--
-- Name: token_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE token_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_profile; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_profile (
    id bigint NOT NULL,
    blocked boolean NOT NULL,
    confirmed boolean NOT NULL,
    email character varying(255) NOT NULL,
    fee integer NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    messenger_type integer,
    messenger_uid character varying(255),
    organization character varying(255),
    passwordhash character varying(255) NOT NULL,
    phone character varying(255),
    referrer bigint,
    register_time timestamp without time zone NOT NULL,
    source_url character varying(255),
    advertiser_account_id bigint,
    affiliate_account_id bigint,
    affiliate_account_not_confirmed_id bigint,
    block_reason character varying(255),
    wmr character varying(255)
);


--
-- Name: user_profile_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_role (
    user_id bigint NOT NULL,
    role integer
);


--
-- Name: withdraw; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE withdraw (
    id bigint NOT NULL,
    amount numeric(19,2),
    done boolean,
    "timestamp" timestamp without time zone,
    account_id bigint NOT NULL
);


--
-- Name: withdraw_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE withdraw_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY keyword_pattern ALTER COLUMN id SET DEFAULT nextval('keyword_pattern_id_seq'::regclass);


--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: accounting_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY accounting_entry
    ADD CONSTRAINT accounting_entry_pkey PRIMARY KEY (id);


--
-- Name: accounting_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY accounting_transaction
    ADD CONSTRAINT accounting_transaction_pkey PRIMARY KEY (id);


--
-- Name: admin_account_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY admin_account
    ADD CONSTRAINT admin_account_account_id_key UNIQUE (account_id);


--
-- Name: admin_account_not_confirmed_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY admin_account_not_confirmed
    ADD CONSTRAINT admin_account_not_confirmed_account_id_key UNIQUE (account_id);


--
-- Name: admin_account_not_confirmed_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY admin_account_not_confirmed
    ADD CONSTRAINT admin_account_not_confirmed_pkey PRIMARY KEY (id);


--
-- Name: admin_account_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY admin_account
    ADD CONSTRAINT admin_account_pkey PRIMARY KEY (id);


--
-- Name: banner_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY banner
    ADD CONSTRAINT banner_pkey PRIMARY KEY (id);


--
-- Name: category_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY category_group
    ADD CONSTRAINT category_group_pkey PRIMARY KEY (id);


--
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: error_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY error_info
    ADD CONSTRAINT error_info_pkey PRIMARY KEY (id);


--
-- Name: ip_segment_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ip_segment
    ADD CONSTRAINT ip_segment_pkey PRIMARY KEY (id);


--
-- Name: keyword_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY keyword_pattern
    ADD CONSTRAINT keyword_pattern_pkey PRIMARY KEY (id);


--
-- Name: mlm_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mlm_execution
    ADD CONSTRAINT mlm_execution_pkey PRIMARY KEY (id);


--
-- Name: offer_action_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT offer_action_pkey PRIMARY KEY (id);


--
-- Name: offer_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offer_category
    ADD CONSTRAINT offer_category_pkey PRIMARY KEY (offer_id, category_id);


--
-- Name: offer_grant_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offer_grant
    ADD CONSTRAINT offer_grant_pkey PRIMARY KEY (id);


--
-- Name: offer_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offer
    ADD CONSTRAINT offer_pkey PRIMARY KEY (id);


--
-- Name: offer_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY offer_stat
    ADD CONSTRAINT offer_stat_pkey PRIMARY KEY (id);


--
-- Name: setting_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY setting
    ADD CONSTRAINT setting_pkey PRIMARY KEY (name);


--
-- Name: site_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY site_category
    ADD CONSTRAINT site_category_pkey PRIMARY KEY (offer_id, category_id);


--
-- Name: site_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- Name: token_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY token
    ADD CONSTRAINT token_pkey PRIMARY KEY (id);


--
-- Name: user_profile_email_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_email_key UNIQUE (email);


--
-- Name: user_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_pkey PRIMARY KEY (id);


--
-- Name: withdraw_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY withdraw
    ADD CONSTRAINT withdraw_pkey PRIMARY KEY (id);


--
-- Name: accounting_entry_event_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX accounting_entry_event_idx ON accounting_entry USING btree (event);


--
-- Name: accounting_entry_source_id_event; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX accounting_entry_source_id_event ON accounting_entry USING btree (source_id, event);


--
-- Name: ip_segment_ip_num_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ip_segment_ip_num_idx ON ip_segment USING btree (start_ip_num, start_ip_num);


--
-- Name: offer_grant_offer_aff_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX offer_grant_offer_aff_idx ON offer_grant USING btree (offer_id, aff_id);


--
-- Name: offer_stat_all_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX offer_stat_all_idx ON offer_stat USING btree (offer_id, aff_id, banner_id, source_id, sub_id, sub_id1, sub_id2, sub_id3, sub_id4);


--
-- Name: offer_stat_keywords_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX offer_stat_keywords_idx ON offer_stat USING btree (keywords NULLS FIRST);


--
-- Name: offer_stat_master_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX offer_stat_master_idx ON offer_stat USING btree (master);


--
-- Name: offer_stat_referer_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX offer_stat_referer_idx ON offer_stat USING btree (referer NULLS FIRST);


--
-- Name: fk143bf46a5b1a8246; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT fk143bf46a5b1a8246 FOREIGN KEY (user_id) REFERENCES user_profile(id);


--
-- Name: fk14c19571b060eb6d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY admin_account_not_confirmed
    ADD CONSTRAINT fk14c19571b060eb6d FOREIGN KEY (account_id) REFERENCES account(id);


--
-- Name: fk2966ea37e9e9694e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_region
    ADD CONSTRAINT fk2966ea37e9e9694e FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fk2a0b0979e9e9694e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_grant
    ADD CONSTRAINT fk2a0b0979e9e9694e FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fk2a0b0979f32d570; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_grant
    ADD CONSTRAINT fk2a0b0979f32d570 FOREIGN KEY (aff_id) REFERENCES user_profile(id);


--
-- Name: fk2dbb4ef687ea7e92; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site_category
    ADD CONSTRAINT fk2dbb4ef687ea7e92 FOREIGN KEY (offer_id) REFERENCES site(id);


--
-- Name: fk2dbb4ef6974c9fe7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site_category
    ADD CONSTRAINT fk2dbb4ef6974c9fe7 FOREIGN KEY (category_id) REFERENCES category(id);


--
-- Name: fk302bcfed0fcda6f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY category
    ADD CONSTRAINT fk302bcfed0fcda6f FOREIGN KEY (category_group_id) REFERENCES category_group(id);


--
-- Name: fk35df475b1a8246; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site
    ADD CONSTRAINT fk35df475b1a8246 FOREIGN KEY (user_id) REFERENCES user_profile(id);


--
-- Name: fk487e2135618a016b; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT fk487e2135618a016b FOREIGN KEY (affiliate_account_not_confirmed_id) REFERENCES account(id);


--
-- Name: fk487e213586ae0b01; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT fk487e213586ae0b01 FOREIGN KEY (advertiser_account_id) REFERENCES account(id);


--
-- Name: fk487e2135c547599f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT fk487e2135c547599f FOREIGN KEY (affiliate_account_id) REFERENCES account(id);


--
-- Name: fk64c1a5c5b1a8246; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer
    ADD CONSTRAINT fk64c1a5c5b1a8246 FOREIGN KEY (user_id) REFERENCES user_profile(id);


--
-- Name: fk64c1a5c931f3040; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer
    ADD CONSTRAINT fk64c1a5c931f3040 FOREIGN KEY (parent_id) REFERENCES offer(id);


--
-- Name: fk64c1a5cb060eb6d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer
    ADD CONSTRAINT fk64c1a5cb060eb6d FOREIGN KEY (account_id) REFERENCES account(id);


--
-- Name: fk696b9f9de588869; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY token
    ADD CONSTRAINT fk696b9f9de588869 FOREIGN KEY (stat_id) REFERENCES offer_stat(id);


--
-- Name: fk8ec4aa484b15e37a; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY accounting_entry
    ADD CONSTRAINT fk8ec4aa484b15e37a FOREIGN KEY (transaction) REFERENCES accounting_transaction(id);


--
-- Name: fk8ec4aa48b060eb6d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY accounting_entry
    ADD CONSTRAINT fk8ec4aa48b060eb6d FOREIGN KEY (account_id) REFERENCES account(id);


--
-- Name: fk977dbb81974c9fe7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_category
    ADD CONSTRAINT fk977dbb81974c9fe7 FOREIGN KEY (category_id) REFERENCES category(id);


--
-- Name: fk977dbb81e9e9694e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_category
    ADD CONSTRAINT fk977dbb81e9e9694e FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fk9a2be5763d14fa6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_stat
    ADD CONSTRAINT fk9a2be5763d14fa6 FOREIGN KEY (banner_id) REFERENCES banner(id);


--
-- Name: fk9a2be57e9e9694e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_stat
    ADD CONSTRAINT fk9a2be57e9e9694e FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fk9a2be57f32d570; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_stat
    ADD CONSTRAINT fk9a2be57f32d570 FOREIGN KEY (aff_id) REFERENCES user_profile(id);


--
-- Name: fka96cd6ec32e50dc7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY site_region
    ADD CONSTRAINT fka96cd6ec32e50dc7 FOREIGN KEY (site_id) REFERENCES site(id);


--
-- Name: fkacc57f2ce9e9694e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY banner
    ADD CONSTRAINT fkacc57f2ce9e9694e FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fkc4e417970d062fd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT fkc4e417970d062fd FOREIGN KEY (offer_id) REFERENCES offer(id);


--
-- Name: fkc4e4179bfbefb4d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT fkc4e4179bfbefb4d FOREIGN KEY (token_id) REFERENCES token(id);


--
-- Name: fkc4e4179de588869; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT fkc4e4179de588869 FOREIGN KEY (stat_id) REFERENCES offer_stat(id);


--
-- Name: fkc4e4179ea30fd42; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT fkc4e4179ea30fd42 FOREIGN KEY (source_id) REFERENCES offer_stat(id);


--
-- Name: fkc4e4179f32d570; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY offer_action
    ADD CONSTRAINT fkc4e4179f32d570 FOREIGN KEY (aff_id) REFERENCES user_profile(id);


--
-- Name: fkc7f50b0ab060eb6d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY withdraw
    ADD CONSTRAINT fkc7f50b0ab060eb6d FOREIGN KEY (account_id) REFERENCES account(id);


--
-- Name: fkf454753db060eb6d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY admin_account
    ADD CONSTRAINT fkf454753db060eb6d FOREIGN KEY (account_id) REFERENCES account(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: account; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE account FROM PUBLIC;
REVOKE ALL ON TABLE account FROM postgres;
GRANT ALL ON TABLE account TO postgres;
GRANT SELECT ON TABLE account TO dumper;


--
-- Name: account_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE account_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE account_seq FROM postgres;
GRANT ALL ON SEQUENCE account_seq TO postgres;
GRANT SELECT ON SEQUENCE account_seq TO dumper;


--
-- Name: accounting_entry; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE accounting_entry FROM PUBLIC;
REVOKE ALL ON TABLE accounting_entry FROM postgres;
GRANT ALL ON TABLE accounting_entry TO postgres;
GRANT SELECT ON TABLE accounting_entry TO dumper;


--
-- Name: accounting_entry_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE accounting_entry_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE accounting_entry_seq FROM postgres;
GRANT ALL ON SEQUENCE accounting_entry_seq TO postgres;
GRANT SELECT ON SEQUENCE accounting_entry_seq TO dumper;


--
-- Name: accounting_transaction; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE accounting_transaction FROM PUBLIC;
REVOKE ALL ON TABLE accounting_transaction FROM postgres;
GRANT ALL ON TABLE accounting_transaction TO postgres;
GRANT SELECT ON TABLE accounting_transaction TO dumper;


--
-- Name: accounting_transaction_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE accounting_transaction_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE accounting_transaction_seq FROM postgres;
GRANT ALL ON SEQUENCE accounting_transaction_seq TO postgres;
GRANT SELECT ON SEQUENCE accounting_transaction_seq TO dumper;


--
-- Name: admin_account; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE admin_account FROM PUBLIC;
REVOKE ALL ON TABLE admin_account FROM postgres;
GRANT ALL ON TABLE admin_account TO postgres;
GRANT SELECT ON TABLE admin_account TO dumper;


--
-- Name: admin_account_not_confirmed; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE admin_account_not_confirmed FROM PUBLIC;
REVOKE ALL ON TABLE admin_account_not_confirmed FROM postgres;
GRANT ALL ON TABLE admin_account_not_confirmed TO postgres;
GRANT SELECT ON TABLE admin_account_not_confirmed TO dumper;


--
-- Name: admin_account_not_confirmed_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE admin_account_not_confirmed_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE admin_account_not_confirmed_seq FROM postgres;
GRANT ALL ON SEQUENCE admin_account_not_confirmed_seq TO postgres;
GRANT SELECT ON SEQUENCE admin_account_not_confirmed_seq TO dumper;


--
-- Name: admin_account_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE admin_account_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE admin_account_seq FROM postgres;
GRANT ALL ON SEQUENCE admin_account_seq TO postgres;
GRANT SELECT ON SEQUENCE admin_account_seq TO dumper;


--
-- Name: banner; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE banner FROM PUBLIC;
REVOKE ALL ON TABLE banner FROM postgres;
GRANT ALL ON TABLE banner TO postgres;
GRANT SELECT ON TABLE banner TO dumper;


--
-- Name: banner_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE banner_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE banner_seq FROM postgres;
GRANT ALL ON SEQUENCE banner_seq TO postgres;
GRANT SELECT ON SEQUENCE banner_seq TO dumper;


--
-- Name: category_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE category_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE category_seq FROM postgres;
GRANT ALL ON SEQUENCE category_seq TO postgres;
GRANT SELECT ON SEQUENCE category_seq TO dumper;


--
-- Name: category; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE category FROM PUBLIC;
REVOKE ALL ON TABLE category FROM postgres;
GRANT ALL ON TABLE category TO postgres;
GRANT SELECT ON TABLE category TO dumper;


--
-- Name: category_group_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE category_group_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE category_group_seq FROM postgres;
GRANT ALL ON SEQUENCE category_group_seq TO postgres;
GRANT SELECT ON SEQUENCE category_group_seq TO dumper;


--
-- Name: category_group; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE category_group FROM PUBLIC;
REVOKE ALL ON TABLE category_group FROM postgres;
GRANT ALL ON TABLE category_group TO postgres;
GRANT SELECT ON TABLE category_group TO dumper;


--
-- Name: error_info; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE error_info FROM PUBLIC;
REVOKE ALL ON TABLE error_info FROM postgres;
GRANT ALL ON TABLE error_info TO postgres;
GRANT SELECT ON TABLE error_info TO dumper;


--
-- Name: error_info_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE error_info_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE error_info_seq FROM postgres;
GRANT ALL ON SEQUENCE error_info_seq TO postgres;
GRANT SELECT ON SEQUENCE error_info_seq TO dumper;


--
-- Name: ip_segment_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE ip_segment_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE ip_segment_seq FROM postgres;
GRANT ALL ON SEQUENCE ip_segment_seq TO postgres;
GRANT SELECT ON SEQUENCE ip_segment_seq TO dumper;


--
-- Name: ip_segment; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE ip_segment FROM PUBLIC;
REVOKE ALL ON TABLE ip_segment FROM postgres;
GRANT ALL ON TABLE ip_segment TO postgres;
GRANT SELECT ON TABLE ip_segment TO dumper;


--
-- Name: keyword_pattern; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE keyword_pattern FROM PUBLIC;
REVOKE ALL ON TABLE keyword_pattern FROM postgres;
GRANT ALL ON TABLE keyword_pattern TO postgres;
GRANT SELECT ON TABLE keyword_pattern TO dumper;


--
-- Name: keyword_pattern_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE keyword_pattern_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE keyword_pattern_id_seq FROM postgres;
GRANT ALL ON SEQUENCE keyword_pattern_id_seq TO postgres;
GRANT SELECT ON SEQUENCE keyword_pattern_id_seq TO dumper;


--
-- Name: mlm_execution; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE mlm_execution FROM PUBLIC;
REVOKE ALL ON TABLE mlm_execution FROM postgres;
GRANT ALL ON TABLE mlm_execution TO postgres;
GRANT SELECT ON TABLE mlm_execution TO dumper;


--
-- Name: mlm_execution_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE mlm_execution_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE mlm_execution_seq FROM postgres;
GRANT ALL ON SEQUENCE mlm_execution_seq TO postgres;
GRANT SELECT ON SEQUENCE mlm_execution_seq TO dumper;


--
-- Name: offer; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer FROM PUBLIC;
REVOKE ALL ON TABLE offer FROM postgres;
GRANT ALL ON TABLE offer TO postgres;
GRANT SELECT ON TABLE offer TO dumper;


--
-- Name: offer_action; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer_action FROM PUBLIC;
REVOKE ALL ON TABLE offer_action FROM postgres;
GRANT ALL ON TABLE offer_action TO postgres;
GRANT SELECT ON TABLE offer_action TO dumper;


--
-- Name: offer_action_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE offer_action_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE offer_action_seq FROM postgres;
GRANT ALL ON SEQUENCE offer_action_seq TO postgres;
GRANT SELECT ON SEQUENCE offer_action_seq TO dumper;


--
-- Name: offer_category; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer_category FROM PUBLIC;
REVOKE ALL ON TABLE offer_category FROM postgres;
GRANT ALL ON TABLE offer_category TO postgres;
GRANT SELECT ON TABLE offer_category TO dumper;


--
-- Name: offer_grant; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer_grant FROM PUBLIC;
REVOKE ALL ON TABLE offer_grant FROM postgres;
GRANT ALL ON TABLE offer_grant TO postgres;
GRANT SELECT ON TABLE offer_grant TO dumper;


--
-- Name: offer_grant_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE offer_grant_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE offer_grant_seq FROM postgres;
GRANT ALL ON SEQUENCE offer_grant_seq TO postgres;
GRANT SELECT ON SEQUENCE offer_grant_seq TO dumper;


--
-- Name: offer_region; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer_region FROM PUBLIC;
REVOKE ALL ON TABLE offer_region FROM postgres;
GRANT ALL ON TABLE offer_region TO postgres;
GRANT SELECT ON TABLE offer_region TO dumper;


--
-- Name: offer_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE offer_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE offer_seq FROM postgres;
GRANT ALL ON SEQUENCE offer_seq TO postgres;
GRANT SELECT ON SEQUENCE offer_seq TO dumper;


--
-- Name: offer_stat; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE offer_stat FROM PUBLIC;
REVOKE ALL ON TABLE offer_stat FROM postgres;
GRANT ALL ON TABLE offer_stat TO postgres;
GRANT SELECT ON TABLE offer_stat TO dumper;


--
-- Name: offer_stat_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE offer_stat_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE offer_stat_seq FROM postgres;
GRANT ALL ON SEQUENCE offer_stat_seq TO postgres;
GRANT SELECT ON SEQUENCE offer_stat_seq TO dumper;


--
-- Name: setting; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE setting FROM PUBLIC;
REVOKE ALL ON TABLE setting FROM postgres;
GRANT ALL ON TABLE setting TO postgres;
GRANT SELECT ON TABLE setting TO dumper;


--
-- Name: site; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE site FROM PUBLIC;
REVOKE ALL ON TABLE site FROM postgres;
GRANT ALL ON TABLE site TO postgres;
GRANT SELECT ON TABLE site TO dumper;


--
-- Name: site_category; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE site_category FROM PUBLIC;
REVOKE ALL ON TABLE site_category FROM postgres;
GRANT ALL ON TABLE site_category TO postgres;
GRANT SELECT ON TABLE site_category TO dumper;


--
-- Name: site_region; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE site_region FROM PUBLIC;
REVOKE ALL ON TABLE site_region FROM postgres;
GRANT ALL ON TABLE site_region TO postgres;
GRANT SELECT ON TABLE site_region TO dumper;


--
-- Name: site_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE site_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE site_seq FROM postgres;
GRANT ALL ON SEQUENCE site_seq TO postgres;
GRANT SELECT ON SEQUENCE site_seq TO dumper;


--
-- Name: token; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE token FROM PUBLIC;
REVOKE ALL ON TABLE token FROM postgres;
GRANT ALL ON TABLE token TO postgres;
GRANT SELECT ON TABLE token TO dumper;


--
-- Name: token_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE token_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE token_seq FROM postgres;
GRANT ALL ON SEQUENCE token_seq TO postgres;
GRANT SELECT ON SEQUENCE token_seq TO dumper;


--
-- Name: user_profile; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE user_profile FROM PUBLIC;
REVOKE ALL ON TABLE user_profile FROM postgres;
GRANT ALL ON TABLE user_profile TO postgres;
GRANT SELECT ON TABLE user_profile TO dumper;


--
-- Name: user_profile_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE user_profile_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE user_profile_seq FROM postgres;
GRANT ALL ON SEQUENCE user_profile_seq TO postgres;
GRANT SELECT ON SEQUENCE user_profile_seq TO dumper;


--
-- Name: user_role; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE user_role FROM PUBLIC;
REVOKE ALL ON TABLE user_role FROM postgres;
GRANT ALL ON TABLE user_role TO postgres;
GRANT SELECT ON TABLE user_role TO dumper;


--
-- Name: withdraw; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE withdraw FROM PUBLIC;
REVOKE ALL ON TABLE withdraw FROM postgres;
GRANT ALL ON TABLE withdraw TO postgres;
GRANT SELECT ON TABLE withdraw TO dumper;


--
-- Name: withdraw_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE withdraw_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE withdraw_seq FROM postgres;
GRANT ALL ON SEQUENCE withdraw_seq TO postgres;
GRANT SELECT ON SEQUENCE withdraw_seq TO dumper;


--
-- PostgreSQL database dump complete
--

