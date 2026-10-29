create table ocorrencias (
    id bigint not null auto_increment,
    turma_id bigint not null,
    aluno_id bigint not null,
    usuario_id bigint not null,
    categoria_id bigint not null,
    tipo_id bigint not null,
    data_registro datetime not null,
    descricao varchar(400) not null,
    status varchar(20) not null,
    data_atualizacao datetime,
    excluida tinyint not null default 0,

    primary key(id),
    constraint fk_ocorrencias_turma_id foreign key(turma_id) references turmas(id),
    constraint fk_ocorrencias_aluno_id foreign key(aluno_id) references alunos(id),
    constraint fk_ocorrencias_usuario_id foreign key(usuario_id) references usuarios(id),
    constraint fk_ocorrencias_categoria_id foreign key(categoria_id) references categorias(id),
    constraint fk_ocorrencias_tipo_id foreign key(tipo_id) references tipos_ocorrencia(id)
);
