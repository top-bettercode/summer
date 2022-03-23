drop sequence if exists HIBERNATE_SEQUENCE;
create sequence if not exists HIBERNATE_SEQUENCE;
drop table if exists user;
create table if not exists user
(
  id       integer not null,
  first_name varchar(255),
  last_name  varchar(255),
  deleted   tinyint      default 0,
  primary key (id)
)