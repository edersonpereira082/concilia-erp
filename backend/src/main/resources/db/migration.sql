CREATE TABLE IF NOT EXISTS retorno_bancario (
  id BIGSERIAL,
  nome_arquivo VARCHAR(255) NOT NULL,
  data_processamento DATE NOT NULL,
  status VARCHAR(30) NOT NULL,
  registros INTEGER NOT NULL,
  PRIMARY KEY (id, data_processamento)
) PARTITION BY RANGE (data_processamento);

CREATE TABLE IF NOT EXISTS retorno_bancario_default PARTITION OF retorno_bancario DEFAULT;
CREATE INDEX IF NOT EXISTS idx_retorno_data_status ON retorno_bancario (data_processamento, status);

CREATE TABLE IF NOT EXISTS retorno_detalhe (
  id BIGSERIAL PRIMARY KEY,
  retorno_id BIGINT NOT NULL,
  numero_linha INTEGER NOT NULL,
  layout VARCHAR(10) NOT NULL,
  linha VARCHAR(400) NOT NULL,
  nosso_numero VARCHAR(25),
  codigo_ocorrencia VARCHAR(3),
  valor NUMERIC(15, 2),
  data_ocorrencia DATE,
  resultado VARCHAR(24) NOT NULL,
  motivo VARCHAR(120)
);
CREATE INDEX IF NOT EXISTS idx_retorno_detalhe_retorno ON retorno_detalhe (retorno_id);

CREATE TABLE IF NOT EXISTS empresa (
  id BIGSERIAL PRIMARY KEY,
  razao_social VARCHAR(150) NOT NULL,
  nome_fantasia VARCHAR(150),
  cnpj VARCHAR(14) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  email VARCHAR(150),
  telefone VARCHAR(20),
  cidade VARCHAR(120),
  uf VARCHAR(2),
  CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj),
  CONSTRAINT ck_empresa_tipo CHECK (tipo IN ('CLIENTE', 'FORNECEDOR', 'AMBOS'))
);

ALTER TABLE empresa ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS tipo_pessoa VARCHAR(20) NOT NULL DEFAULT 'JURIDICA';
ALTER TABLE empresa ALTER COLUMN cnpj DROP NOT NULL;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS cpf VARCHAR(11);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS rg VARCHAR(20);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS inscricao_estadual VARCHAR(20);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS cep VARCHAR(8);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS logradouro VARCHAR(150);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS numero VARCHAR(20);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS complemento VARCHAR(80);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS bairro VARCHAR(80);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS celular VARCHAR(11);
UPDATE empresa SET celular = telefone, telefone = NULL
  WHERE celular IS NULL AND telefone IS NOT NULL AND length(regexp_replace(telefone, '\D', '', 'g')) = 11;
CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_cpf ON empresa (cpf);
ALTER TABLE retorno_bancario ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS software_house (
  id BIGSERIAL PRIMARY KEY,
  razao_social VARCHAR(150) NOT NULL,
  nome_fantasia VARCHAR(150),
  cnpj VARCHAR(14) NOT NULL UNIQUE,
  email VARCHAR(150) NOT NULL,
  telefone VARCHAR(20),
  cidade VARCHAR(120),
  uf VARCHAR(2),
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario (
  id BIGSERIAL PRIMARY KEY,
  software_house_id BIGINT NOT NULL REFERENCES software_house(id),
  nome VARCHAR(120) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  senha_hash VARCHAR(100) NOT NULL,
  perfil VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sessao_usuario (
  token VARCHAR(80) PRIMARY KEY,
  usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  expira_em TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS produto (
  id BIGSERIAL PRIMARY KEY,
  sku VARCHAR(40) NOT NULL UNIQUE,
  nome VARCHAR(150) NOT NULL,
  unidade VARCHAR(10) NOT NULL,
  preco_venda NUMERIC(15, 2) NOT NULL,
  preco_custo NUMERIC(15, 2) NOT NULL,
  estoque NUMERIC(15, 3) NOT NULL,
  estoque_minimo NUMERIC(15, 3) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS pedido (
  id BIGSERIAL PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL,
  empresa_id BIGINT NOT NULL REFERENCES empresa(id),
  numero VARCHAR(20) NOT NULL UNIQUE,
  data_emissao DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  total NUMERIC(15, 2) NOT NULL,
  observacao VARCHAR(255),
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_pedido_tipo CHECK (tipo IN ('VENDA', 'COMPRA')),
  CONSTRAINT ck_pedido_status CHECK (status IN ('ABERTO', 'FATURADO', 'CANCELADO'))
);
CREATE INDEX IF NOT EXISTS idx_pedido_tipo_status ON pedido (tipo, status);

CREATE TABLE IF NOT EXISTS pedido_item (
  id BIGSERIAL PRIMARY KEY,
  pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
  produto_id BIGINT NOT NULL REFERENCES produto(id),
  quantidade NUMERIC(15, 3) NOT NULL,
  valor_unitario NUMERIC(15, 2) NOT NULL,
  total NUMERIC(15, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS titulo (
  id BIGSERIAL PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL,
  empresa_id BIGINT NOT NULL REFERENCES empresa(id),
  pedido_id BIGINT REFERENCES pedido(id),
  descricao VARCHAR(180) NOT NULL,
  vencimento DATE NOT NULL,
  valor NUMERIC(15, 2) NOT NULL,
  saldo NUMERIC(15, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_titulo_tipo CHECK (tipo IN ('RECEBER', 'PAGAR')),
  CONSTRAINT ck_titulo_status CHECK (status IN ('ABERTO', 'LIQUIDADO', 'CANCELADO'))
);
CREATE INDEX IF NOT EXISTS idx_titulo_tipo_status ON titulo (tipo, status);

CREATE TABLE IF NOT EXISTS forma_pagamento (
  id BIGSERIAL PRIMARY KEY,
  nome VARCHAR(80) NOT NULL UNIQUE,
  parcelas INTEGER NOT NULL DEFAULT 1,
  dias_vencimento INTEGER NOT NULL DEFAULT 0,
  ativo BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE pedido ADD COLUMN IF NOT EXISTS forma_pagamento_id BIGINT REFERENCES forma_pagamento(id);

INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'DINHEIRO', 1, 0, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'DINHEIRO');
INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'PIX', 1, 0, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'PIX');
INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'CARTAO DE CREDITO', 1, 0, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'CARTAO DE CREDITO');
INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'CARTAO DE DEBITO', 1, 0, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'CARTAO DE DEBITO');
INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'BOLETO', 1, 30, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'BOLETO');
INSERT INTO forma_pagamento (nome, parcelas, dias_vencimento, ativo)
SELECT 'TRANSFERENCIA', 1, 0, TRUE WHERE NOT EXISTS (SELECT 1 FROM forma_pagamento WHERE nome = 'TRANSFERENCIA');

CREATE TABLE IF NOT EXISTS transportadora (
  id BIGSERIAL PRIMARY KEY,
  nome VARCHAR(150) NOT NULL UNIQUE,
  cnpj VARCHAR(14),
  telefone VARCHAR(20),
  ativo BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE pedido ADD COLUMN IF NOT EXISTS transportadora_id BIGINT REFERENCES transportadora(id);
ALTER TABLE pedido ADD COLUMN IF NOT EXISTS frete NUMERIC(15, 2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS conta_corrente (
  id BIGSERIAL PRIMARY KEY,
  descricao VARCHAR(80) NOT NULL UNIQUE,
  banco_codigo VARCHAR(3) NOT NULL,
  banco_nome VARCHAR(120) NOT NULL,
  agencia VARCHAR(20) NOT NULL,
  agencia_digito VARCHAR(2),
  conta VARCHAR(20) NOT NULL,
  conta_digito VARCHAR(2),
  tipo VARCHAR(20) NOT NULL DEFAULT 'CORRENTE',
  titular VARCHAR(150),
  pix VARCHAR(120),
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_conta_corrente_banco_agencia_conta UNIQUE (banco_codigo, agencia, conta),
  CONSTRAINT ck_conta_corrente_tipo CHECK (tipo IN ('CORRENTE', 'POUPANCA'))
);

CREATE TABLE IF NOT EXISTS caixa (
  id BIGSERIAL PRIMARY KEY,
  descricao VARCHAR(80) NOT NULL UNIQUE,
  ativo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO caixa (descricao, ativo)
SELECT 'CAIXA PRINCIPAL', TRUE WHERE NOT EXISTS (SELECT 1 FROM caixa WHERE descricao = 'CAIXA PRINCIPAL');

CREATE TABLE IF NOT EXISTS caixa_sessao (
  id BIGSERIAL PRIMARY KEY,
  caixa_id BIGINT NOT NULL REFERENCES caixa(id),
  data_abertura TIMESTAMP NOT NULL,
  data_fechamento TIMESTAMP,
  saldo_inicial NUMERIC(15, 2) NOT NULL,
  saldo_informado NUMERIC(15, 2),
  saldo_final NUMERIC(15, 2),
  status VARCHAR(20) NOT NULL,
  usuario_abertura_id BIGINT REFERENCES usuario(id),
  usuario_fechamento_id BIGINT REFERENCES usuario(id),
  CONSTRAINT ck_caixa_sessao_status CHECK (status IN ('ABERTO', 'FECHADO'))
);
CREATE INDEX IF NOT EXISTS idx_caixa_sessao_caixa ON caixa_sessao (caixa_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_caixa_sessao_aberta ON caixa_sessao (caixa_id) WHERE status = 'ABERTO';

CREATE TABLE IF NOT EXISTS caixa_movimento (
  id BIGSERIAL PRIMARY KEY,
  sessao_id BIGINT NOT NULL REFERENCES caixa_sessao(id) ON DELETE CASCADE,
  tipo VARCHAR(20) NOT NULL,
  origem VARCHAR(20) NOT NULL,
  valor NUMERIC(15, 2) NOT NULL,
  descricao VARCHAR(180) NOT NULL,
  data_movimento TIMESTAMP NOT NULL,
  titulo_id BIGINT REFERENCES titulo(id),
  pedido_id BIGINT REFERENCES pedido(id),
  CONSTRAINT ck_caixa_movimento_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
  CONSTRAINT ck_caixa_movimento_origem CHECK (origem IN ('VENDA', 'COMPRA', 'TITULO', 'LANCAMENTO'))
);
CREATE INDEX IF NOT EXISTS idx_caixa_movimento_sessao ON caixa_movimento (sessao_id);

ALTER TABLE forma_pagamento ADD COLUMN IF NOT EXISTS tipo_destino VARCHAR(20) NOT NULL DEFAULT 'CAIXA';
ALTER TABLE forma_pagamento ADD COLUMN IF NOT EXISTS conta_corrente_id BIGINT REFERENCES conta_corrente(id);
ALTER TABLE forma_pagamento ADD COLUMN IF NOT EXISTS caixa_id BIGINT REFERENCES caixa(id);
ALTER TABLE forma_pagamento DROP CONSTRAINT IF EXISTS ck_forma_pagamento_destino;
ALTER TABLE forma_pagamento ADD CONSTRAINT ck_forma_pagamento_destino CHECK (tipo_destino IN ('CONTA_CORRENTE', 'CAIXA'));
UPDATE forma_pagamento SET caixa_id = (SELECT id FROM caixa WHERE descricao = 'CAIXA PRINCIPAL' LIMIT 1)
  WHERE tipo_destino = 'CAIXA' AND caixa_id IS NULL;

ALTER TABLE conta_corrente ALTER COLUMN agencia TYPE VARCHAR(20);

CREATE TABLE IF NOT EXISTS conta_movimento (
  id BIGSERIAL PRIMARY KEY,
  conta_corrente_id BIGINT NOT NULL REFERENCES conta_corrente(id),
  tipo VARCHAR(20) NOT NULL,
  origem VARCHAR(20) NOT NULL,
  valor NUMERIC(15, 2) NOT NULL,
  descricao VARCHAR(180) NOT NULL,
  data_movimento TIMESTAMP NOT NULL,
  titulo_id BIGINT REFERENCES titulo(id),
  pedido_id BIGINT REFERENCES pedido(id),
  CONSTRAINT ck_conta_movimento_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
  CONSTRAINT ck_conta_movimento_origem CHECK (origem IN ('VENDA', 'COMPRA', 'TITULO', 'LANCAMENTO'))
);
CREATE INDEX IF NOT EXISTS idx_conta_movimento_conta ON conta_movimento (conta_corrente_id);

ALTER TABLE titulo ADD COLUMN IF NOT EXISTS conta_corrente_id BIGINT REFERENCES conta_corrente(id);
ALTER TABLE titulo ADD COLUMN IF NOT EXISTS caixa_id BIGINT REFERENCES caixa(id);
ALTER TABLE titulo ADD COLUMN IF NOT EXISTS pedido_numero VARCHAR(20);
UPDATE titulo SET pedido_numero = (SELECT numero FROM pedido WHERE pedido.id = titulo.pedido_id)
  WHERE pedido_id IS NOT NULL AND pedido_numero IS NULL;
ALTER TABLE caixa_movimento ADD COLUMN IF NOT EXISTS pedido_numero VARCHAR(20);
UPDATE caixa_movimento SET pedido_numero = (SELECT numero FROM pedido WHERE pedido.id = caixa_movimento.pedido_id)
  WHERE pedido_id IS NOT NULL AND pedido_numero IS NULL;
ALTER TABLE conta_movimento ADD COLUMN IF NOT EXISTS pedido_numero VARCHAR(20);
UPDATE conta_movimento SET pedido_numero = (SELECT numero FROM pedido WHERE pedido.id = conta_movimento.pedido_id)
  WHERE pedido_id IS NOT NULL AND pedido_numero IS NULL;

UPDATE forma_pagamento
  SET tipo_destino = 'CAIXA',
      caixa_id = (SELECT id FROM caixa WHERE descricao = 'CAIXA PRINCIPAL' LIMIT 1),
      conta_corrente_id = NULL
  WHERE nome = 'DINHEIRO';

UPDATE forma_pagamento
  SET tipo_destino = 'CONTA_CORRENTE',
      conta_corrente_id = (SELECT id FROM conta_corrente WHERE ativo = TRUE ORDER BY id LIMIT 1),
      caixa_id = NULL
  WHERE nome IN ('PIX', 'CARTAO DE CREDITO', 'CARTAO DE DEBITO', 'BOLETO', 'TRANSFERENCIA')
    AND EXISTS (SELECT 1 FROM conta_corrente WHERE ativo = TRUE);

ALTER TABLE conta_movimento DROP CONSTRAINT IF EXISTS ck_conta_movimento_origem;
ALTER TABLE conta_movimento ADD CONSTRAINT ck_conta_movimento_origem CHECK (origem IN ('VENDA', 'COMPRA', 'TITULO', 'LANCAMENTO'));
ALTER TABLE caixa_movimento DROP CONSTRAINT IF EXISTS ck_caixa_movimento_origem;
ALTER TABLE caixa_movimento ADD CONSTRAINT ck_caixa_movimento_origem CHECK (origem IN ('VENDA', 'COMPRA', 'TITULO', 'LANCAMENTO'));

CREATE TABLE IF NOT EXISTS sistema_log (
  id BIGSERIAL PRIMARY KEY,
  software_house_id BIGINT NOT NULL REFERENCES software_house(id),
  data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  acao VARCHAR(40) NOT NULL,
  metodo VARCHAR(10) NOT NULL,
  recurso VARCHAR(180) NOT NULL,
  descricao VARCHAR(240) NOT NULL,
  usuario_nome VARCHAR(120) NOT NULL,
  usuario_email VARCHAR(150) NOT NULL,
  confirmador_nome VARCHAR(120),
  confirmador_email VARCHAR(150),
  status_http INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sistema_log_data ON sistema_log (software_house_id, data_hora DESC);
