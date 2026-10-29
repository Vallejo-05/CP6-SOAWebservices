create table turmas (
    id bigint not null auto_increment,
    nome varchar(100) not null,
    turno varchar(10) not null,
    ano int not null,
    semestre varchar(10) not null,
    cancelada tinyint not null default 0,

    primary key(id)
);

-- Relacionamento N:M entre alunos e turmas
create table turmas_alunos (
    turma_id bigint not null,
    aluno_id bigint not null,

    primary key(turma_id, aluno_id),
    constraint fk_turmas_alunos_turma_id foreign key(turma_id) references turmas(id),
    constraint fk_turmas_alunos_aluno_id foreign key(aluno_id) references alunos(id)
);
