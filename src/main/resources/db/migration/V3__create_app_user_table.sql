create table app_user (
  id bigserial primary key,
  username varchar(50) not null unique,
  password_hash varchar(255) not null,
  role varchar(30) not null  default 'ROLE_USER'
);