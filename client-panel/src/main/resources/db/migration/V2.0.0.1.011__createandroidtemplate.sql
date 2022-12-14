create table if not EXISTS android_campaign(
id bigserial not null primary key,
client_id bigint not null,
appuser_id bigint not null,
template_id bigint not null,
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now(),
campaign_id bigint references campaign(id)
)
;
create table if not EXISTS notification_template_android(
id bigserial not null primary key,
client_id bigint not null,
name varchar(50) not null,
appuser_id bigint not null,
title varchar(100) not null,
body varchar(512) not null,
channel_id varchar(50),
channel_name varchar(50),
image_url varchar(512),
large_icon_url varchar(512),
deep_link varchar(512),
sound varchar(512),
badge_icon varchar(20),
collapse_key varchar(100),
priority varchar(10),
time_to_live bigint,
from_userndot boolean,
custom_key_value_pair text,
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
)
;
create table if not EXISTS notification_template_android_action(
id bigserial not null primary key,
action_id varchar(20) not null,
deep_link varchar(512),
label varchar(50) not null,
client_id bigint not NULL,
icon varchar(50),
auto_cancel boolean,
android_template_id bigint references notification_template_android(id),
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
)
;

create table if not EXISTS notification_template_webpush(
id bigserial not null primary key,
client_id bigint not null,
appuser_id bigint not null,
name varchar(100) not null,
title varchar(512) not null,
body text not null,
language varchar(20),
badge_url varchar(1024),
collapse_key VARCHAR(100),
icon_url varchar(1024),
image_url varchar(1024),
tag varchar(50),
require_interaction boolean default false,
urgency varchar(20),
time_to_live bigint,
deep_link varchar(1024),
custom_data_pair text,
from_userndot boolean default TRUE ,
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
);

create table if not EXISTS webpush_notification_action(
id bigserial not null primary key,
action varchar(1024),
title varchar(512) not null,
icon_url varchar(1024),
template_id bigint references notification_template_webpush(id),
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
);

create table if not EXISTS webpush_campaign_table(
id bigserial not null primary key,
client_id bigint not null,
appuser_id bigint not null,
template_id bigint not null,
campaign_id bigint references campaign(id),
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
);

ALTER sequence IF EXISTS notification_template_webpush_id_seq RESTART 1000 increment 1;
ALTER sequence  IF  EXISTS webpush_notification_action_id_seq RESTART 1000 increment 1;
ALTER sequence if EXISTS webpush_campaign_table_id_seq RESTART 1000 increment 1;

ALTER SEQUENCE IF EXISTS notification_template_android_id_seq RESTART 1000 INCREMENT 1;
ALTER SEQUENCE if EXISTS notification_template_android_action_id_seq RESTART 1000 INCREMENT 1;
ALTER SEQUENCE IF EXISTS android_campaign_id_seq RESTART 1000 INCREMENT 1;


create table if not exists fcm_failure_audit_log(
id bigserial not null PRIMARY KEY ,
client_id bigint not null,
message VARCHAR(1024) not NULL ,
status VARCHAR (1024) not NULL ,
error_code bigint,
type VARCHAR(50) NOT NULL ,
date_created TIMESTAMP WITH time ZONE DEFAULT now()
);

ALTER sequence fcm_failure_audit_log_id_seq RESTART 1000 increment 1;