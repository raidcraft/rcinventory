-- apply changes
alter table rcinventory_inventories alter column serialized_inventory type text using serialized_inventory::text;
