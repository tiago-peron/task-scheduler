# User

## Endpoints

| Método | Rota        | Descrição |
|:------:|-------------|-----------|
|  GET   | /auth       | público   |
|  POST  | /user       | público   |
|  POST  | /user/login | público   |
|   *    | /user/**    | público   |

## Segurança

- OAuth2 Resource Server com JWT.
- Sessões `STATELESS` (CSRF desabilitado).
- Apenas tokens com claim `type=ACCESS` são aceitos.
- Authorities vêm da claim `role` com prefixo `ROLE_`.
- Persistência via Spring Data Jpa - entidade `tb_users`.

## Rodando localmente

```shell
cd services/user-service
./mvnw spring-book:run
```

## Variáveis de ambiente

# Testes

```shell
./mvnw test
```
