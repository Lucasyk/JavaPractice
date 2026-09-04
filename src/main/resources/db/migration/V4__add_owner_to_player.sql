alter table player 
add column owner_id BIGINT;

alter table player
add constraint fk_player_owner
foreign key (owner_id)
references app_user(id);