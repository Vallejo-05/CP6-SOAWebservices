create table alunos (
    id bigint not null auto_increment,
    nome varchar(100) not null,
    data_nascimento date not null,
    foto_content_type varchar(50),
    ativo tinyint not null default 1,

    primary key(id)
);

-- A foto fica em tabela separada para não ser carregada nas listagens de alunos
create table alunos_fotos (
    aluno_id bigint not null,
    conteudo longblob not null,

    primary key(aluno_id),
    constraint fk_alunos_fotos_aluno_id foreign key(aluno_id) references alunos(id)
);
