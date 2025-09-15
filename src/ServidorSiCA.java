import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

/**
 * Servidor SiCA (Sistema de Compartilhamento de Arquivos)
 * Responsável por inicializar o servidor, gerenciar a comunicação
 * com os clientes através de sockets TCP e processar as requisições de
 * LISTAR, UPLOAD e DOWNLOAD de arquivos.
 */
public class ServidorSiCA {
  private static final int PORTA = 7979;
  private static final String DIRETORIO_ARQUIVOS = "arquivos_servidor";

  public static void main(String[] args) {
    // Garante que o diretório de arquivos exista.
    File diretorio = new File(DIRETORIO_ARQUIVOS);
    if (!diretorio.exists()) {
      diretorio.mkdirs();
    }

    try (ServerSocket servidorSocket = new ServerSocket(PORTA)) {
      System.out.println("Servidor SiCA iniciado. Aguardando conexões na porta " + PORTA + "...");

      while (true) {
        // Aceita a conexão de um novo cliente. O método accept() bloqueia a execução.
        Socket clienteSocket = servidorSocket.accept();
        System.out.println("Novo cliente conectado de " + clienteSocket.getInetAddress().getHostAddress());

        // Para o propósito deste exercício, vamos processar a requisição diretamente.
        processarRequisicaoCliente(clienteSocket);
      }
    } catch (IOException e) {
      System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void processarRequisicaoCliente(Socket clienteSocket) {
    try (
        BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
        PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true)) {
      // Lê o comando enviado pelo cliente.
      String comando = entrada.readLine();
      if (comando == null) {
        System.out.println("Cliente desconectado.");
        return;
      }

      System.out.println("Comando recebido do cliente: " + comando);

      String[] partesComando = comando.split(" ", 2);
      String operacao = partesComando[0].toUpperCase();

      // Processa o comando.
      switch (operacao) {
        case "LISTAR":
          executarComandoListar(saida);
          break;
        case "UPLOAD":
          executarComandoUpload(partesComando, entrada, clienteSocket);
          break;
        case "DOWNLOAD":
          executarComandoDownload(partesComando, saida, clienteSocket);
          break;
        default:
          saida.println("Comando inválido. Use LISTAR, UPLOAD <nome_do_arquivo> ou DOWNLOAD <nome_do_arquivo>.");
          break;
      }
    } catch (IOException e) {
      System.err.println("Erro de E/S na comunicação com o cliente: " + e.getMessage());
    } finally {
      try {
        if (!clienteSocket.isClosed()) {
          clienteSocket.close();
          System.out.println("Conexão com o cliente fechada.");
        }
      } catch (IOException e) {
        System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
      }
    }
  }

  /**
   * Lida com o comando LISTAR, enviando a lista de arquivos disponíveis para o
   * cliente.
   *
   * @param saida O PrintWriter para enviar a resposta ao cliente.
   */
  private static void executarComandoListar(PrintWriter saida) {
    File diretorio = new File(DIRETORIO_ARQUIVOS);
    File[] arquivos = diretorio.listFiles();

    StringBuilder listaArquivos = new StringBuilder();
    if (arquivos != null && arquivos.length > 0) {
      for (File arquivo : arquivos) {
        if (arquivo.isFile()) {
          listaArquivos.append(arquivo.getName()).append("\n");
        }
      }
      saida.println(listaArquivos.toString());
    } else {
      saida.println("Nenhum arquivo encontrado no servidor.");
    }
    System.out.println("Lista de arquivos enviada para o cliente.");
  }

  /**
   * Lida com o comando UPLOAD, recebendo um arquivo do cliente e salvando-o.
   *
   * @param partesComando O array de strings do comando.
   * @param entrada       O BufferedReader para ler a resposta do cliente.
   * @param clienteSocket O socket do cliente para ler o stream de bytes.
   * @throws IOException Se ocorrer um erro de E/S.
   */
  private static void executarComandoUpload(String[] partesComando, BufferedReader entrada, Socket clienteSocket)
      throws IOException {
    if (partesComando.length < 2) {
      new PrintWriter(clienteSocket.getOutputStream(), true)
          .println("Comando UPLOAD inválido. Tente: UPLOAD <nome_do_arquivo>");
      return;
    }

    String nomeArquivoUpload = partesComando[1];
    File arquivoServidor = new File(DIRETORIO_ARQUIVOS + "/" + nomeArquivoUpload);

    try (FileOutputStream fos = new FileOutputStream(arquivoServidor)) {
      InputStream entradaStream = clienteSocket.getInputStream();
      byte[] buffer = new byte[4096];
      int bytesLidos;

      while ((bytesLidos = entradaStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesLidos);
      }

      PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true);
      saida.println("Arquivo " + nomeArquivoUpload + " recebido com sucesso.");
      System.out.println("Arquivo " + nomeArquivoUpload + " salvo no servidor.");

    } catch (IOException e) {
      System.err.println("Erro ao receber o arquivo: " + e.getMessage());
      new PrintWriter(clienteSocket.getOutputStream(), true).println("Erro ao receber o arquivo: " + e.getMessage());
    }
  }

  /**
   * Lida com o comando DOWNLOAD, enviando um arquivo para o cliente.
   *
   * @param partesComando O array de strings do comando.
   * @param saida         O PrintWriter para enviar a resposta inicial ao cliente.
   * @param clienteSocket O socket do cliente para enviar o stream de bytes.
   * @throws IOException Se ocorrer um erro de E/S.
   */
  private static void executarComandoDownload(String[] partesComando, PrintWriter saida, Socket clienteSocket)
      throws IOException {
    if (partesComando.length < 2) {
      saida.println("Comando DOWNLOAD inválido. Tente: DOWNLOAD <nome_do_arquivo>");
      return;
    }

    String nomeArquivoDownload = partesComando[1];
    File arquivoParaEnviar = new File(DIRETORIO_ARQUIVOS + "/" + nomeArquivoDownload);

    if (!arquivoParaEnviar.exists() || arquivoParaEnviar.isDirectory()) {
      saida.println("ERRO: O arquivo '" + nomeArquivoDownload + "' não existe no servidor.");
      return;
    }

    saida.println("OK");
    saida.flush();

    try (FileInputStream fis = new FileInputStream(arquivoParaEnviar)) {
      OutputStream saidaStream = clienteSocket.getOutputStream();
      byte[] buffer = new byte[4096];
      int bytesLidos;

      while ((bytesLidos = fis.read(buffer)) != -1) {
        saidaStream.write(buffer, 0, bytesLidos);
      }
      saidaStream.flush();
      System.out.println("Arquivo '" + nomeArquivoDownload + "' enviado para o cliente.");

    } catch (IOException e) {
      System.err.println("Erro ao enviar o arquivo: " + e.getMessage());
    }
  }
}