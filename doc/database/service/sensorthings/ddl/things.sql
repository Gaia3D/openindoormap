drop table if exists things cascade;


-- things
create table things(
	things_id						integer,
	things_name					varchar(100)							not null,
	description					varchar(50)								not null,
	properties					text
	constraint things_pk primary key (things_id)	
);

comment on table things is 'things';
comment on column things.things_id is '고유번호';
comment on column things.things_name is 'things명';
comment on column things.description is 'things 설명';
comment on column things.properties is 'things properties';