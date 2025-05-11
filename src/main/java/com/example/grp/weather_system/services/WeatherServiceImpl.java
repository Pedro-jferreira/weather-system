package com.example.grp.weather_system.services;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import weather.Weather;
import weather.WeatherServiceGrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
public class WeatherServiceImpl extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final String apiKey = "31e54a68050ef4f2d07202a432802fb6";
    private final RestTemplate restTemplate = new RestTemplate();

    private static final List<String> cidades = new ArrayList<>(Arrays.asList("São Paulo", "Rio de Janeiro", "Belo Horizonte"));

    @Override
    public void obterTemperaturaAtual(Weather.CidadeRequest request, StreamObserver<Weather.TemperaturaResponse> responseObserver) {
        String cidade = request.getNome();
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", cidade, apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("main")) {
                Map<String, Object> main = (Map<String, Object>) body.get("main");
                double temperatura = ((Number) main.get("temp")).doubleValue();

                Weather.TemperaturaResponse resposta = Weather.TemperaturaResponse.newBuilder()
                        .setCidade(cidade)
                        .setTemperaturaAtual(temperatura)
                        .build();

                responseObserver.onNext(resposta);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Throwable("Dados climáticos não encontrados."));
            }
        } catch (Exception e) {
            responseObserver.onError(new Throwable("Erro ao buscar dados: " + e.getMessage()));
        }
    }

    @Override
    public void previsaoCincoDias(Weather.CidadeRequest request, StreamObserver<Weather.PrevisaoResponse> responseObserver) {
        String cidade = request.getNome();
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&cnt=5&appid=%s", cidade, apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("list")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("list");
                List<Double> temperaturas = list.stream()
                        .map(item -> (Map<String, Object>) item.get("main"))
                        .map(main -> ((Number) main.get("temp")).doubleValue())
                        .collect(Collectors.toList());

                Weather.PrevisaoResponse resposta = Weather.PrevisaoResponse.newBuilder()
                        .setCidade(cidade)
                        .addAllPrevisao(temperaturas)
                        .build();

                responseObserver.onNext(resposta);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Throwable("Dados de previsão não encontrados."));
            }
        } catch (Exception e) {
            responseObserver.onError(new Throwable("Erro ao buscar previsão: " + e.getMessage()));
        }
    }

    @Override
    public void listarCidades(Weather.Empty request, StreamObserver<Weather.CidadesResponse> responseObserver) {
        Weather.CidadesResponse resposta = Weather.CidadesResponse.newBuilder()
                .addAllCidades(cidades)
                .build();

        responseObserver.onNext(resposta);
        responseObserver.onCompleted();
    }

    @Override
    public void cadastrarCidade(Weather.CidadeRequest request, StreamObserver<Weather.CidadeResponse> responseObserver) {
        String cidade = request.getNome();
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s",
                cidade, apiKey
        );

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                if (!cidades.contains(cidade)) {
                    cidades.add(cidade);
                }

                Weather.CidadeResponse resposta = Weather.CidadeResponse.newBuilder()
                        .setMensagem("Cidade " + cidade + " cadastrada com sucesso!")
                        .build();
                responseObserver.onNext(resposta);
                responseObserver.onCompleted();
            } else {
                Weather.CidadeResponse resposta = Weather.CidadeResponse.newBuilder()
                        .setMensagem("Cidade inválida ou não encontrada.")
                        .build();
                responseObserver.onNext(resposta);
                responseObserver.onCompleted();
            }

        } catch (HttpClientErrorException.NotFound e) {
            // Cidade não encontrada (erro 404)
            Weather.CidadeResponse resposta = Weather.CidadeResponse.newBuilder()
                    .setMensagem("Cidade não encontrada na base da OpenWeatherMap.")
                    .build();
            responseObserver.onNext(resposta);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(new Throwable("Erro ao verificar cidade: " + e.getMessage()));
        }
    }


    @Override
    public void estatisticasClimaticas(Weather.CidadeRequest request, StreamObserver<Weather.EstatisticasResponse> responseObserver) {
        String cidade = request.getNome();
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&cnt=7&appid=%s", cidade, apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("list")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("list");

                List<Double> temperaturas = list.stream()
                        .map(item -> (Map<String, Object>) item.get("main"))
                        .map(main -> ((Number) main.get("temp")).doubleValue())
                        .collect(Collectors.toList());

                double media = temperaturas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double minima = temperaturas.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double maxima = temperaturas.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                Weather.EstatisticasResponse resposta = Weather.EstatisticasResponse.newBuilder()
                        .setCidade(cidade)
                        .setMedia(media)
                        .setMinima(minima)
                        .setMaxima(maxima)
                        .build();

                responseObserver.onNext(resposta);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Throwable("Dados insuficientes para estatísticas."));
            }
        } catch (Exception e) {
            responseObserver.onError(new Throwable("Erro ao calcular estatísticas: " + e.getMessage()));
        }
    }
}
