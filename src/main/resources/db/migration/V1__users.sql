create table users
(
  id           bigint not null auto_increment,
  created_at   datetime,
  email        varchar(100) not null,
  full_name    varchar(255) not null,
  password     varchar(255) not null,
  updated_at   datetime,
  primary key (id)
);
alter table users add constraint usersemail unique (email);