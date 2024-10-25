package conversor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.ResultadoConversao;
import utils.EntradaDeDados;
import utils.GeradorDeArquivos;
import utils.ObterCodigoMoeda;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Scanner menu = new Scanner(System.in);
        GeradorDeArquivos gerador = new GeradorDeArquivos();
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦");
        System.out.println("♦CONVERSOR DE MOEDAS ONLINE - SIMPLES, RÁPIDO E PRECISO!♦");
        System.out.println("♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦♦");

        int sair = 1;

        while (sair != 0) {
            int[] moedas = EntradaDeDados.selecionarMoedas(menu);
            int origem = moedas[0];
            int destino = moedas[1];

            double valorParaConverter = EntradaDeDados.validarValor(menu);

            String codigoMoedaOrigem = ObterCodigoMoeda.obterCodigoMoeda(origem);
            String codigoMoedaDestino = ObterCodigoMoeda.obterCodigoMoeda(destino);

            if (codigoMoedaOrigem == null || codigoMoedaDestino == null) {
                System.out.println("Opção de moeda inválida. Tente novamente.");
                continue;
            }

            String link = "https://v6.exchangerate-api.com/v6/01c207c66b29a443f914a3ea/latest/USD" + codigoMoedaOrigem + "/" + codigoMoedaDestino;

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(link))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(response.body());
                JsonObject jsonobj = root.getAsJsonObject();

                double taxaDeCambio = jsonobj.get("conversion_rate").getAsDouble();
                double valorConvertido = converterMoeda(valorParaConverter, taxaDeCambio);

                EntradaDeDados.exibirResultado(valorConvertido, codigoMoedaDestino, taxaDeCambio, codigoMoedaOrigem, valorParaConverter);

                ResultadoConversao resultado = new ResultadoConversao(valorParaConverter, valorConvertido, codigoMoedaOrigem, codigoMoedaDestino, taxaDeCambio);
                gerador.geradorJson(resultado, "resultado");

            } catch (IOException | InterruptedException e) {
                System.out.println("Erro ao tentar conectar. Tente novamente");
            } catch (Exception e) {
                System.out.println("Ocorreu um erro inesperado: " + e.getMessage());
            }

            System.out.println("Deseja realizar outra conversão? (1 - Sim, 0 - Não)");
            sair = menu.nextInt();
        }

        menu.close();
    }

    public static double converterMoeda(double valor, double taxa) {
        return valor * taxa;
    }
}
