

create table if not exists client
(
  id serial not null
    constraint appuser_verification_pkey
    primary key,
  name varchar(512) not null,
  state smallint default 1 not null,
  email varchar(512) not null,
  phone varchar(20),
  email_verified boolean default false,
  phone_verified boolean default false,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  address text,
  firstname varchar(255),
  lastname varchar(255),
  country varchar(255)
)
;

create table if not exists client_verification
(
  id serial not null
    constraint client_verification_pkey
    primary key,
  client_id bigint not null
    constraint client_verification_client_id_fkey
    references client,
  email_code varchar(512),
  email_code_date timestamp with time zone,
  phone_otp varchar(8),
  phone_otp_date timestamp with time zone
)
;

create table if not exists appuser
(
  id bigserial not null
    constraint appuser_pkey
    primary key,
  client_id bigint not null
    constraint appuser_client_id_fkey
    references client,
  secret varchar(2048) default 'mySecret'::character varying not null,
  email varchar(512) not null,
  enabled boolean not null,
  firstname varchar(512) not null,
  key varchar(2048),
  lastpasswordresetdate timestamp not null,
  lastname varchar(512) not null,
  password varchar(100) not null,
  user_type varchar(40) not null,
  phone varchar(20),
  username varchar(512) not null
    constraint uk_418sr20kh207kb22viuyno1
    unique,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists authority
(
  id bigserial not null
    constraint authority_pkey
    primary key,
  name varchar(50) not null,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists user_authority
(
  user_id bigint not null
    constraint fkjk083dm06nfs1ycs8jeyjevdy
    references appuser,
  authority_id bigint not null
    constraint fkgvxjs381k6f48d5d2yi11uh89
    references authority,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists email_template
(
  id bigserial not null
    constraint email_template_pkey
    primary key,
  client_id bigint not null
    constraint email_template_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint email_template_appuser_id_fkey
    references appuser,
  parent_id bigint,
  from_user varchar(200),
  message_type varchar(40) not null,
  tags varchar(200),
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  name varchar(100) not null,
  email_template_subject integer,
  email_template_body integer,
  editor_selected varchar
)
;

create unique index if not exists email_template_name
  on email_template (client_id, name)
;

create table if not exists campaign
(
  id bigserial not null
    constraint campaign_pkey
    primary key,
  client_id bigint not null
    constraint campaign_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint campaign_appuser_id_fkey
    references appuser,
  campaign_type varchar(40) not null,
  segmentation_id bigint not null,
  schedule text not null,
  campaign_status varchar(40),
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  name varchar(256) not null
)
;

create table if not exists email_campaign
(
  id bigserial not null
    constraint email_campaign_pkey
    primary key,
  client_id bigint not null
    constraint email_campaign_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint email_campaign_appuser_id_fkey
    references appuser,
  campaign_id bigint not null
    constraint email_campaign_campaign_id_fkey
    references campaign,
  email_template_id bigint not null
    constraint email_campaign_email_template_id_fkey
    references email_template,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists campign_trigger
(
  id bigserial not null
    constraint campaign_trigger_pkey
    primary key,
  client_id bigint not null
    constraint campign_trigger_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint campign_trigger_appuser_id_fkey
    references appuser,
  campaign_id bigint not null
    constraint campign_trigger_campaign_id_fkey
    references campaign,
  trigger_time timestamp not null,
  trigger_status varchar(40) not null,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists service_provider_credentials
(
  id bigserial not null
    constraint service_provider_credentials_pkey
    primary key,
  client_id bigint not null
    constraint service_provider_credentials_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint service_provider_credentials_appuser_id_fkey
    references appuser,
  service_provider_type varchar(40) not null,
  service_provider varchar(40) not null,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  status varchar(20),
  credentials text
)
;

create table if not exists sms_template
(
  id bigserial not null
    constraint sms_template_pkey
    primary key,
  client_id bigint not null
    constraint sms_template_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint sms_template_appuser_id_fkey
    references appuser,
  sms_template_body text not null,
  parent_id bigint,
  from_user varchar(200),
  message_type varchar(40) not null,
  tags varchar(200),
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  status varchar(20) not null,
  name varchar(100) not null
)
;

create table if not exists countries
(
  id serial not null
    constraint countries_pkey
    primary key,
  shortname varchar(3) not null,
  name varchar(150) not null,
  phonecode integer not null
)
;

create table if not exists states
(
  id serial not null
    constraint states_pkey
    primary key,
  name varchar(100) not null,
  country_id integer not null
)
;

create table if not exists cities
(
  id serial not null
    constraint cities_pkey
    primary key,
  name varchar(100) not null,
  state_id integer not null
)
;

create table if not exists segment
(
  id bigserial not null
    constraint segment_pkey
    primary key,
  client_id bigint not null
    constraint segment_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint segment_appuser_id_fkey
    references appuser,
  name varchar(200),
  data text not null,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now(),
  status varchar(20),
  type varchar(100) not null,
  conversion_event varchar(256) not null
)
;

create table if not exists sms_campaign
(
  id bigserial not null
    constraint sms_campaign_pkey
    primary key,
  client_id bigint not null
    constraint sms_campaign_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint sms_campaign_appuser_id_fkey
    references appuser,
  campaign_id bigint not null
    constraint sms_campaign_campaign_id_fkey
    references campaign,
  sms_template_id bigint not null
    constraint sms_campaign_sms_template_id_fkey
    references sms_template,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists client_settings
(
  id serial not null
    constraint client_settings_pkey
    primary key,
  client_id bigint not null
    constraint client_settings_client_id_key
    unique
    constraint client_settings_client_id_fkey
    references client,
  authorized_urls text,
  timezone varchar(50),
  date_created timestamp with time zone default now() not null,
  date_modified timestamp with time zone default now() not null,
  unsubscribe_link text
)
;

create table if not exists campaign_audit_log
(
  id bigserial not null
    constraint campaign_audit_logs_pkey
    primary key,
  client_id bigint not null
    constraint campaign_audit_log_client_id_fkey
    references client,
  campaign_id bigint not null
    constraint campaign_audit_log_campaign_id_fkey
    references campaign,
  status varchar(20),
  action varchar(20),
  message text,
  date_created timestamp with time zone default now()
)
;

create table if not exists contact_us
(
  id bigserial not null
    constraint contact_us_pkey
    primary key,
  name varchar(200),
  email varchar(512),
  mobile_no varchar(20),
  message varchar,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create unique index if not exists contact_us_email_uindex
  on contact_us (email)
;

create table if not exists template
(
  id bigserial not null
    constraint template_pkey
    primary key,
  client_id bigint not null
    constraint template_client_id_fkey
    references client,
  appuser_id bigint not null
    constraint template_appuser_id_fkey
    references appuser,
  template text not null,
  name varchar not null,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create table if not exists client_setting_email
(
  id bigserial not null
    constraint client_setting_email_pkey
    primary key,
  client_id bigint not null
    constraint client_setting_email_client_id_fkey
    references client,
  address varchar(200),
  email varchar(512),
  verified boolean default false,
  deleted boolean default false,
  date_created timestamp with time zone default now(),
  date_modified timestamp with time zone default now()
)
;

create unique index if not exists client_setting_email_client_id_email__deleted_null
  on client_setting_email (client_id, email)
  where (deleted IS FALSE)
;

