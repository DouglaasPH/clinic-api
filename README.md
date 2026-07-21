# Clinic API (Backend)

A **Clinic API** é o serviço de backend desenvolvido em Java para o gerenciamento de uma clínica médica especializada em exames raio-x. 

O projeto foi desenhado seguindo o princípio entities, repositories, services e controllers. Ele foi estruturado para rodar localmente simulando um ambiente de nuvem AWS real através do **LocalStack**.

---

## Tecnologias Utilizadas

* **Java 25** / **Spring Boot 4.1.0**
* **LocalStack** (Emulação offline de serviços AWS)
* **AWS SDK para Java**
* **PostgreSQL**
* **Docker & Docker Compose**

---

## Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:

* Docker
* Java JDK 25 ou superior
* AWS CLI](https://aws.amazon.com/cli/) (opcional, mas recomendado para inspecionar o LocalStack) // retirar

---

## Como Executar o Projeto

Siga os passos abaixo para colocar a API e a infraestrutura local em execução.

## Passo a Passo para o Deploy Local

1. **Subir a Infraestrutura (Docker):** LocalStack & DB.
   Suba os containers do LocalStack e do banco de dados em segundo plano:

```bash
docker compose up -d

```


2. **Verificar Serviços AWS Local:** Sanity Check.
   Garanta que os serviços emulados (ex: S3, SQS, DynamoDB) estão saudáveis:

```bash
aws --endpoint-url=http://localhost:4566 localstack status

```


3. **Criar Recursos Iniciais:** Scripts de Setup.
   Caso seu projeto não crie os buckets/tabelas automaticamente via código na inicialização, execute o script de inicialização ou os comandos AWS CLI apontando para o LocalStack:

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://clinic-documents-bucket

```


4. **Compilar e Rodar a API:** Application Boot.
   Execute a aplicação Spring Boot utilizando o perfil de configuração local/aws-local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

```


---

## 📂 Variáveis de Ambiente (`.env`)

A aplicação espera as seguintes variáveis para se comunicar corretamente com o container do LocalStack. Elas geralmente já vêm configuradas no `application-local.yml`:

```properties
AWS_ENDPOINT_URL=http://localhost:4566
AWS_REGION=us-east-1
AWS_ACCESS_KEY=test
AWS_SECRET_KEY=test

```

> 💡 **Nota do LocalStack:** Como estamos emulando a AWS localmente, as credenciais `AWS_ACCESS_KEY` e `AWS_SECRET_KEY` podem ser preenchidas com qualquer valor fictício (como `test`), mas são obrigatórias para o SDK assinar as requisições.

---

## 🔗 Endpoints Principais (Exemplos)

A API estará disponível em `http://localhost:8080`.

| Método | Endpoint | Descrição | Integração AWS |
| --- | --- | --- | --- |
| `GET` | `/api/v1/patients` | Lista todos os pacientes | - |
| `POST` | `/api/v1/documents` | Upload de prontuário/exame | **S3 Bucket** |
| `POST` | `/api/v1/notifications` | Envia alerta de consulta | **SQS / SNS** |

---

## 👥 Contribuição e Desenvolvimento

Para garantir o isolamento do projeto, este repositório contém estritamente as regras de negócio e infraestrutura do backend. Alterações na camada visual/frontend devem ser realizadas em seu respectivo repositório separado.

---