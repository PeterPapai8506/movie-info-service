create table search_events (
 id bigint not null auto_increment primary key,
 movie_title varchar(255) not null,
 api_name varchar(16) not null,
 searched_at timestamp(6) not null,
 index idx_search_events_time (searched_at),
 index idx_search_events_api_title (api_name, movie_title)
);
