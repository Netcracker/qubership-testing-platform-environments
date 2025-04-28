--
-- PostgreSQL database dump
--

-- Dumped from database version 14.2
-- Dumped by pg_dump version 14.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: remove_lonely_system(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.remove_lonely_system() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM SYSTEMS
    WHERE ID IN (
        SELECT TAB1.ID
        FROM SYSTEMS TAB1
        LEFT JOIN ENVIRONMENT_SYSTEMS TAB2
            ON TAB2.SYSTEM_ID = TAB1.ID
        WHERE
            TAB1.ID = OLD.SYSTEM_ID
            AND TAB2.ENVIRONMENT_ID IS NULL
    );
    RETURN NULL;
END;
$$;


--
-- Name: remove_old_temporary_environment(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.remove_old_temporary_environment() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 BEGIN
     DELETE FROM environments WHERE environments.project_id = NEW.project_id and environments.category_id =
     '884d7c4c-ba6a-4ec5-843a-4aa87225f7b4' and environments.created < now() - interval '2 week';
     RETURN NULL;
 END;
$$;


--
-- Name: remove_open_shift_dependency(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.remove_open_shift_dependency() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
     IF (OLD.source_template_id = 'e4b8a9aa-1952-4be7-8687-56d95f078d32') THEN
	     UPDATE CONNECTIONS  set PARAMETERS = PARAMETERS::jsonb || '{"root_synchronize_project":""}'::jsonb
		 WHERE SOURCE_TEMPLATE_ID = '2cb3b9e0-0067-46af-8f18-b103fbc19a73'
		 AND (PARAMETERS::jsonb->>'root_synchronize_project') = OLD.ID::text;
			END IF;
			RETURN NULL;
 END;
$$;


SET default_tablespace = '';

--
-- Name: alert_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alert_events (
    alert_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    entity_id uuid NOT NULL,
    tag_list character varying(256),
    status integer NOT NULL,
    last_updated timestamp without time zone NOT NULL
);


--
-- Name: TABLE alert_events; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.alert_events IS 'Table ALERT_EVENTS';


--
-- Name: alerts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alerts (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    short_description character varying(256),
    tag_list character varying(256),
    parameters text NOT NULL,
    subscriber_id uuid NOT NULL,
    status integer NOT NULL,
    created timestamp without time zone NOT NULL
);


--
-- Name: TABLE alerts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.alerts IS 'Table ALERTS';


--
-- Name: COLUMN alerts.parameters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.alerts.parameters IS 'body alert';


--
-- Name: connections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.connections (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(128),
    tag_list character varying(256),
    parameters text NOT NULL,
    connection_type character varying(20),
    created_by uuid,
    created timestamp without time zone,
    modified_by uuid,
    modified timestamp without time zone,
    system_id uuid,
    source_template_id uuid,
    services text,
    source_id uuid
);


--
-- Name: TABLE connections; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.connections IS 'Table systems';


--
-- Name: COLUMN connections.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.name IS 'Name connection';


--
-- Name: COLUMN connections.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.description IS 'Description connection';


--
-- Name: COLUMN connections.parameters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.parameters IS 'Parameters connection';


--
-- Name: COLUMN connections.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.created_by IS 'User_Id who created this connection';


--
-- Name: COLUMN connections.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.created IS 'Date created';


--
-- Name: COLUMN connections.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.modified_by IS 'User_Id who modified this connection last';


--
-- Name: COLUMN connections.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.modified IS 'Date changed';


--
-- Name: COLUMN connections.services; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.connections.services IS 'Used services on connection';


--
-- Name: database_directory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.database_directory (
    name character varying(32) NOT NULL,
    url_format character varying(128)
);


--
-- Name: TABLE database_directory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.database_directory IS 'Database Directory';


--
-- Name: COLUMN database_directory.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.database_directory.name IS 'Name database';


--
-- Name: COLUMN database_directory.url_format; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.database_directory.url_format IS 'JDBC URL format';


--
-- Name: environment_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.environment_categories (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(32) NOT NULL,
    description character varying(64),
    tag_list character varying(256),
    created_by character varying(64),
    created timestamp without time zone,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE environment_categories; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.environment_categories IS 'Environment categories';


--
-- Name: COLUMN environment_categories.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.name IS 'Name category';


--
-- Name: COLUMN environment_categories.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.description IS 'Description category';


--
-- Name: COLUMN environment_categories.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.created_by IS 'User_Id who created this category';


--
-- Name: COLUMN environment_categories.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.created IS 'Date created';


--
-- Name: COLUMN environment_categories.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.modified_by IS 'User_Id who changed this category';


--
-- Name: COLUMN environment_categories.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environment_categories.modified IS 'Date changed';


--
-- Name: environment_systems; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.environment_systems (
    environment_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    system_id uuid DEFAULT public.uuid_generate_v4() NOT NULL
);


--
-- Name: TABLE environment_systems; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.environment_systems IS 'Table systems';


--
-- Name: environments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.environments (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(128),
    tag_list character varying(256),
    created_by uuid,
    created timestamp without time zone,
    modified_by uuid,
    modified timestamp without time zone,
    project_id uuid NOT NULL,
    category_id uuid,
    graylog_name character varying,
    source_id uuid,
    ssm_solution_alias character varying,
    ssm_instance_alias character varying,
    consul_egress_config_path character varying,
    tags jsonb
);


--
-- Name: TABLE environments; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.environments IS 'Table systems';


--
-- Name: COLUMN environments.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.name IS 'Name category';


--
-- Name: COLUMN environments.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.description IS 'Description project';


--
-- Name: COLUMN environments.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN environments.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.created IS 'Date created';


--
-- Name: COLUMN environments.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN environments.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.modified IS 'Date modified';


--
-- Name: COLUMN environments.tags; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.environments.tags IS 'Specified tags on environment';


--
-- Name: history_store; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.history_store (
    entity_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    table_name character varying(64) NOT NULL,
    change_event jsonb NOT NULL,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE history_store; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.history_store IS 'Table history operation';


--
-- Name: COLUMN history_store.table_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.history_store.table_name IS 'Table name where were the changes';


--
-- Name: COLUMN history_store.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.history_store.modified_by IS 'User_Id who modified this connection last';


--
-- Name: COLUMN history_store.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.history_store.modified IS 'Date changed';


--
-- Name: jv_commit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jv_commit (
    commit_pk bigint NOT NULL,
    author character varying(200),
    commit_date timestamp without time zone,
    commit_date_instant character varying(30),
    commit_id numeric(22,2)
);


--
-- Name: jv_commit_pk_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.jv_commit_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: jv_commit_property; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jv_commit_property (
    property_name character varying(191) NOT NULL,
    property_value character varying(600),
    commit_fk bigint NOT NULL
);


--
-- Name: jv_global_id; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jv_global_id (
    global_id_pk bigint NOT NULL,
    local_id character varying(191),
    fragment character varying(200),
    type_name character varying(200),
    owner_id_fk bigint
);


--
-- Name: jv_global_id_pk_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.jv_global_id_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: jv_snapshot; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jv_snapshot (
    snapshot_pk bigint NOT NULL,
    type character varying(200),
    version bigint,
    state text,
    changed_properties text,
    managed_type character varying(200),
    global_id_fk bigint,
    commit_fk bigint
);


--
-- Name: jv_snapshot_pk_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.jv_snapshot_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.projects (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    short_name character varying(64) NOT NULL,
    description character varying(128),
    external_ref character varying(256),
    tag_list character varying(256),
    created_by character varying(32),
    created timestamp without time zone,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE projects; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.projects IS 'Table projects';


--
-- Name: COLUMN projects.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.name IS 'Name project';


--
-- Name: COLUMN projects.short_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.short_name IS 'Short name project';


--
-- Name: COLUMN projects.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.description IS 'Description project';


--
-- Name: COLUMN projects.external_ref; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.external_ref IS 'External link to the JIRA project';


--
-- Name: COLUMN projects.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN projects.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.created IS 'Date created';


--
-- Name: COLUMN projects.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN projects.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.projects.modified IS 'Date modified';


--
-- Name: role_grants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_grants (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    entity_id character varying(128) NOT NULL,
    read boolean,
    write boolean
);


--
-- Name: TABLE role_grants; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.role_grants IS 'Table projects';


--
-- Name: COLUMN role_grants.entity_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role_grants.entity_id IS 'Id Project or Environment or System etc';


--
-- Name: COLUMN role_grants.read; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role_grants.read IS 'Permission to view the entity';


--
-- Name: COLUMN role_grants.write; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role_grants.write IS 'Permission to edit the entity';


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(128),
    tag_list character varying(256),
    parent_id uuid DEFAULT public.uuid_generate_v4(),
    created_by character varying(32),
    created timestamp without time zone,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE roles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.roles IS 'Table projects';


--
-- Name: COLUMN roles.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN roles.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.created IS 'Date created';


--
-- Name: COLUMN roles.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN roles.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.modified IS 'Date modified';


--
-- Name: shedlock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.shedlock (
    name character varying(64) NOT NULL,
    lock_until timestamp without time zone,
    locked_at timestamp without time zone,
    locked_by character varying(255)
);


--
-- Name: subscribers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscribers (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    host character varying(64) NOT NULL,
    subscriber_type integer NOT NULL,
    signature character varying(256) NOT NULL,
    tag_list character varying(256),
    host_status integer,
    notification_url character varying(256) NOT NULL,
    registration_date timestamp without time zone NOT NULL
);


--
-- Name: TABLE subscribers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.subscribers IS 'Table SUBSCRIBERS for registration TA-tools';


--
-- Name: COLUMN subscribers.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.subscribers.name IS 'Name TA-Tool';


--
-- Name: COLUMN subscribers.subscriber_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.subscribers.subscriber_type IS 'enum: BV, ITF4.0, Executor, ATP1.0';


--
-- Name: COLUMN subscribers.host_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.subscribers.host_status IS 'Host Status';


--
-- Name: COLUMN subscribers.notification_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.subscribers.notification_url IS 'for send JSON';


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscriptions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    subscription_type integer NOT NULL,
    project_id uuid,
    environment_id uuid,
    system_id uuid,
    subscriber_id uuid NOT NULL,
    status integer NOT NULL,
    last_updated timestamp without time zone NOT NULL
);


--
-- Name: TABLE subscriptions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.subscriptions IS 'Table SUBSCRIPTION';


--
-- Name: system_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_categories (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(32) NOT NULL,
    description character varying(128),
    tag_list character varying(256),
    created_by character varying(32),
    created timestamp without time zone,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE system_categories; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.system_categories IS 'Table system categories';


--
-- Name: COLUMN system_categories.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.name IS 'Name category';


--
-- Name: COLUMN system_categories.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.description IS 'Description project';


--
-- Name: COLUMN system_categories.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN system_categories.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.created IS 'Date created';


--
-- Name: COLUMN system_categories.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN system_categories.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_categories.modified IS 'Date modified';


--
-- Name: systems; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.systems (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(128),
    tag_list character varying(256),
    created_by uuid,
    created timestamp without time zone,
    modified_by uuid,
    modified timestamp without time zone,
    category_id uuid,
    status character varying,
    date_of_last_check timestamp without time zone,
    version character varying,
    date_of_check_version timestamp without time zone,
    parameters_getting_version text,
    parent_system_id uuid,
    server_itf text,
    merge_by_name boolean,
    link_to_system_id uuid,
    external_id uuid,
    source_id uuid,
    external_name character varying
);


--
-- Name: TABLE systems; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.systems IS 'Table systems';


--
-- Name: COLUMN systems.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.name IS 'Name category';


--
-- Name: COLUMN systems.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.description IS 'Description project';


--
-- Name: COLUMN systems.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN systems.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.created IS 'Date created';


--
-- Name: COLUMN systems.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN systems.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.modified IS 'Date modified';


--
-- Name: COLUMN systems.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.version IS 'Parent system';


--
-- Name: COLUMN systems.date_of_check_version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.date_of_check_version IS 'Date of last check version';


--
-- Name: COLUMN systems.parameters_getting_version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.parameters_getting_version IS 'Parameters getting versions';


--
-- Name: COLUMN systems.server_itf; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.systems.server_itf IS 'Parameters for integration with ITF';


--
-- Name: tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tags (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(64) NOT NULL
);


--
-- Name: TABLE tags; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tags IS 'Table tags';


--
-- Name: COLUMN tags.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.tags.name IS 'Name project';


--
-- Name: update_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.update_events (
    subscription_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    tag_list character varying(256),
    status integer NOT NULL,
    last_event_date timestamp without time zone NOT NULL,
    entity_type character varying(50) DEFAULT 'undefined'::character varying NOT NULL
);


--
-- Name: TABLE update_events; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.update_events IS 'Table UPDATE_EVENTS';


--
-- Name: user_projects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_projects (
    project_id uuid NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: TABLE user_projects; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_projects IS 'Table systems';


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


--
-- Name: TABLE user_roles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_roles IS 'Table systems';


--
-- Name: user_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_settings (
    user_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    view character varying(32) NOT NULL
);


--
-- Name: TABLE user_settings; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_settings IS 'Table projects';


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(32) NOT NULL,
    first_name character varying(32) NOT NULL,
    last_name character varying(64) NOT NULL,
    "E-MAIL" character varying(32),
    description character varying(128),
    provider character varying(20),
    tag_list character varying(256),
    created_by character varying(32),
    created timestamp without time zone,
    modified_by character varying(32),
    modified timestamp without time zone
);


--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.users IS 'Table projects';


--
-- Name: COLUMN users.created_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.users.created_by IS 'User_Id who created this project';


--
-- Name: COLUMN users.created; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.users.created IS 'Date created';


--
-- Name: COLUMN users.modified_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.users.modified_by IS 'User_Id who modified this project';


--
-- Name: COLUMN users.modified; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.users.modified IS 'Date modified';


--
-- Name: connections UK_CONNECTIONS(NAME,SYSTEM_ID); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT "UK_CONNECTIONS(NAME,SYSTEM_ID)" UNIQUE (name, system_id);


--
-- Name: environments UK_ENVIRONMENTS(NAME,PROJECT_ID); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environments
    ADD CONSTRAINT "UK_ENVIRONMENTS(NAME,PROJECT_ID)" UNIQUE (name, project_id);


--
-- Name: environment_categories UK_ENVIRONMENT_CATEGORIES(NAME); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_categories
    ADD CONSTRAINT "UK_ENVIRONMENT_CATEGORIES(NAME)" UNIQUE (name);


--
-- Name: environment_systems UK_ENVIRONMENT_SYSTEMS(ENVIRONMENT_ID,SYSTEM_ID); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_systems
    ADD CONSTRAINT "UK_ENVIRONMENT_SYSTEMS(ENVIRONMENT_ID,SYSTEM_ID)" UNIQUE (environment_id, system_id);


--
-- Name: projects UK_PROJECTS(NAME); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT "UK_PROJECTS(NAME)" UNIQUE (name);


--
-- Name: roles UK_ROLES(NAME); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT "UK_ROLES(NAME)" UNIQUE (name);


--
-- Name: subscribers UK_SUBSCRIBERS(NAME); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscribers
    ADD CONSTRAINT "UK_SUBSCRIBERS(NAME)" UNIQUE (name);


--
-- Name: system_categories UK_SYSTEM_CATEGORIES(NAME); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_categories
    ADD CONSTRAINT "UK_SYSTEM_CATEGORIES(NAME)" UNIQUE (name);


--
-- Name: user_projects UK_USER_PROJECTS(PROJECT_ID,USER_ID); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_projects
    ADD CONSTRAINT "UK_USER_PROJECTS(PROJECT_ID,USER_ID)" UNIQUE (project_id, user_id);


--
-- Name: user_roles UK_USER_ROLES(ROLE_ID,USER_ID); Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "UK_USER_ROLES(ROLE_ID,USER_ID)" UNIQUE (role_id, user_id);


--
-- Name: user_settings USER_SETTINGS_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_settings
    ADD CONSTRAINT "USER_SETTINGS_pkey" PRIMARY KEY (user_id);


--
-- Name: jv_commit jv_commit_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_commit
    ADD CONSTRAINT jv_commit_pk PRIMARY KEY (commit_pk);


--
-- Name: jv_commit_property jv_commit_property_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_commit_property
    ADD CONSTRAINT jv_commit_property_pk PRIMARY KEY (commit_fk, property_name);


--
-- Name: jv_global_id jv_global_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_global_id
    ADD CONSTRAINT jv_global_id_pk PRIMARY KEY (global_id_pk);


--
-- Name: jv_snapshot jv_snapshot_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_pk PRIMARY KEY (snapshot_pk);


--
-- Name: alerts pk_alerts; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alerts
    ADD CONSTRAINT pk_alerts PRIMARY KEY (id);


--
-- Name: connections pk_connections; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT pk_connections PRIMARY KEY (id);


--
-- Name: database_directory pk_database_directory; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.database_directory
    ADD CONSTRAINT pk_database_directory PRIMARY KEY (name);


--
-- Name: environment_categories pk_environment_categories; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_categories
    ADD CONSTRAINT pk_environment_categories PRIMARY KEY (id);


--
-- Name: environments pk_environments; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environments
    ADD CONSTRAINT pk_environments PRIMARY KEY (id);


--
-- Name: history_store pk_history_store; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.history_store
    ADD CONSTRAINT pk_history_store PRIMARY KEY (entity_id);


--
-- Name: projects pk_projects; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT pk_projects PRIMARY KEY (id);


--
-- Name: roles pk_roles; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT pk_roles PRIMARY KEY (id);


--
-- Name: subscribers pk_subscribers; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscribers
    ADD CONSTRAINT pk_subscribers PRIMARY KEY (id);


--
-- Name: subscriptions pk_subscriptions; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT pk_subscriptions PRIMARY KEY (id);


--
-- Name: system_categories pk_system_categories; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_categories
    ADD CONSTRAINT pk_system_categories PRIMARY KEY (id);


--
-- Name: systems pk_systems; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.systems
    ADD CONSTRAINT pk_systems PRIMARY KEY (id);


--
-- Name: tags pk_tags; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT pk_tags PRIMARY KEY (id);


--
-- Name: users pk_users; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);


--
-- Name: shedlock shedlock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);


--
-- Name: alert_events unique FK's combination alert_events; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_events
    ADD CONSTRAINT "unique FK's combination alert_events" UNIQUE (entity_id, alert_id);


--
-- Name: update_events unique FK's combination update_events; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.update_events
    ADD CONSTRAINT "unique FK's combination update_events" UNIQUE (entity_id, subscription_id);


--
-- Name: subscriptions unique entity's combination subscription; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT "unique entity's combination subscription" UNIQUE (project_id, environment_id, system_id, subscriber_id);


--
-- Name: IDX_ALERTS(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERTS(NAME)" ON public.alerts USING btree (name);


--
-- Name: IDX_ALERTS(STATUS); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERTS(STATUS)" ON public.alerts USING btree (status);


--
-- Name: IDX_ALERTS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERTS(TAG_LIST)" ON public.alerts USING btree (tag_list);


--
-- Name: IDX_ALERT_EVENTS(ENTITY_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERT_EVENTS(ENTITY_ID)" ON public.alert_events USING btree (entity_id);


--
-- Name: IDX_ALERT_EVENTS(STATUS); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERT_EVENTS(STATUS)" ON public.alert_events USING btree (status);


--
-- Name: IDX_ALERT_EVENTS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ALERT_EVENTS(TAG_LIST)" ON public.alert_events USING btree (tag_list);


--
-- Name: IDX_CONNECTIONS(CONNECTION_TYPE); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_CONNECTIONS(CONNECTION_TYPE)" ON public.connections USING btree (connection_type);


--
-- Name: IDX_CONNECTIONS(NAME,SYSTEM_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_CONNECTIONS(NAME,SYSTEM_ID)" ON public.connections USING btree (name, system_id);


--
-- Name: IDX_CONNECTIONS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_CONNECTIONS(TAG_LIST)" ON public.connections USING btree (tag_list);


--
-- Name: IDX_DATABASE_DIRECTORY(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_DATABASE_DIRECTORY(NAME)" ON public.database_directory USING btree (name);


--
-- Name: IDX_ENVIRONMENTS(NAME,PROJECT_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_ENVIRONMENTS(NAME,PROJECT_ID)" ON public.environments USING btree (name, project_id, category_id);


--
-- Name: IDX_ENVIRONMENTS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ENVIRONMENTS(TAG_LIST)" ON public.environments USING btree (tag_list);


--
-- Name: IDX_ENVIRONMENT_CATEGORIES(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_ENVIRONMENT_CATEGORIES(NAME)" ON public.environment_categories USING btree (name);


--
-- Name: IDX_ENVIRONMENT_CATEGORIES(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ENVIRONMENT_CATEGORIES(TAG_LIST)" ON public.environment_categories USING btree (tag_list);


--
-- Name: IDX_ENVIRONMENT_SYSTEMS(SYSTEM_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ENVIRONMENT_SYSTEMS(SYSTEM_ID)" ON public.environment_systems USING btree (system_id);


--
-- Name: IDX_HISTORY_STORE(ENTITY_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_HISTORY_STORE(ENTITY_ID)" ON public.history_store USING btree (entity_id);


--
-- Name: IDX_HISTORY_STORE(TABLE_NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_HISTORY_STORE(TABLE_NAME)" ON public.history_store USING btree (table_name);


--
-- Name: IDX_PROJECTS(EXTERNAL_REF); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_PROJECTS(EXTERNAL_REF)" ON public.projects USING btree (external_ref);


--
-- Name: IDX_PROJECTS(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_PROJECTS(NAME)" ON public.projects USING btree (name);


--
-- Name: IDX_PROJECTS(SHORT_NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_PROJECTS(SHORT_NAME)" ON public.projects USING btree (short_name);


--
-- Name: IDX_PROJECTS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_PROJECTS(TAG_LIST)" ON public.projects USING btree (tag_list);


--
-- Name: IDX_ROLES(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_ROLES(NAME)" ON public.roles USING btree (name);


--
-- Name: IDX_ROLES(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ROLES(TAG_LIST)" ON public.roles USING btree (tag_list);


--
-- Name: IDX_ROLE_GRANTS(ENTITY_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_ROLE_GRANTS(ENTITY_ID)" ON public.role_grants USING btree (entity_id);


--
-- Name: IDX_SUBSCRIBERS(HOST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIBERS(HOST)" ON public.subscribers USING btree (host);


--
-- Name: IDX_SUBSCRIBERS(HOST_STATUS); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIBERS(HOST_STATUS)" ON public.subscribers USING btree (host_status);


--
-- Name: IDX_SUBSCRIBERS(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIBERS(NAME)" ON public.subscribers USING btree (name);


--
-- Name: IDX_SUBSCRIBERS(REGISTRATION_DATE); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIBERS(REGISTRATION_DATE)" ON public.subscribers USING btree (registration_date);


--
-- Name: IDX_SUBSCRIBERS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIBERS(TAG_LIST)" ON public.subscribers USING btree (tag_list);


--
-- Name: IDX_SUBSCRIPTIONS(PROJECT_ID,ENVIRONMENT_ID,SYSTEM_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIPTIONS(PROJECT_ID,ENVIRONMENT_ID,SYSTEM_ID)" ON public.subscriptions USING btree (project_id, environment_id, system_id);


--
-- Name: IDX_SUBSCRIPTIONS(STATUS); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIPTIONS(STATUS)" ON public.subscriptions USING btree (status);


--
-- Name: IDX_SUBSCRIPTIONS(SUBSCRIPTION_TYPE); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SUBSCRIPTIONS(SUBSCRIPTION_TYPE)" ON public.subscriptions USING btree (subscription_type);


--
-- Name: IDX_SYSTEMS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SYSTEMS(TAG_LIST)" ON public.systems USING btree (tag_list);


--
-- Name: IDX_SYSTEM_CATEGORIES(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_SYSTEM_CATEGORIES(NAME)" ON public.system_categories USING btree (name);


--
-- Name: IDX_SYSTEM_CATEGORIES(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_SYSTEM_CATEGORIES(TAG_LIST)" ON public.system_categories USING btree (tag_list);


--
-- Name: IDX_TAGS(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_TAGS(NAME)" ON public.tags USING btree (name);


--
-- Name: IDX_UPDATE_EVENTS(ENTITY_ID); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_UPDATE_EVENTS(ENTITY_ID)" ON public.update_events USING btree (entity_id);


--
-- Name: IDX_UPDATE_EVENTS(STATUS); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_UPDATE_EVENTS(STATUS)" ON public.update_events USING btree (status);


--
-- Name: IDX_UPDATE_EVENTS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_UPDATE_EVENTS(TAG_LIST)" ON public.update_events USING btree (tag_list);


--
-- Name: IDX_USERS(E-MAIL); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_USERS(E-MAIL)" ON public.users USING btree ("E-MAIL");


--
-- Name: IDX_USERS(FIRST_NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_USERS(FIRST_NAME)" ON public.users USING btree (first_name);


--
-- Name: IDX_USERS(LAST_NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_USERS(LAST_NAME)" ON public.users USING btree (last_name);


--
-- Name: IDX_USERS(NAME); Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX "IDX_USERS(NAME)" ON public.users USING btree (name);


--
-- Name: IDX_USERS(PROVIDER); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_USERS(PROVIDER)" ON public.users USING btree (provider);


--
-- Name: IDX_USERS(TAG_LIST); Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "IDX_USERS(TAG_LIST)" ON public.users USING btree (tag_list);


--
-- Name: jv_commit_commit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_commit_commit_id_idx ON public.jv_commit USING btree (commit_id);


--
-- Name: jv_commit_property_commit_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_commit_property_commit_fk_idx ON public.jv_commit_property USING btree (commit_fk);


--
-- Name: jv_commit_property_property_name_property_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_commit_property_property_name_property_value_idx ON public.jv_commit_property USING btree (property_name, property_value);


--
-- Name: jv_global_id_local_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_global_id_local_id_idx ON public.jv_global_id USING btree (local_id);


--
-- Name: jv_global_id_owner_id_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_global_id_owner_id_fk_idx ON public.jv_global_id USING btree (owner_id_fk);


--
-- Name: jv_snapshot_commit_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_snapshot_commit_fk_idx ON public.jv_snapshot USING btree (commit_fk);


--
-- Name: jv_snapshot_global_id_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jv_snapshot_global_id_fk_idx ON public.jv_snapshot USING btree (global_id_fk);


--
-- Name: environment_systems remove_lonely_system; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER remove_lonely_system AFTER DELETE ON public.environment_systems FOR EACH ROW EXECUTE PROCEDURE public.remove_lonely_system();


--
-- Name: environments remove_old_temporary_environment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER remove_old_temporary_environment AFTER INSERT ON public.environments FOR EACH ROW EXECUTE PROCEDURE public.remove_old_temporary_environment();


--
-- Name: connections remove_open_shift_dependency; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER remove_open_shift_dependency AFTER DELETE ON public.connections FOR EACH ROW EXECUTE PROCEDURE public.remove_open_shift_dependency();


--
-- Name: alerts FK_ALERTS(SUBSCRIBER_ID)-SUBSCRIBERS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alerts
    ADD CONSTRAINT "FK_ALERTS(SUBSCRIBER_ID)-SUBSCRIBERS(ID)" FOREIGN KEY (subscriber_id) REFERENCES public.subscribers(id) ON DELETE CASCADE;


--
-- Name: alert_events FK_ALERT_EVENTS(ALERT_ID)-ALERTS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_events
    ADD CONSTRAINT "FK_ALERT_EVENTS(ALERT_ID)-ALERTS(ID)" FOREIGN KEY (alert_id) REFERENCES public.alerts(id) ON DELETE CASCADE;


--
-- Name: connections FK_CONNECTIONS(SYSTEM_ID)-CONNECTIONS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT "FK_CONNECTIONS(SYSTEM_ID)-CONNECTIONS(ID)" FOREIGN KEY (source_template_id) REFERENCES public.connections(id) ON DELETE SET NULL;


--
-- Name: connections FK_CONNECTIONS(SYSTEM_ID)-SYSTEMS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT "FK_CONNECTIONS(SYSTEM_ID)-SYSTEMS(ID)" FOREIGN KEY (system_id) REFERENCES public.systems(id) ON DELETE CASCADE;


--
-- Name: environments FK_ENVIRONMENTS(CATEGORY_ID)-ENVIRONMENT_CATEGORIES(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environments
    ADD CONSTRAINT "FK_ENVIRONMENTS(CATEGORY_ID)-ENVIRONMENT_CATEGORIES(ID)" FOREIGN KEY (category_id) REFERENCES public.environment_categories(id) ON DELETE SET NULL;


--
-- Name: environments FK_ENVIRONMENTS(PROJECT_ID)-PROJECTS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environments
    ADD CONSTRAINT "FK_ENVIRONMENTS(PROJECT_ID)-PROJECTS(ID)" FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE RESTRICT;


--
-- Name: environment_systems FK_ENVIRONMENT_SYSTEMS(ENVIRONMENT_ID)-ENVIRONMENTS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_systems
    ADD CONSTRAINT "FK_ENVIRONMENT_SYSTEMS(ENVIRONMENT_ID)-ENVIRONMENTS(ID)" FOREIGN KEY (environment_id) REFERENCES public.environments(id) ON DELETE CASCADE;


--
-- Name: environment_systems FK_ENVIRONMENT_SYSTEMS(SYSTEM_ID)-SYSTEMS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_systems
    ADD CONSTRAINT "FK_ENVIRONMENT_SYSTEMS(SYSTEM_ID)-SYSTEMS(ID)" FOREIGN KEY (system_id) REFERENCES public.systems(id) ON DELETE CASCADE;


--
-- Name: roles FK_ROLES(PARENT_ID)-ROLES(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT "FK_ROLES(PARENT_ID)-ROLES(ID)" FOREIGN KEY (parent_id) REFERENCES public.roles(id);


--
-- Name: role_grants FK_ROLE_GRANTS(ID)-ROLES(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_grants
    ADD CONSTRAINT "FK_ROLE_GRANTS(ID)-ROLES(ID)" FOREIGN KEY (id) REFERENCES public.roles(id);


--
-- Name: subscriptions FK_SUBSCRIPTIONS(SUBSCRIBER_ID)-SUBSCRIBERS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT "FK_SUBSCRIPTIONS(SUBSCRIBER_ID)-SUBSCRIBERS(ID)" FOREIGN KEY (subscriber_id) REFERENCES public.subscribers(id) ON DELETE CASCADE;


--
-- Name: systems FK_SYSTEMS(CATEGORY_ID)-SYSTEM_CATEGORIES(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.systems
    ADD CONSTRAINT "FK_SYSTEMS(CATEGORY_ID)-SYSTEM_CATEGORIES(ID)" FOREIGN KEY (category_id) REFERENCES public.system_categories(id) ON DELETE SET NULL;


--
-- Name: systems FK_SYSTEMS(PARENT_SYSTEM_ID)-SYSTEM(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.systems
    ADD CONSTRAINT "FK_SYSTEMS(PARENT_SYSTEM_ID)-SYSTEM(ID)" FOREIGN KEY (parent_system_id) REFERENCES public.systems(id) ON DELETE SET NULL;


--
-- Name: update_events FK_UPDATE_EVENTS(SUBSCRIPTION_ID)-SUBSCRIPTIONS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.update_events
    ADD CONSTRAINT "FK_UPDATE_EVENTS(SUBSCRIPTION_ID)-SUBSCRIPTIONS(ID)" FOREIGN KEY (subscription_id) REFERENCES public.subscriptions(id) ON DELETE CASCADE;


--
-- Name: user_projects FK_USER_PROJECTS(PROJECT_ID)-PROJECTS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_projects
    ADD CONSTRAINT "FK_USER_PROJECTS(PROJECT_ID)-PROJECTS(ID)" FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: user_projects FK_USER_PROJECTS(USER_ID)-USERS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_projects
    ADD CONSTRAINT "FK_USER_PROJECTS(USER_ID)-USERS(ID)" FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_roles FK_USER_ROLES(PROJECT_ID)-ROLES(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "FK_USER_ROLES(PROJECT_ID)-ROLES(ID)" FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: user_roles FK_USER_ROLES(PROJECT_ID)-USERS(ID); Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT "FK_USER_ROLES(PROJECT_ID)-USERS(ID)" FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: jv_commit_property jv_commit_property_commit_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_commit_property
    ADD CONSTRAINT jv_commit_property_commit_fk FOREIGN KEY (commit_fk) REFERENCES public.jv_commit(commit_pk);


--
-- Name: jv_global_id jv_global_id_owner_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_global_id
    ADD CONSTRAINT jv_global_id_owner_id_fk FOREIGN KEY (owner_id_fk) REFERENCES public.jv_global_id(global_id_pk);


--
-- Name: jv_snapshot jv_snapshot_constraint; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_constraint FOREIGN KEY (commit_fk) REFERENCES public.jv_commit(commit_pk);


--
-- Name: jv_snapshot jv_snapshot_global_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jv_snapshot
    ADD CONSTRAINT jv_snapshot_global_id_fk FOREIGN KEY (global_id_fk) REFERENCES public.jv_global_id(global_id_pk);


--
-- PostgreSQL database dump complete
--

