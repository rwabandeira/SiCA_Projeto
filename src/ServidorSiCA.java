import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor SiCA (Sistema de Compartilhamento de Arquivos)
 * Este servidor é responsável por receber conexões de clientes e processar
 * comandos relacionados ao compartilhamento de arquivos. Ele utiliza sockets
 * TCP para comunicação e permanece em execução contínua, pronto para atender
 * múltiplos clientes.
 */
public class ServidorSiCA {
  public static void main(String[] args) {
    // Porta utilizada para comunicação entre cliente e servidor.
    // Certifique-se de que o cliente também está configurado para usar esta porta.
    int porta = 7979;
    ServerSocket serverSocket = null;

    try {
      // Inicializa o servidor na porta definida.
      // Caso a porta já esteja ocupada, será lançada uma exceção.
      serverSocket = new ServerSocket(porta);
      System.out.println("Servidor SiCA iniciado. Aguardando conexões na porta " + porta + "...");

      // O servidor permanece em execução, aceitando conexões de clientes de forma
      // contínua.
      while (true) {
        // Aguarda até que um cliente se conecte.
        // Quando isso acontece, um novo socket é criado para comunicação com esse
        // cliente.
        Socket clienteSocket = serverSocket.accept();
        System.out.println("Novo cliente conectado de " + clienteSocket.getInetAddress().getHostAddress());

        // Utiliza try-with-resources para garantir que os recursos de entrada e saída
        // sejam fechados automaticamente após o uso.
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true)) {

          // Lê o comando enviado pelo cliente (espera-se que seja a primeira linha
          // recebida).
          String comando = in.readLine();
          System.out.println("Comando recebido do cliente: " + comando);

          // Analisa o comando recebido e responde de acordo.
          switch (comando) {
            case "LIST":
              // Em versões futuras, aqui será implementada a listagem de arquivos
              // disponíveis.
              out.println("Comando LIST recebido. Lógica a ser implementada.");
              break;
            case "UPLOAD":
              // Em versões futuras, aqui será implementado o recebimento de arquivos do
              // cliente.
              out.println("Comando UPLOAD recebido. Lógica a ser implementada.");
              break;
            case "DOWNLOAD":
              // Em versões futuras, aqui será implementado o envio de arquivos para o
              // cliente.
              out.println("Comando DOWNLOAD recebido. Lógica a ser implementada.");
              break;
            default:
              // Caso o comando não seja reconhecido, informa o cliente sobre as opções
              // válidas.
              out.println("Comando inválido. Tente LIST, UPLOAD ou DOWNLOAD.");
              break;
          }
        } catch (IOException e) {
          // Caso ocorra algum erro de entrada/saída durante a comunicação com o cliente,
          // uma mensagem de erro será exibida.
          System.err.println("Erro de E/S na comunicação com o cliente: " + e.getMessage());
        } finally {
          // Após o atendimento ao cliente, garante que o socket seja fechado
          // corretamente.
          try {
            clienteSocket.close();
            System.out.println("Conexão com o cliente fechada.");
          } catch (IOException e) {
            System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
          }
        }
      }

    } catch (IOException e) {
      // Caso ocorra algum erro ao iniciar o servidor, exibe detalhes para facilitar o
      // diagnóstico.
      System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Ao encerrar o servidor, garante que o ServerSocket seja fechado corretamente.
      if (serverSocket != null && !serverSocket.isClosed()) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          System.err.println("Erro ao fechar o ServerSocket: " + e.getMessage());
        }
      }
    }
  }
}
