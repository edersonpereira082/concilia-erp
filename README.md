# Concilia.ERP

[Português](#português) · [English](#english) · [Italiano](#italiano)

ERP comercial para pedidos, estoque, financeiro, caixa, contas bancárias e conciliação CNAB.

---

## Português

### O que é

O **Concilia.ERP** reúne operação comercial e tesouraria no mesmo painel: vendas, compras, produtos, clientes e fornecedores, títulos a pagar e receber, caixa, contas correntes, formas de pagamento, transportadoras e conciliação de retorno bancário.

### Tecnologias

- Backend: Spring Boot 3, Java 21, PostgreSQL 16, RabbitMQ
- Frontend: React + Vite
- Execução: Docker Compose

### Como executar

Pré-requisito: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```powershell
docker compose up --build -d
```

- Sistema: http://localhost:5173/
- API: http://localhost:8080/
- RabbitMQ: http://localhost:15672/ (`guest` / `guest`)
- PostgreSQL (HeidiSQL ou outro cliente): `127.0.0.1`, porta `5433`, banco `conciliacao`, usuário `conciliacao`, senha `conciliacao`

Conta de teste local: `admin@teste.com` / `12345678` (administrador).

### Módulos

- Comercial: vendas, compras e estoque
- Financeiro: títulos, contas correntes, extrato com lançamentos, caixa, formas de pagamento e conciliação CNAB
- Cadastros: cliente/fornecedor, transportadoras e usuários
- Sistema: log das rotinas (criação, alteração, exclusão e acesso)

### Regras de operação

- Listas mostram 5 registros por página
- Exclusão e alteração pedem e-mail e senha de um usuário cadastrado
- Cadastros têm interruptor de ativo/inativo
- Data e hora aparecem no fuso do computador

### Acesso ao banco no HeidiSQL

Tipo de rede **PostgreSQL**, servidor `127.0.0.1`, porta **5433**, usuário e senha `conciliacao`, banco `conciliacao`. O container precisa estar no ar.

---

## English

### What it is

**Concilia.ERP** is a commercial ERP that combines sales operations and treasury in one workspace: sales, purchases, products, customers and suppliers, payables and receivables, cash register, bank accounts, payment methods, carriers, and bank return (CNAB) reconciliation.

### Stack

- Backend: Spring Boot 3, Java 21, PostgreSQL 16, RabbitMQ
- Frontend: React + Vite
- Runtime: Docker Compose

### How to run

Requirement: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```powershell
docker compose up --build -d
```

- App: http://localhost:5173/
- API: http://localhost:8080/
- RabbitMQ: http://localhost:15672/ (`guest` / `guest`)
- PostgreSQL (HeidiSQL or any client): `127.0.0.1`, port `5433`, database `conciliacao`, user `conciliacao`, password `conciliacao`

Local test account: `admin@teste.com` / `12345678` (administrator).

### Modules

- Sales: sales orders, purchase orders, and inventory
- Finance: titles, bank accounts, statement with postings, cash register, payment methods, and CNAB reconciliation
- Master data: customers/suppliers, carriers, and users
- System: activity log (create, update, delete, and sign-in)

### Operating rules

- Lists show 5 records per page
- Deletes and changes require email and password of a registered user
- Records use an active/inactive toggle
- Date and time follow the computer’s local timezone

### HeidiSQL connection

Network type **PostgreSQL**, host `127.0.0.1`, port **5433**, user and password `conciliacao`, database `conciliacao`. The Postgres container must be running.

---

## Italiano

### Cos’è

**Concilia.ERP** è un ERP commerciale che unisce operazione di vendita e tesoreria nello stesso pannello: vendite, acquisti, prodotti, clienti e fornitori, titoli da pagare e da incassare, cassa, conti correnti, forme di pagamento, corrieri e riconciliazione dei flussi bancari CNAB.

### Tecnologie

- Backend: Spring Boot 3, Java 21, PostgreSQL 16, RabbitMQ
- Frontend: React + Vite
- Esecuzione: Docker Compose

### Come avviarlo

Requisito: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```powershell
docker compose up --build -d
```

- Applicazione: http://localhost:5173/
- API: http://localhost:8080/
- RabbitMQ: http://localhost:15672/ (`guest` / `guest`)
- PostgreSQL (HeidiSQL o altro client): `127.0.0.1`, porta `5433`, database `conciliacao`, utente `conciliacao`, password `conciliacao`

Account di prova locale: `admin@teste.com` / `12345678` (amministratore).

### Moduli

- Commerciale: vendite, acquisti e magazzino
- Finanziario: titoli, conti correnti, estratto con registrazioni, cassa, forme di pagamento e riconciliazione CNAB
- Anagrafiche: cliente/fornitore, corrieri e utenti
- Sistema: log delle routine (creazione, modifica, cancellazione e accesso)

### Regole operative

- Le liste mostrano 5 record per pagina
- Cancellazioni e modifiche richiedono e-mail e password di un utente registrato
- Le anagrafiche hanno l’interruttore attivo/inattivo
- Data e ora seguono il fuso orario del computer

### Connessione HeidiSQL

Tipo di rete **PostgreSQL**, server `127.0.0.1`, porta **5433**, utente e password `conciliacao`, database `conciliacao`. Il container deve essere avviato.
