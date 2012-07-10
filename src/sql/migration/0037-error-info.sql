--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: error_info; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE error_info (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    last_occurred timestamp without time zone NOT NULL,
    occurrence_count bigint NOT NULL,
    stack_trace character varying(10000),
    uri character varying(255) NOT NULL
);


ALTER TABLE public.error_info OWNER TO postgres;

--
-- Name: error_info_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE error_info_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.error_info_seq OWNER TO postgres;

--
-- Name: error_info_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY error_info
    ADD CONSTRAINT error_info_pkey PRIMARY KEY (id);


--
-- Name: error_info; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE error_info FROM PUBLIC;
REVOKE ALL ON TABLE error_info FROM postgres;
GRANT ALL ON TABLE error_info TO postgres;


--
-- Name: error_info_seq; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON SEQUENCE error_info_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE error_info_seq FROM postgres;
GRANT ALL ON SEQUENCE error_info_seq TO postgres;


--
-- PostgreSQL database dump complete
--

