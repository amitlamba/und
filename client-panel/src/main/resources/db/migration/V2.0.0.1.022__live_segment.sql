ALTER TABLE live_segment ADD COLUMN if not exists date_created timestamp with time zone default now(),ADD COLUMN if not exists live_segment_type varchar (100) not null ;
ALTER TABLE campaign ADD COLUMN start_date timestamp with time zone default null,
                     ADD COLUMN end_date timestamp with time zone default null ;