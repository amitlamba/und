create table if not exists campaignTriggerInfo (campaign_id bigint primary key , client_id bigint not null,error boolean not null);

create table if not exists executionStatus (execution_id varchar (56)  primary key ,execution_time timestamp not null,campaign_id bigint  references campaignTriggerInfo(campaign_id));