create table categorias (
    id bigint not null auto_increment,
    nome varchar(50) not null unique,

    primary key(id)
);

create table tipos_ocorrencia (
    id bigint not null auto_increment,
    nome varchar(50) not null,
    categoria_id bigint not null,

    primary key(id),
    constraint uk_tipos_ocorrencia_nome_categoria unique(nome, categoria_id),
    constraint fk_tipos_ocorrencia_categoria_id foreign key(categoria_id) references categorias(id)
);
