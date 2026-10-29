create table perfis (
    id bigint not null auto_increment,
    nome varchar(30) not null unique,

    primary key(id)
);
