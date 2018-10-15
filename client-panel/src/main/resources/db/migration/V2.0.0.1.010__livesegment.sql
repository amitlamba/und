create table if not exists live_segment
(
  id bigserial not null
    constraint live_segment_pkey
    primary key,
  client_id bigint not null
    constraint live_segment_client_id_fkey
    references client,
  segment_id bigint not null
    constraint live_segment_segment_id_fkey
    references segment,
  start_event varchar(256) not null,
  end_event varchar(256) not null,
  start_event_filter text not null,
  end_event_filter text not null,
  end_event_done bool DEFAULT FALSE ,
  interval_seconds bigint not null,
  date_modified timestamp with time zone default now()
);

alter sequence  live_segment_id_seq RESTART WITH 1000 INCREMENT 1;