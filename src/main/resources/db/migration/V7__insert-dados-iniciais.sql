-- Perfis definidos nos requisitos
insert into perfis (nome) values ('PROFESSOR'), ('ANALISTA'), ('COORDENADOR'), ('ADMINISTRATIVO');

-- Um usuário inicial por perfil (senha de todos: 123456, armazenada com BCrypt)
insert into usuarios (nome, login, senha, perfil_id)
select 'Administrativo', 'admin@escola.com', '$2a$10$g6yGlHX8swNz5miR010/FeznGNKSo2i7NqVku3ksM9kWWZPScLPh2', id from perfis where nome = 'ADMINISTRATIVO';
insert into usuarios (nome, login, senha, perfil_id)
select 'Coordenador', 'coordenador@escola.com', '$2a$10$g6yGlHX8swNz5miR010/FeznGNKSo2i7NqVku3ksM9kWWZPScLPh2', id from perfis where nome = 'COORDENADOR';
insert into usuarios (nome, login, senha, perfil_id)
select 'Analista', 'analista@escola.com', '$2a$10$g6yGlHX8swNz5miR010/FeznGNKSo2i7NqVku3ksM9kWWZPScLPh2', id from perfis where nome = 'ANALISTA';
insert into usuarios (nome, login, senha, perfil_id)
select 'Professor', 'professor@escola.com', '$2a$10$g6yGlHX8swNz5miR010/FeznGNKSo2i7NqVku3ksM9kWWZPScLPh2', id from perfis where nome = 'PROFESSOR';

-- Categorias e tipos de ocorrência iniciais (podem ser gerenciados pelo perfil ADMINISTRATIVO)
insert into categorias (nome) values ('DISCIPLINAR'), ('PEDAGOGICA'), ('ADMINISTRATIVA');

insert into tipos_ocorrencia (nome, categoria_id) select 'INDISCIPLINA', id from categorias where nome = 'DISCIPLINAR';
insert into tipos_ocorrencia (nome, categoria_id) select 'BRIGA', id from categorias where nome = 'DISCIPLINAR';
insert into tipos_ocorrencia (nome, categoria_id) select 'USO_DE_CELULAR', id from categorias where nome = 'DISCIPLINAR';
insert into tipos_ocorrencia (nome, categoria_id) select 'RENDIMENTO', id from categorias where nome = 'PEDAGOGICA';
insert into tipos_ocorrencia (nome, categoria_id) select 'FALTA_DE_TAREFA', id from categorias where nome = 'PEDAGOGICA';
insert into tipos_ocorrencia (nome, categoria_id) select 'ATRASO', id from categorias where nome = 'ADMINISTRATIVA';
insert into tipos_ocorrencia (nome, categoria_id) select 'FALTA_DE_UNIFORME', id from categorias where nome = 'ADMINISTRATIVA';
