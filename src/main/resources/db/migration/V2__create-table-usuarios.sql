create table usuarios (
    id bigint not null auto_increment,
    nome varchar(100) not null,
    login varchar(100) not null unique,
    senha varchar(255) not null,
    perfil_id bigint not null,
    ativo tinyint not null default 1,

    primary key(id),
    constraint fk_usuarios_perfil_id foreign key(perfil_id) references perfis(id)
);
