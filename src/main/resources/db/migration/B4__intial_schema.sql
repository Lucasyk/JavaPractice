create table app_user (
  id bigserial primary key,
  username varchar(50) not null unique,
  password_hash varchar(255) not null,
  role varchar(30) not null default 'ROLE_USER'
);

create table player (
  id bigserial primary key,
  name varchar(255),
  level integer not null,
  experience integer not null default 0,
  owner_id BIGINT,

  constraint fk_player_owner
  foreign key (owner_id)
  references app_user(id)
);