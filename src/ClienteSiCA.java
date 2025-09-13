import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente SiCA (Sistema de Compartilhamento de Arquivos)
 * Esta classe representa o cliente do sistema SiCA, permitindo que o usuário se
 * conecte ao servidor, envie comandos para listar, fazer upload e download de
 * arquivos. Toda a comunicação é feita via sockets TCP.
 */
public class ClienteSiCA {
  public static void main(String[] args) {
    // Defina aqui o endereço IP e a porta do servidor com o qual deseja se
    // conectar.
    // Certifique-se de que esses valores correspondem aos definidos no servidor.
    String enderecoServidor = "127.0.0.1"; // Utilizando localhost para testes locais
    int porta = 7979;

    try (
        // Cria um socket TCP e tenta estabelecer conexão com o servidor.
        Socket socket = new Socket(enderecoServidor, porta);
        // Permite enviar mensagens de texto para o servidor de forma simples.
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        // Permite receber mensagens de texto enviadas pelo servidor.
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Utiliza o Scanner para capturar o comando digitado pelo usuário no terminal.
        Scanner scanner = new Scanner(System.in)) {
      System.out.println("Conectado ao servidor SiCA. Digite um comando (LIST, UPLOAD, DOWNLOAD):");

      // Aguarda o usuário digitar um comando e lê a linha completa.
      String comando = scanner.nextLine();

      // Envia o comando digitado pelo usuário para o servidor.
      out.println(comando);

      // Neste momento, apenas enviamos o comando ao servidor.
      // A lógica para processar a resposta do servidor pode ser implementada
      // posteriormente.
      System.out.println("Comando enviado: " + comando);

    } catch (Exception e) {
      // Caso ocorra algum erro de conexão ou comunicação, exibe uma mensagem amigável
      // ao usuário.
      System.err.println("Erro ao conectar ou comunicar com o servidor: " + e.getMessage());
    }
  }
}