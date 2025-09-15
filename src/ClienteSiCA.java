import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente SiCA (Sistema de Compartilhamento de Arquivos)
 * * Esta classe representa a aplicação cliente que interage com o servidor
 * SiCA.
 * Ela oferece uma interface de linha de comando para que o usuário possa
 * listar, fazer upload e download de arquivos. A comunicação é feita via
 * sockets TCP.
 */
public class ClienteSiCA {
  private static final String ENDERECO_SERVIDOR = "127.0.0.1";
  private static final int PORTA = 7979;

  public static void main(String[] args) {
    try {
      Scanner scanner = new Scanner(System.in);
      String comando = "";

      while (true) {
        try (Socket socket = new Socket(ENDERECO_SERVIDOR, PORTA)) {
          PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
          BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

          exibirMenu();
          int escolha = scanner.nextInt();
          scanner.nextLine();

          switch (escolha) {
            case 1:
              comando = "LIST";
              break;
            case 2:
              System.out.println("Digite o nome do arquivo para UPLOAD:");
              String nomeArquivoUpload = scanner.nextLine();
              comando = "UPLOAD " + nomeArquivoUpload;
              break;
            case 3:
              System.out.println("Digite o nome do arquivo para DOWNLOAD:");
              String nomeArquivoDownload = scanner.nextLine();
              comando = "DOWNLOAD " + nomeArquivoDownload;
              break;
            case 4:
              System.out.println("Finalizando cliente. Até mais!");
              return;
            default:
              System.out.println("Opção inválida. Tente novamente.");
              continue;
          }

          saida.println(comando);

          String[] partesComando = comando.split(" ", 2);
          String operacao = partesComando[0].toUpperCase();

          switch (operacao) {
            case "LIST":
              executarComandoListar(entrada);
              break;
            case "UPLOAD":
              executarComandoUpload(partesComando, socket, entrada);
              break;
            case "DOWNLOAD":
              executarComandoDownload(partesComando, socket, entrada);
              break;
            default:
              String resposta = entrada.readLine();
              if (resposta != null) {
                System.out.println("Resposta do servidor:\n" + resposta);
              }
              break;
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Erro de conexão ou I/O: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Exibe o menu de opções para o usuário.
   */
  private static void exibirMenu() {
    System.out.println("\n--- MENU DE OPÇÕES ---");
    System.out.println("1. Listar arquivos");
    System.out.println("2. Fazer upload de um arquivo");
    System.out.println("3. Fazer download de um arquivo");
    System.out.println("4. Sair");
    System.out.print("Escolha uma opção: ");
  }

  /**
   * Lida com a resposta do comando LIST do servidor.
   *
   * @param entrada O BufferedReader para ler a resposta.
   * @throws IOException Se ocorrer um erro de E/S.
   */
  private static void executarComandoListar(BufferedReader entrada) throws IOException {
    String respostaServidor;
    System.out.println("\n--- ARQUIVOS NO SERVIDOR ---");
    while ((respostaServidor = entrada.readLine()) != null) {
      System.out.println(respostaServidor);
    }
  }

  /**
   * Lida com o comando UPLOAD, enviando um arquivo local para o servidor.
   *
   * @param partesComando O array de strings do comando.
   * @param socket        O socket do cliente para enviar o stream de bytes.
   * @param entrada       O BufferedReader para ler a resposta do servidor.
   * @throws IOException Se ocorrer um erro de E/S.
   */
  private static void executarComandoUpload(String[] partesComando, Socket socket, BufferedReader entrada)
      throws IOException {
    if (partesComando.length < 2) {
      System.err.println("Uso: UPLOAD <nome_do_arquivo>");
      return;
    }
    String nomeArquivo = partesComando[1];
    File arquivoLocal = new File(nomeArquivo);

    if (!arquivoLocal.exists() || arquivoLocal.isDirectory()) {
      System.err.println("Erro: O arquivo " + nomeArquivo + " não existe ou é um diretório.");
      return;
    }

    System.out.println("Iniciando upload do arquivo: " + nomeArquivo + "...");

    try (FileInputStream fis = new FileInputStream(arquivoLocal)) {
      OutputStream saidaStream = socket.getOutputStream();
      byte[] buffer = new byte[4096];
      int bytesLidos;

      while ((bytesLidos = fis.read(buffer)) != -1) {
        saidaStream.write(buffer, 0, bytesLidos);
      }
      saidaStream.flush();
      socket.shutdownOutput();

      String respostaServidor = entrada.readLine();
      System.out.println("Resposta do servidor: " + respostaServidor);

    } catch (IOException e) {
      System.err.println("Erro ao realizar upload: " + e.getMessage());
    }
  }

  /**
   * Lida com o comando DOWNLOAD, recebendo um arquivo do servidor e salvando-o
   * localmente.
   *
   * @param partesComando O array de strings do comando.
   * @param socket        O socket do cliente para ler o stream de bytes.
   * @param entrada       O BufferedReader para ler a resposta do servidor.
   * @throws IOException Se ocorrer um erro de E/S.
   */
  private static void executarComandoDownload(String[] partesComando, Socket socket, BufferedReader entrada)
      throws IOException {
    if (partesComando.length < 2) {
      System.err.println("Uso: DOWNLOAD <nome_do_arquivo>");
      return;
    }
    String nomeArquivoDownload = partesComando[1];

    String respostaServidor = entrada.readLine();
    if (respostaServidor == null || respostaServidor.startsWith("ERRO")) {
      System.err.println("Erro do servidor: " + respostaServidor);
      return;
    }

    File novoArquivoLocal = new File("downloaded_" + nomeArquivoDownload);
    System.out.println("Iniciando download de '" + nomeArquivoDownload + "'...");

    try (FileOutputStream fos = new FileOutputStream(novoArquivoLocal)) {
      InputStream entradaStream = socket.getInputStream();
      byte[] buffer = new byte[4096];
      int bytesLidos;

      while ((bytesLidos = entradaStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesLidos);
      }
      System.out.println("Arquivo salvo como: " + novoArquivoLocal.getName());

    } catch (IOException e) {
      System.err.println("Erro ao baixar o arquivo: " + e.getMessage());
    }
  }
}