create table android_campaign(
id bigserial not null primary key,
client_id bigint not null,
appuser_id bigint not null,
template_id bigint not null,
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now(),
campaign_id bigint references campaign(id)
)
;
create table notification_template_android(
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
create table notification_template_android_action(
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

create table notification_template_webpush(
id bigint not null primary key,
client_id bigint not null,
appuser_id bigint not null,
name varchar(100) not null,
title varchar(512) not null,
body text not null,
language varchar(20),
badge_url varchar(1024),
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

create table webpush_notification_action(
id bigint not null primary key,
action varchar(1024),
title varchar(512) not null,
icon_url varchar(1024),
template_id bigint references notification_template_webpush(id),
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
);

create table webpush_campaign_table(
id bigserial not null primary key,
client_id bigint not null,
appuser_id bigint not null,
template_id bigint not null,
campaign_id bigint references campaign(id),
creation_date timestamp with time zone DEFAULT now(),
date_modified timestamp with time zone DEFAULT now()
);

create sequence webpush_template_id_seq start 1000 increment 1;
create sequence webpush_action_id_seq start 1000 increment 1;
create sequence webpush_campaign_id_seq start 1000 increment 1;

ALTER SEQUENCE android_template_id_seq START 1000 INCREMENT 1;
ALTER SEQUENCE android_action_id_seq START 1000 INCREMENT 1;
ALTER SEQUENCE android_campaign_id_seq START 1000 INCREMENT 1