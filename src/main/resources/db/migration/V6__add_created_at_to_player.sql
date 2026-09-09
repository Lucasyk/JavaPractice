alter table player 
add column created_at timestamp with time zone
not null default CURRENT_TIMESTAMP;