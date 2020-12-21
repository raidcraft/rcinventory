-- apply changes
create table rcinventory_inventories (
  id                            uuid not null,
  holder_id                     uuid,
  serialized_inventory          varchar(255),
  saturation                    float,
  exp                           float,
  level                         integer not null,
  creation_millis               bigint not null,
  world                         varchar(255),
  version                       bigint not null,
  when_created                  timestamptz not null,
  when_modified                 timestamptz not null,
  constraint pk_rcinventory_inventories primary key (id)
);

