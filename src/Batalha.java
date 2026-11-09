import java.util.Random;

/**
 * MODELO - Regras de negócio da Batalha.
 */
public class Batalha {
    private int vidaJogador = 100;
    private int vidaChefe = 150;
    private int pocoes = 2;
    private int turno = 1;
    private boolean encerrado = false;
    private String resultado = "em_andamento";
    private String mensagem = "A batalha começou! Escolha sua primeira ação.";
    
    private final Random random = new Random();

    public synchronized String agir(String acao) {
        // Se a partida já acabou, não processa nenhuma ação
        if (encerrado) {
            return estadoJson();
        }

        // --- VALIDAÇÃO E AÇÃO DO JOGADOR ---
        boolean acaoValida = true;
        String logJogador = "";

        if (acao.equals("atacar")) {
            // Dano de 18 a 25
            int dano = 18 + random.nextInt(8); // 18 + (0 a 7)
            vidaChefe = Math.max(0, vidaChefe - dano);
            logJogador = "Você atacou o chefão.";
        } else if (acao.equals("defender")) {
            logJogador = "Você assumiu posição de defesa.";
        } else if (acao.equals("curar")) {
            // Validações de cura
            if (pocoes <= 0) {
                mensagem = "Você não tem mais poções!";
                return estadoJson(); // Não gasta turno nem poção
            }
            if (vidaJogador >= 100) {
                mensagem = "Sua vida já está no máximo!";
                return estadoJson(); // Não gasta turno nem poção
            }
            
            // Cura de 26 a 34
            int cura = 26 + random.nextInt(9); // 26 + (0 a 8)
            vidaJogador = Math.min(100, vidaJogador + cura);
            pocoes--;
            logJogador = "Você tomou uma poção e recuperou vida.";
        } else {
            return estadoJson(); // Ação desconhecida
        }

        // --- VERIFICAÇÃO DE VITÓRIA (CHEFE MORREU) ---
        if (vidaChefe <= 0) {
            encerrado = true;
            resultado = "vitoria";
            mensagem = logJogador + " O chefão foi derrotado! Você venceu!";
            return estadoJson(); // Chefe morto não contra-ataca
        }

        // --- CONTRA-ATAQUE DO CHEFE ---
        String logChefe = "";
        int danoChefe;

        // Regra do 3º turno: Especial (32 a 42) vs Comum (11 a 17)
        if (turno % 3 == 0) {
            danoChefe = 32 + random.nextInt(11); // 32 + (0 a 10)
            logChefe = " O chefão usou um GOLPE ESPECIAL!";
        } else {
            danoChefe = 11 + random.nextInt(7); // 11 + (0 a 6)
            logChefe = " O chefão contra-atacou.";
        }

        // Se o jogador defendeu: reduz dano a 1/4 (mínimo 1)
        if (acao.equals("defender")) {
            danoChefe = Math.max(1, danoChefe / 4);
        }

        vidaJogador = Math.max(0, vidaJogador - danoChefe);

        // --- VERIFICAÇÃO DE DERROTA (JOGADOR MORREU) ---
        if (vidaJogador <= 0) {
            encerrado = true;
            resultado = "derrota";
            mensagem = logJogador + logChefe + " Você foi derrotado!";
        } else {
            mensagem = logJogador + logChefe;
            turno++; // Incrementa o turno SOMENTE se a ação foi válida e a batalha continua
        }

        return estadoJson();
    }

    public synchronized String reiniciar() {
        vidaJogador = 100;
        vidaChefe = 150;
        pocoes = 2;
        turno = 1;
        encerrado = false;
        resultado = "em_andamento";
        mensagem = "Nova batalha! Boa sorte.";
        return estadoJson();
    }

    private String avisoProximoTurno() {
        if (encerrado) return "Batalha encerrada.";
        if (turno % 3 == 0) {
            return "⚠️ ATENÇÃO! O próximo contra-ataque será ESPECIAL!";
        }
        return "Próximo contra-ataque: COMUM.";
    }

    public synchronized String estadoJson() {
        return "{" +
            "\"jogadorVida\":" + vidaJogador + "," +
            "\"jogadorMax\":100," +
            "\"chefeVida\":" + vidaChefe + "," +
            "\"chefeMax\":150," +
            "\"pocoes\":" + pocoes + "," +
            "\"turno\":" + turno + "," +
            "\"encerrado\":" + encerrado + "," +
            "\"resultado\":\"" + escapar(resultado) + "\"," +
            "\"mensagem\":\"" + escapar(mensagem) + "\"," +
            "\"aviso\":\"" + escapar(avisoProximoTurno()) + "\"" +
            "}";
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}