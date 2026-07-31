create table if not exists accounts (
    id serial primary key,
    name varchar(50),
    email varchar(50)
);