-- apply changes
create table rcinventory_inventories (
  id                            varchar(40) not null,
  holder_id                     varchar(40),
  serialized_inventory          varchar(255),
  saturation                    float,
  exp                           float,
  level                         integer not null,
  creation_millis               integer not null,
  world                         varchar(255),
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcinventory_inventories primary key (id)
);

