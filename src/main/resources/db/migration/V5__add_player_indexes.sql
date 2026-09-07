create index idx_player_owner_id
on player(owner_id);

create index idx_player_owner_level
on player(owner_id, level desc);