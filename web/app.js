/**
 * Conexão do front-end com o servidor Java via Fetch API.
 */
const botoes = document.querySelectorAll('[data-acao]');
const botaoReiniciar = document.querySelector('#reiniciar');

async function carregarEstado() {
  const resposta = await fetch('/api/estado');
  const dados = await resposta.json();
  renderizar(dados);
}

// TODO 4: Enviar a ação escolhida para a rota POST /api/jogar
async function enviarAcao(acao) {
  try {
    const resposta = await fetch('/api/jogar', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ acao })
    });
    const dados = await resposta.json();
    renderizar(dados);
  } catch (erro) {
    document.querySelector('#mensagem').textContent = 'Erro ao enviar ação: ' + erro.message;
  }
}

function renderizar(dados) {
  // Atualizações textuais principais
  document.querySelector('#vida-jogador-texto').textContent = `${dados.jogadorVida} / ${dados.jogadorMax} PV`;
  document.querySelector('#vida-chefe-texto').textContent = `${dados.chefeVida} / ${dados.chefeMax} PV`;
  document.querySelector('#pocoes').textContent = dados.pocoes;
  document.querySelector('#turno').textContent = `Turno ${dados.turno}`;
  document.querySelector('#mensagem').textContent = dados.mensagem;

  // TODO 5: Atualização das barras de vida (porcentagem de preenchimento)
  const pctJogador = Math.max(0, (dados.jogadorVida / dados.jogadorMax) * 100);
  const pctChefe = Math.max(0, (dados.chefeVida / dados.chefeMax) * 100);

  document.querySelector('#barra-jogador').style.width = `${pctJogador}%`;
  document.querySelector('#barra-chefe').style.width = `${pctChefe}%`;

  // TODO 8: Atualizar aviso do próximo turno
  document.querySelector('#aviso').textContent = dados.aviso;

  // TODO 6: Desabilitar/habilitar botões de ação se o jogo terminou
  botoes.forEach(botao => {
    botao.disabled = dados.encerrado;
  });

  // TODO 7: Exibir o resultado final (Vitória / Derrota)
  const elementoResultado = document.querySelector('#resultado');
  if (dados.resultado === 'vitoria') {
    elementoResultado.textContent = '🏆 VITÓRIA! Você derrotou o chefão!';
  } else if (dados.resultado === 'derrota') {
    elementoResultado.textContent = '💀 DERROTA! Você caiu em batalha!';
  } else {
    elementoResultado.textContent = '';
  }
}

// Ouvintes de eventos
botoes.forEach(botao => {
  botao.addEventListener('click', () => enviarAcao(botao.dataset.acao));
});

botaoReiniciar.addEventListener('click', async () => {
  const resposta = await fetch('/api/reiniciar', { method: 'POST' });
  const dados = await resposta.json();
  renderizar(dados);
});

// Inicialização
carregarEstado().catch(erro => {
  document.querySelector('#mensagem').textContent = 'Falha ao conectar ao Java: ' + erro.message;
});