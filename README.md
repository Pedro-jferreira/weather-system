# 🌤️ Sistema de Previsão Meteorológica Distribuído com gRPC e Spring Boot

Este projeto implementa um sistema **distribuído de previsão do tempo** utilizando **gRPC** e **Spring Boot**. A proposta é simular um ambiente onde uma aplicação cliente (REST) se comunica com um servidor gRPC responsável por fornecer dados meteorológicos como temperatura atual, previsão de dias futuros e estatísticas climáticas.

---

## 🎯 Objetivo

Desenvolver uma aplicação **cliente-servidor** onde:

- O **servidor gRPC** oferece 5 serviços meteorológicos.
- A **API REST (cliente)** faz chamadas HTTP (via Postman, por exemplo) e se comunica com o servidor via gRPC.
- Os dados são **simulados** em memória ou consultados por meio da API **OpenWeatherMap** para validação das cidades.

---

## 🔧 Tecnologias Utilizadas

- Java 17
- Spring Boot
- gRPC com `grpc-server-spring-boot-starter`
- API OpenWeatherMap (Para consultar as Temperaturas)
- Maven

---

## 🚀 Como Executar o Projeto

### 1. Clone o repositório

```bash
git clone https://github.com/Pedro-jferreira/weather-system
cd weather-system
```

### 2. Adicione a chave da API OpenWeatherMap

No arquivo `application.properties`, localize a variável `apiKey` e insira sua chave pessoal:

```properties
weather.api.key={sua chave}
```

Você pode obter uma chave gratuita em: https://openweathermap.org/api

### 3. Gere os stubs gRPC

Use o Maven para compilar o projeto e gerar os stubs a partir do `.proto`:

```bash
./mvn compile
```

> Certifique-se de que o plugin de compilação do gRPC esteja configurado corretamente no `pom.xml`.

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

O servidor gRPC será iniciado na porta padrão `9090`.

---
## 🌐 Usando gRPC-Web Proxy (grpcwebproxy)

Este projeto inclui o binário do [`grpcwebproxy`](https://github.com/improbable-eng/grpc-web/tree/master/go/grpcwebproxy) para **Windows 64 bits**, já na pasta `grpcwebproxy/`.

### 📦 1. Arquivo incluso no repositório

Você pode encontrar o executável do proxy para **Windows** na seguinte pasta:

```
weather-system/grpcwebproxy/grpcwebproxy-v0.15.0-win64.exe
```

### 💻 2. Usuários de Linux/macOS

Se você utiliza Linux ou macOS, baixe o binário correspondente à sua plataforma aqui:

👉 [Releases oficiais do grpcwebproxy](https://github.com/improbable-eng/grpc-web/releases)

### ⚙️ 3. Execute o proxy

Com o servidor gRPC (Spring Boot) rodando na porta `9090`, execute o proxy com:

#### Windows (PowerShell ou CMD):

```powershell
src/main/grpcwebproxy/grpcwebproxy-v0.15.0-win64.exe ^
  --backend_addr=localhost:9090 ^
  --run_tls_server=false ^
  --allow_all_origins ^
  --server_http_debug_port=8081
```

#### Linux/macOS:

```bash
  src/main/grpcwebproxygrpcwebproxy-v0.15.0-linux-x86_64   --backend_addr=localhost:9090   --run_tls_server=false   --allow_all_origins   --server_http_debug_port=8081
```

> Se necessário, dê permissão de execução ao arquivo com `chmod +x`.

### ✅ 4. Teste no navegador

Abra: [http://localhost:8081](http://localhost:8081)

Se aparecer "404 Not Found" e o terminal mostrar `Channel Connectivity change to READY`, o proxy está funcionando corretamente.

---

## 📌 Serviços Disponíveis

O servidor oferece os seguintes serviços meteorológicos:

- 🔸 **Obter temperatura atual** de uma cidade
- 🔸 **Previsão dos próximos 5 dias**
- 🔸 **Listar cidades** disponíveis no sistema
- 🔸 **Cadastrar cidade** (validação via OpenWeatherMap)
- 🔸 **Estatísticas climáticas**: média, mínima e máxima da cidade


```protobuf
syntax = "proto3";

package weather;

service WeatherService {
  rpc ObterTemperaturaAtual (CidadeRequest) returns (TemperaturaResponse);
  rpc PrevisaoCincoDias (CidadeRequest) returns (PrevisaoResponse);
  rpc ListarCidades (Empty) returns (CidadesResponse);
  rpc CadastrarCidade (CidadeRequest) returns (CidadeResponse);
  rpc EstatisticasClimaticas (CidadeRequest) returns (EstatisticasResponse);
}

message Empty {}

message CidadeRequest {
  string nome = 1;
}

message CidadeResponse {
  string mensagem = 1;
}

message TemperaturaResponse {
  string cidade = 1;
  double temperaturaAtual = 2;
}

message PrevisaoResponse {
  string cidade = 1;
  repeated double previsao = 2;
}

message CidadesResponse {
  repeated string cidades = 1;
}

message EstatisticasResponse {
  string cidade = 1;
  double media = 2;
  double minima = 3;
  double maxima = 4;
}
```
---

## 🧪 Testes

Embora o projeto principal seja gRPC, recomenda-se o uso de uma aplicação REST cliente (em Spring Boot) para testar os serviços via Postman ou Insomnia.
