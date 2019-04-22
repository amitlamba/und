alter table campaign drop column ab_campaign_id,drop column type_campaign;

drop table if exists ab_campaign;
drop table if exists variant;


CREATE TABLE ab_campaign (id serial primary key , campaign_id bigint references campaign(id), run_type varchar (50) not null , remind boolean default true ,
                          wait_time bigint default null , sample_size bigint default null );

CREATE TABLE variant  (id serial primary key , percentage bigint not null , name varchar (512) not null ,
                        users bigint , winner boolean default false , template_id bigint not null , campaign_id bigint references campaign(id));

ALTER TABLE campaign ADD COLUMN if not exists type_campaign varchar (100) not null default 'NORMAL';

-- create sequence ab_campaign_seq_id start 1000;
-- create sequence variant_seq_id start 1000;
alter sequence ab_campaign_seq_id RESTART with 1000 increment 1;
alter sequence variant_seq_id RESTART with 1000 increment 1;