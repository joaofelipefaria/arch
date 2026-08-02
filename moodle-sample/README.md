# Fundamentos de Computação — Moodle + Script de Seed

Este projeto documenta como subir uma instância local do Moodle via Docker,
confirmar que está tudo funcionando, e popular o curso **"Fundamentos de
Computação"** (9 módulos, com conteúdo, quizzes e usuários de teste) usando o
script `seed_curso_computacao.php`.

> ⚠️ Os passos de instalação abaixo (nomes de containers, portas, variáveis de
> ambiente) assumem um `docker-compose.yml` padrão de Moodle, com o serviço
> web chamado **`moodle-app`** e um serviço de banco de dados separado.
> Ajuste nomes/portas/valores conforme o seu `docker-compose.yml` real, se
> for diferente.

---

## 1. Instalação inicial (subindo os containers)

Na pasta do projeto (onde está o `docker-compose.yml`):

```bash
docker compose up -d
```

Isso deve subir pelo menos dois containers:

- **`moodle-app`** — a aplicação Moodle (PHP + Apache/Nginx).
- Um container de **banco de dados** (MySQL, MariaDB ou PostgreSQL,
  dependendo da imagem usada).

Confira se os containers estão de pé:

```bash
docker ps
```

Você deve ver `moodle-app` (e o container do banco) com status `Up`.

---

## 2. Tela de instalação do Moodle (parâmetros do banco de dados)

Na primeira vez que você acessar o Moodle pelo navegador
(geralmente `http://localhost` ou a porta configurada no `docker-compose.yml`),
o instalador web do Moodle pode pedir os dados de conexão com o banco. Os
valores abaixo devem bater com as variáveis de ambiente definidas no seu
`docker-compose.yml` para o serviço de banco — confira lá antes de digitar:

| Campo no instalador       | O que preencher                                      |
|----------------------------|-------------------------------------------------------|
| **Tipo de driver de banco** | MariaDB / MySQLi (ou PostgreSQL, se for o seu caso)  |
| **Servidor do banco (host)**| Nome do container do banco (ex: `moodle-db`, **não** `localhost`) |
| **Nome do banco de dados**  | O valor de `MYSQL_DATABASE` / `POSTGRES_DB` no compose |
| **Usuário do banco**        | O valor de `MYSQL_USER` / `POSTGRES_USER` no compose   |
| **Senha do banco**          | O valor de `MYSQL_PASSWORD` / `POSTGRES_PASSWORD` no compose |
| **Prefixo das tabelas**     | `mdl_` (padrão, pode manter)                          |

> 💡 Dica: como o Moodle e o banco rodam em containers separados na mesma
> rede Docker, o "servidor do banco" **é o nome do container/serviço**
> (ex: `moodle-db`), nunca `localhost` ou `127.0.0.1` — isso é o erro mais
> comum nesta etapa.

Depois dessa tela, o instalador ainda vai pedir:

- Aceitar a licença do Moodle.
- Verificação dos requisitos do servidor (PHP, extensões, etc. — devem
  aparecer todos com "OK" se a imagem Docker estiver correta).
- **Criação da conta de administrador** (usuário, senha, e-mail) — guarde
  essas credenciais, é com elas que você vai logar como admin para rodar
  o script de seed depois.
- Nome completo e nome curto do site (pode preencher como quiser, ex:
  "Meu Moodle" / "meumoodle").

Ao final, o Moodle deve te redirecionar para a página inicial do site,
já logado como administrador.

---

## 3. Como confirmar que está tudo funcionando

1. **Site carrega:** acesse a URL do Moodle no navegador (ex:
   `http://localhost`) e confirme que a página inicial aparece, sem erros
   de PHP/banco na tela.
2. **Login de admin funciona:** faça logout e login novamente com o usuário
   administrador criado na instalação.
3. **Painel de administração acessível:** vá em
   *Administração do site* (ícone de engrenagem/menu do admin) e confirme
   que as páginas de configuração abrem normalmente (isso indica que a
   conexão com o banco está saudável).
4. **Logs dos containers sem erro:** se algo parecer estranho, cheque:
   ```bash
   docker logs moodle-app --tail 100
   ```
5. **Cron do Moodle rodando (opcional, mas recomendado):** o Moodle depende
   de uma tarefa agendada (`cron`) para várias funções em segundo plano.
   Confirme rodando manualmente uma vez:
   ```bash
   docker exec -it moodle-app php /var/www/html/admin/cli/cron.php
   ```
   Se não der erro, está tudo certo.

Com isso confirmado, o ambiente está pronto para rodar o script de seed.

---

## 4. Copiando o script `seed_curso_computacao.php` para dentro do container

No seu computador (fora do container), na pasta onde está o arquivo
`seed_curso_computacao.php`:

```bash
docker cp seed_curso_computacao.php moodle-app:/var/www/html/seed_curso_computacao.php
```

> Ajuste `/var/www/html` se a sua instalação do Moodle dentro do container
> estiver em outro caminho (é onde deve estar o `config.php` do Moodle).

### Se você usa Git Bash / MSYS no Windows

O Git Bash converte automaticamente caminhos que começam com `/` para um
caminho de disco do Windows, o que pode gerar erros como
`Could not open input file: C:/Program Files/Git/var/...`. Para evitar isso,
use uma das opções abaixo:

```bash
# Opção 1: dobrar a barra inicial
docker cp seed_curso_computacao.php moodle-app://var/www/html/seed_curso_computacao.php

# Opção 2: desligar a conversão de path só para este comando
MSYS_NO_PATHCONV=1 docker cp seed_curso_computacao.php moodle-app:/var/www/html/seed_curso_computacao.php
```

---

## 5. Rodando o script

Dentro do container:

```bash
docker exec -it moodle-app php /var/www/html/seed_curso_computacao.php
```

(No Git Bash/MSYS, use o mesmo truque acima se necessário:
`MSYS_NO_PATHCONV=1 docker exec -it moodle-app php /var/www/html/seed_curso_computacao.php`)

### Lendo a saída

- `-> Etapa ... OK` → passo concluído com sucesso.
- `[SKIP] ...` → aquele item já existia, não precisou recriar. **O script é
  idempotente**: pode rodar quantas vezes quiser, sem duplicar nada.
- `[ERRO]` com um bloco `====...====` → alguma etapa falhou. A mensagem já
  traz a causa técnica, o arquivo/linha exatos e o stack trace — corrija o
  problema indicado e rode o script de novo.

Ao final, com tudo certo, o script imprime um resumo parecido com:

```
===========================================================
 Curso pronto!
 Nome: Fundamentos de Computação
 Shortname: COMPBAS-2026-V2
 URL: http://localhost/course/view.php?id=...
 Senha padrão dos usuários criados (se novos): Moodle#2026!
 (recomendado: forçar troca de senha no primeiro login)
===========================================================
```

---

## 6. Usuários criados pelo script

| Papel      | Usuário        | Senha            |
|------------|-----------------|-------------------|
| Professor  | `prof.silva`    | `Moodle#2026!`    |
| Aluno      | `aluno.ana`     | `Moodle#2026!`    |
| Aluno      | `aluno.bruno`   | `Moodle#2026!`    |
| Aluno      | `aluno.carla`   | `Moodle#2026!`    |
| Aluno      | `aluno.diego`   | `Moodle#2026!`    |
| Aluno      | `aluno.elisa`   | `Moodle#2026!`    |

> ⚠️ Senha padrão de teste — troque antes de usar em qualquer ambiente que
> não seja local/descartável.

---

## 7. Acessando o curso

### Como professor ou aluno (login direto)

1. Acesse a URL do Moodle e clique em **Entrar**.
2. Use qualquer um dos usuários da tabela acima (ex: `prof.silva` /
   `Moodle#2026!`, ou `aluno.ana` / `Moodle#2026!`).
3. O curso **"Fundamentos de Computação"** deve aparecer na página inicial
   (em *Meus cursos*), já com as 9 seções, páginas de conteúdo e quizzes.

### Como admin, "entrando como" um aluno (sem saber a senha)

Útil para testar rapidamente sem sair da sua sessão de administrador:

1. Acesse o curso **"Fundamentos de Computação"**.
2. Vá na aba **Participantes**.
3. Encontre o aluno desejado (ex: Ana Ferreira), clique no menu/engrenagem
   ao lado do nome e escolha **"Entrar como"** (*Log in as*).
4. Você passa a navegar o Moodle como se fosse aquele usuário — dá para ver
   as atividades, responder quizzes, etc.
5. Para voltar a ser admin, clique no aviso no topo da página ("Você está
   entrando como... Sair") ou no seu nome de usuário no canto superior.

> Essa opção exige que o usuário logado (o admin) tenha a permissão
> `moodle/user:loginas`, que já vem habilitada por padrão para
> administradores.

---

## 8. Referência rápida de comandos

```bash
# Subir o ambiente
docker compose up -d

# Ver containers rodando
docker ps

# Ver logs do Moodle
docker logs moodle-app --tail 100

# Rodar o cron manualmente (opcional, para checar saúde do sistema)
docker exec -it moodle-app php /var/www/html/admin/cli/cron.php

# Copiar o script de seed para dentro do container
docker cp seed_curso_computacao.php moodle-app:/var/www/html/seed_curso_computacao.php

# Rodar o script de seed
docker exec -it moodle-app php /var/www/html/seed_curso_computacao.php
```
