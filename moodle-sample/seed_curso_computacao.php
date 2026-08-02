<?php
/**
 * seed_curso_computacao.php
 *
 * Cria, dentro de uma instância Moodle já rodando, o curso:
 *   "Fundamentos de Computação"
 * com 9 módulos de conteúdo (página + quiz com 2 perguntas cada), 1
 * professor e 5 alunos matriculados.
 *
 * IDEMPOTENTE: pode rodar quantas vezes quiser. Curso, seções, páginas,
 * quizzes, perguntas, usuários e matrículas que já existem são detectados
 * e pulados (mensagem [SKIP]); só o que ainda não existe é criado.
 *
 * DIAGNÓSTICO: cada etapa roda dentro da própria transação de banco. Se
 * algo falhar, o script desfaz só aquela transação, mostra em qual etapa
 * exata falhou, a mensagem real do Moodle/banco, o arquivo/linha e o
 * stack trace completo, e para a execução ali (exit 1) — sem deixar dados
 * pela metade.
 *
 * COMO USAR
 * ---------
 * 1. Copie este arquivo para dentro do container do Moodle, na RAIZ do
 *    Moodle (onde fica o config.php):
 *
 *      docker cp seed_curso_computacao.php <container_moodle>:/var/www/html/seed_curso_computacao.php
 *
 * 2. Rode dentro do container:
 *
 *      docker exec -it <container_moodle> php seed_curso_computacao.php
 *
 * 3. Se der erro, a saída já mostra a causa exata. Corrija e rode de novo —
 *    o que já foi criado com sucesso antes é detectado e pulado.
 *
 * REQUISITOS / AVISOS
 * --------------------
 * - Faça backup do banco antes de rodar em ambiente que já tem dados reais.
 * - Este script cria um curso NOVO (shortname diferente do script anterior
 *   de "Algoritmos"), para não misturar conteúdo antigo com os novos
 *   tópicos. Ajuste $CATEGORYID e a senha padrão dos usuários antes de
 *   rodar em produção.
 */

define('CLI_SCRIPT', true);
require(__DIR__ . '/config.php');
require_once($CFG->dirroot . '/course/lib.php');
require_once($CFG->dirroot . '/course/modlib.php');
require_once($CFG->dirroot . '/user/lib.php');
require_once($CFG->dirroot . '/question/format.php');
require_once($CFG->dirroot . '/question/format/gift/format.php');
require_once($CFG->dirroot . '/question/editlib.php');
require_once($CFG->dirroot . '/mod/quiz/locallib.php');
require_once($CFG->libdir . '/enrollib.php');
require_once($CFG->libdir . '/moodlelib.php');
require_once($CFG->libdir . '/questionlib.php');
require_once($CFG->libdir . '/cronlib.php');
require_once($CFG->libdir . '/clilib.php');

// Rodando via CLI não existe $USER logado. cron_setup_user() configura o
// usuário admin como executor, do mesmo jeito que tarefas agendadas usam.
cron_setup_user();

// Este é um script de SEED/teste: os e-mails dos usuários criados são
// fictícios (@example.com). Sem isso, ações como matricular um aluno
// disparam notificações reais via core\message\manager, que agora lança
// uma exceção (em vez de só logar) quando a mensagem não consegue ser
// entregue a nenhum processador — o que sempre vai acontecer aqui, já
// que não há SMTP/processador de mensagens configurado (nem faria
// sentido, com e-mails fictícios). Desligamos ambos só durante a
// execução deste script.
$CFG->messaging  = 0;
$CFG->noemailever = true;

// ---------------------------------------------------------------------
// CONFIGURAÇÃO — ajuste aqui se necessário
// ---------------------------------------------------------------------
$CATEGORYID   = 1; // 1 = "Miscellaneous" / "Geral" por padrão.
$SHORTNAME    = 'COMPBAS-2026-V2';
$SENHA_PADRAO = 'Moodle#2026!'; // troque antes de usar em produção

// ---------------------------------------------------------------------
// FERRAMENTAS DE DIAGNÓSTICO E TRANSAÇÃO
// ---------------------------------------------------------------------

/**
 * Executa $fn dentro da própria transação de banco. Se der certo, confirma
 * (commit). Se der erro, desfaz (rollback) só essa transação e imprime
 * exatamente onde e por que falhou antes de encerrar o script.
 */
function seed_step($label, callable $fn) {
    global $DB;
    echo "-> $label ... ";
    $transaction = $DB->start_delegated_transaction();
    try {
        $result = $fn();
        $transaction->allow_commit();
        echo "OK\n";
        return $result;
    } catch (\Throwable $e) {
        $causaraiz = $e;
        try {
            $transaction->rollback($e);
        } catch (\dml_transaction_exception $errotransacao) {
            // Transação já tinha sido desfeita automaticamente — ignora
            // este erro secundário de propósito, a causa real é $causaraiz.
        } catch (\Throwable $rethrown) {
            $causaraiz = $rethrown;
        }
        echo "FALHOU\n";
        seed_print_error($label, $causaraiz);
        exit(1);
    }
}

function seed_print_error($label, \Throwable $e) {
    echo "\n============================================================\n";
    echo "[ERRO] Etapa que falhou: $label\n";
    echo "Tipo da exceção: " . get_class($e) . "\n";
    echo "Mensagem: " . $e->getMessage() . "\n";
    if (property_exists($e, 'debuginfo') && !empty($e->debuginfo)) {
        echo "Debug info (detalhe técnico / SQL): " . $e->debuginfo . "\n";
    }
    if (property_exists($e, 'errorcode') && !empty($e->errorcode)) {
        echo "Código do erro Moodle: " . $e->errorcode . "\n";
    }
    echo "Local exato: " . $e->getFile() . ", linha " . $e->getLine() . "\n";
    echo "Stack trace:\n" . $e->getTraceAsString() . "\n";
    echo "============================================================\n";
}

// ---------------------------------------------------------------------
// 1. CRIAR (OU REAPROVEITAR) O CURSO
// ---------------------------------------------------------------------
$course = $DB->get_record('course', ['shortname' => $SHORTNAME]);
if ($course) {
    echo "[SKIP] Curso '$SHORTNAME' já existe (id={$course->id}), reaproveitando.\n";
} else {
    $course = seed_step("Criar curso '$SHORTNAME'", function () use ($CATEGORYID, $SHORTNAME) {
        $coursedata = new stdClass();
        $coursedata->fullname      = 'Fundamentos de Computação';
        $coursedata->shortname     = $SHORTNAME;
        $coursedata->category      = $CATEGORYID;
        $coursedata->format        = 'topics';
        $coursedata->numsections   = 9;
        $coursedata->startdate     = time();
        $coursedata->visible       = 1;
        $coursedata->summary       = 'Curso introdutório de fundamentos de computação: como o computador '
                                    . 'funciona por dentro, o que são dados e informação, como programas '
                                    . 'funcionam, redes, internet, servidores, sistemas corporativos e IA.';
        $coursedata->summaryformat = FORMAT_HTML;
        $coursedata->newsitems     = 0;
        $coursedata->showgrades    = 1;
        return create_course($coursedata);
    });
}

// ---------------------------------------------------------------------
// 2. NOMEAR AS SEÇÕES
// ---------------------------------------------------------------------
$sectionnames = [
    1 => 'Módulo 1 – O que é um Computador',
    2 => 'Módulo 2 – O que são Informações',
    3 => 'Módulo 3 – O que é um Programa de Computador',
    4 => 'Módulo 4 – Redes de Computadores',
    5 => 'Módulo 5 – Dispositivos Modernos',
    6 => 'Módulo 6 – Internet',
    7 => 'Módulo 7 – Servidores e Clientes',
    8 => 'Módulo 8 – Sistemas Complexos Corporativos',
    9 => 'Módulo 9 – Inteligência Artificial',
];
seed_step("Nomear seções do curso", function () use ($course, $sectionnames, $DB) {
    foreach ($sectionnames as $num => $name) {
        $section = $DB->get_record('course_sections', ['course' => $course->id, 'section' => $num]);
        if ($section) {
            $DB->set_field('course_sections', 'name', $name, ['id' => $section->id]);
        }
    }
    rebuild_course_cache($course->id, true);
});

// ---------------------------------------------------------------------
// 3. HELPERS DE CONTEÚDO — módulos (page/forum/quiz) e perguntas (GIFT)
// ---------------------------------------------------------------------

function seed_module_exists($courseid, $modulename, $name) {
    global $DB;
    $sql = "SELECT cm.id
              FROM {course_modules} cm
              JOIN {modules} m ON m.id = cm.module
              JOIN {" . $modulename . "} i ON i.id = cm.instance
             WHERE cm.course = :courseid
               AND m.name = :modname
               AND i.name = :name";
    return $DB->record_exists_sql($sql, [
        'courseid' => $courseid,
        'modname'  => $modulename,
        'name'     => $name,
    ]);
}

function seed_editor_field($html) {
    return [
        'text'   => $html,
        'format' => FORMAT_HTML,
        'itemid' => 0,
    ];
}

/**
 * Consulta o schema real da tabela do módulo (ex: {page}, {quiz}, {forum})
 * via $DB->get_columns() e preenche com um valor neutro (0 ou '') qualquer
 * coluna NOT NULL que ainda não tenha sido definida em $data — mesmo que a
 * coluna tenha DEFAULT no schema, porque vários add_instance() de módulo
 * (ex: assign, quiz) copiam explicitamente $formdata->campo, e se a
 * propriedade não existe isso vira null e sobrescreve o DEFAULT do banco.
 * Funciona para qualquer módulo, em qualquer versão do Moodle, porque lê o
 * schema instalado de verdade em vez de depender de uma lista fixa.
 */
function seed_autofill_required_columns($modulename, $data) {
    global $DB;
    $ignorar = ['id', 'course', 'name', 'intro', 'introformat', 'timemodified', 'timecreated'];

    $colunas = $DB->get_columns($modulename);
    foreach ($colunas as $coluna) {
        $campo = $coluna->name;
        if (in_array($campo, $ignorar)) {
            continue;
        }
        if (property_exists($data, $campo) && $data->$campo !== null) {
            continue;
        }
        if (!$coluna->not_null) {
            continue;
        }
        switch ($coluna->meta_type) {
            case 'I':
            case 'N':
            case 'F':
            case 'L':
                $data->$campo = 0;
                break;
            case 'C':
            case 'X':
                $data->$campo = '';
                break;
        }
    }
    return $data;
}

/**
 * Cria um módulo (page/forum/quiz/...) na seção indicada, pulando se já
 * existir uma atividade com o mesmo nome no curso.
 */
function seed_create_module($course, $modulename, $sectionnum, $fields) {
    global $DB;

    if (seed_module_exists($course->id, $modulename, $fields['name'])) {
        echo "[SKIP] Atividade '{$fields['name']}' ($modulename) já existe no curso.\n";
        return null;
    }

    $moduleid = $DB->get_field('modules', 'id', ['name' => $modulename]);
    if (!$moduleid) {
        echo "[SKIP] Módulo '$modulename' não está instalado/habilitado nesta instância.\n";
        return null;
    }

    return seed_step("Criar atividade '{$fields['name']}' ($modulename, seção $sectionnum)",
        function () use ($course, $modulename, $sectionnum, $fields) {
            list($module, $context, $cw, $cm, $data) =
                prepare_new_moduleinfo_data($course, $modulename, $sectionnum);

            foreach ($fields as $k => $v) {
                $data->$k = $v;
            }

            $data = seed_autofill_required_columns($modulename, $data);

            return add_moduleinfo($data, $course, null);
        }
    );
}

/**
 * Importa um bloco de texto em formato GIFT (perguntas de múltipla
 * escolha com feedback) para uma categoria dedicada do banco de questões
 * do curso, criando a categoria se ainda não existir. Usa o importador
 * oficial do Moodle (qformat_gift) em vez de inserir direto nas tabelas
 * de pergunta — assim toda a complexidade interna (versionamento de
 * perguntas, categorias, contexto) é tratada pelo próprio Moodle.
 *
 * Retorna o array de ids das perguntas atualmente na categoria (cobre
 * tanto o schema novo do Moodle 4.x com versionamento quanto o antigo).
 */
function seed_import_gift_questions($course, $categoryname, $gifttext) {
    global $DB;

    $coursecontext = context_course::instance($course->id);
    $topcategory = question_get_top_category($coursecontext->id, true);

    $category = $DB->get_record('question_categories', [
        'contextid' => $coursecontext->id,
        'name'      => $categoryname,
        'parent'    => $topcategory->id,
    ]);
    if (!$category) {
        $categorydata = new stdClass();
        $categorydata->name       = $categoryname;
        $categorydata->contextid  = $coursecontext->id;
        $categorydata->info       = '';
        $categorydata->infoformat = FORMAT_HTML;
        $categorydata->stamp      = make_unique_id_code();
        $categorydata->parent     = $topcategory->id;
        $categorydata->sortorder  = 999;
        $categoryid = $DB->insert_record('question_categories', $categorydata);
        $category = $DB->get_record('question_categories', ['id' => $categoryid]);
    }

    $tmpfile = tempnam(sys_get_temp_dir(), 'gift_');
    file_put_contents($tmpfile, $gifttext);

    $qformat = new qformat_gift();
    $qformat->setCategory($category);
    $qformat->setContexts([$coursecontext]);
    $qformat->setCourse($course);
    $qformat->setFilename($tmpfile);
    $qformat->setRealfilename('perguntas.gift');
    $qformat->setMatchgrades('error');
    $qformat->setCatfromfile(false);
    $qformat->setContextfromfile(false);
    $qformat->setStoponerror(true);

    $preprocessok = $qformat->importpreprocess();
    $importok = $preprocessok ? $qformat->importprocess() : false;
    @unlink($tmpfile);

    if (!$preprocessok || !$importok) {
        throw new \moodle_exception('erroimportargift', 'error', '', null,
            "Falha ao importar perguntas GIFT na categoria '$categoryname'. "
            . "Verifique a sintaxe do bloco GIFT correspondente no script.");
    }

    $dbman = $DB->get_manager();
    if ($dbman->table_exists('question_versions')) {
        return $DB->get_fieldset_sql(
            "SELECT q.id
               FROM {question} q
               JOIN {question_versions} qv ON qv.questionid = q.id
               JOIN {question_bank_entries} qbe ON qbe.id = qv.questionbankentryid
              WHERE qbe.questioncategoryid = :categoryid",
            ['categoryid' => $category->id]
        );
    }
    return $DB->get_fieldset_select('question', 'id', 'category = :categoryid', ['categoryid' => $category->id]);
}

/**
 * Adiciona uma lista de perguntas (por id) a um quiz já criado.
 */
function seed_add_questions_to_quiz($quizinstanceid, array $questionids) {
    global $DB;
    $quiz = $DB->get_record('quiz', ['id' => $quizinstanceid], '*', MUST_EXIST);
    foreach ($questionids as $questionid) {
        quiz_add_quiz_question($questionid, $quiz);
    }
    quiz_update_sumgrades($quiz);
}

/**
 * Cria a página de conteúdo + o quiz de um módulo, de forma padronizada.
 */
function seed_criar_topico($course, $sectionnum, $nometopico, $paginahtml, $gifttext) {
    // 1) Perguntas no banco de questões.
    $questionids = seed_step("Importar perguntas – $nometopico", function () use ($course, $nometopico, $gifttext) {
        return seed_import_gift_questions($course, "Perguntas – $nometopico", $gifttext);
    });

    // 2) Página de conteúdo.
    //
    // ATENÇÃO: page_add_instance() só copia o array editor 'page' (['text'=>...])
    // para as colunas reais 'content'/'contentformat' quando recebe um $mform
    // de verdade (`if ($mform) { $data->content = $data->page['text']; ... }`).
    // Como este script chama add_moduleinfo($data, $course, null) — ou seja,
    // sem form —, isso nunca acontece, e 'content' (NOT NULL) fica vazio.
    // Por isso preenchemos 'content'/'contentformat' diretamente aqui.
    seed_create_module($course, 'page', $sectionnum, [
        'name'         => $nometopico,
        'introeditor'  => seed_editor_field(''),
        'page'         => seed_editor_field($paginahtml), // mantido por compatibilidade
        'content'      => $paginahtml,
        'contentformat'=> FORMAT_HTML,
        'display'      => 5, // RESOURCELIB_DISPLAY_AUTO
        'printheading' => 1,
        'printintro'   => 0,
    ]);

    // 3) Quiz do módulo.
    $quizcm = seed_create_module($course, 'quiz', $sectionnum, [
        'name'               => "Quiz – $nometopico",
        'introeditor'        => seed_editor_field('<p>Responda às perguntas abaixo para checar seu entendimento do módulo.</p>'),
        'preferredbehaviour' => 'deferredfeedback', // aluno vê o resultado só depois de enviar
        'attempts'           => 0,  // 0 = tentativas ilimitadas
        'grademethod'        => 1,  // 1 = usar a nota mais alta entre as tentativas
        'questionsperpage'   => 0,  // 0 = todas as perguntas numa página só
        'navmethod'          => 'free',
        'shuffleanswers'     => 1,
        'grade'              => 10,
        // O mod_form do quiz usa o nome 'quizpassword' (não 'password') de
        // propósito, para navegadores não tentarem autocompletar esse campo
        // com a senha de login salva do usuário. O código interno do quiz
        // faz $quiz->password = $quiz->quizpassword, sobrescrevendo
        // qualquer valor que a gente ponha direto em 'password'.
        'quizpassword'       => '', // sem senha de acesso ao quiz
        'password'           => '', // mantido por segurança/compatibilidade
    ]);

    if ($quizcm && !empty($quizcm->instance) && !empty($questionids)) {
        seed_step("Adicionar perguntas ao Quiz – $nometopico", function () use ($quizcm, $questionids) {
            seed_add_questions_to_quiz($quizcm->instance, $questionids);
        });
    } elseif (empty($quizcm)) {
        echo "[INFO] Quiz de '$nometopico' já existia — perguntas não foram (re)vinculadas automaticamente.\n";
    }
}

// ---------------------------------------------------------------------
// 4. CONTEÚDO — 9 módulos (página + quiz de 2 perguntas cada)
// ---------------------------------------------------------------------

// --- Módulo 1: O que é um Computador ---
seed_criar_topico($course, 1, 'O que é um Computador',
    '<h3>Introdução: o que faz de algo um "computador"?</h3>
    <p>Um computador é qualquer máquina capaz de <strong>receber dados de entrada, processar essas
    informações seguindo instruções, e produzir uma saída</strong>. Essa definição é propositalmente
    ampla: ela vale tanto para um supercomputador de laboratório quanto para o celular no seu bolso ou
    para o computador de bordo de um carro moderno. Ao longo deste módulo vamos abrir essa "caixa-preta"
    e entender as peças que fazem isso acontecer.</p>

    <h3>As três funções essenciais</h3>
    <p>Todo computador — não importa o tamanho ou a marca — é organizado em torno de três grandes
    funções, que trabalham em conjunto o tempo todo:</p>
    <ul>
      <li><strong>Processamento (CPU — Unidade Central de Processamento):</strong> é o "cérebro" da
      máquina. A CPU executa instruções muito simples (somar dois números, comparar dois valores, mover
      um dado de um lugar para o outro), mas faz isso bilhões de vezes por segundo. É a velocidade e a
      repetição dessas operações simples que criam a sensação de "inteligência" que vemos em qualquer
      programa complexo.</li>
      <li><strong>Memória (RAM — Random Access Memory):</strong> é o espaço de trabalho temporário do
      computador. Quando você abre um programa, parte dele é copiada para a RAM, porque ler e escrever
      ali é extremamente rápido — muito mais rápido do que acessar o armazenamento permanente. A
      contrapartida é que a RAM é <strong>volátil</strong>: tudo o que está nela desaparece assim que o
      computador é desligado ou reiniciado.</li>
      <li><strong>Armazenamento (HD ou SSD):</strong> é onde os dados ficam guardados de forma
      permanente, mesmo sem energia elétrica. Seus documentos, fotos, aplicativos instalados e o próprio
      sistema operacional moram aqui. É mais lento que a RAM, mas não perde as informações quando o
      computador é desligado.</li>
    </ul>

    <h3>Uma analogia para fixar</h3>
    <p>Imagine uma pessoa trabalhando em um escritório:</p>
    <ul>
      <li>O <strong>armário de arquivos</strong> no canto da sala é o <em>armazenamento</em>: guarda tudo
      de forma organizada e permanente, mas é preciso levantar e ir até lá buscar o que precisa.</li>
      <li>A <strong>mesa de trabalho</strong> é a <em>memória RAM</em>: só cabem ali os documentos que
      estão sendo usados agora mesmo, e quando a pessoa vai embora no fim do expediente, a mesa é
      limpa — nada fica ali de um dia para o outro.</li>
      <li>A <strong>própria pessoa</strong>, lendo os documentos, fazendo contas e tomando decisões, é a
      <em>CPU</em>: o processamento de fato acontece nela, usando o que está disponível na mesa naquele
      momento.</li>
    </ul>

    <h3>Como as peças se conectam</h3>
    <p>Essas três peças (e outras, como a placa-mãe, que serve de "sistema nervoso" conectando tudo, e a
    fonte de energia) trabalham em ciclos extremamente rápidos: a CPU busca uma instrução na memória,
    executa essa instrução, eventualmente busca ou grava um dado no armazenamento, e passa para a
    próxima instrução. Esse ciclo se repete continuamente enquanto o computador está ligado — é
    literalmente isso que "processar" significa, em termos técnicos.</p>

    <h3>Por que isso importa</h3>
    <p>Entender essa separação entre processamento, memória temporária e armazenamento permanente é a
    base para entender praticamente tudo o que vem depois neste curso: por que um computador "trava" com
    poucos programas abertos ao mesmo tempo (falta de RAM), por que perder energia no meio de um trabalho
    não salvo apaga o que você estava fazendo (a RAM é volátil), e por que um arquivo salvo continua lá
    mesmo depois de reiniciar o computador (o armazenamento não é volátil).</p>',
    <<<'GIFT'
::Modulo1 Q1:: Qual das opções abaixo descreve corretamente a função da memória RAM em um computador? {
=Armazenar temporariamente os dados e programas que estão sendo usados no momento, perdendo tudo ao desligar.
~Guardar arquivos permanentemente, mesmo com o computador desligado.
~Executar as instruções dos programas, fazendo os cálculos.
~Conectar o computador a outros computadores em rede.
####RAM significa "Random Access Memory". Ela é rápida e serve como espaço de trabalho temporário -- diferente do HD/SSD, que guarda dados de forma permanente, e da CPU, que executa as instruções.
}

::Modulo1 Q2:: O que acontece com os dados guardados no SSD ou HD quando o computador é desligado? {
=Eles continuam salvos normalmente, pois é um tipo de armazenamento permanente.
~Eles são apagados imediatamente, como acontece com a memória RAM.
~Eles são movidos automaticamente para a internet.
~Eles são convertidos em instruções de processamento.
####Diferente da RAM (volátil), o armazenamento em HD ou SSD é permanente -- é justamente por isso que seus arquivos continuam lá da próxima vez que você liga o computador.
}
GIFT
);

// --- Módulo 2: O que são Informações ---
seed_criar_topico($course, 2, 'O que são Informações',
    '<h3>Dado x Informação</h3>
    <p>É comum usar "dado" e "informação" como sinônimos no dia a dia, mas em computação vale a pena
    diferenciar: um <strong>dado</strong> é um valor bruto, isolado, sem contexto (um número, uma letra, a
    cor de um pixel). Uma <strong>informação</strong> surge quando esses dados são organizados de um jeito
    que faz sentido para alguém. Por exemplo: os números "18", "05" e "2026" são apenas dados soltos; mas
    organizados na ordem certa e com o rótulo "data de aniversário", eles viram informação de verdade.</p>

    <h3>Tudo dentro do computador é binário</h3>
    <p>Fisicamente, um computador é feito de circuitos elétricos que só reconhecem dois estados: ligado
    e desligado, representados por <strong>1</strong> e <strong>0</strong>. Esse é o chamado
    <em>sistema binário</em>. Cada 0 ou 1 individual é chamado de <strong>bit</strong> (do inglês
    "binary digit"), a menor unidade de informação possível. Como um único bit só consegue representar
    duas possibilidades, agrupamos vários bits para representar coisas mais complexas: um grupo de 8
    bits forma um <strong>byte</strong>, capaz de representar 256 valores diferentes (2 elevado à
    oitava potência).</p>

    <h3>Como o texto vira número</h3>
    <p>Cada letra, número ou símbolo de texto é associado a um número através de uma tabela de
    codificação. Um dos padrões mais conhecidos é o <strong>ASCII</strong>, onde, por exemplo, a letra
    maiúscula "A" corresponde ao número 65. Hoje em dia usamos principalmente o padrão
    <strong>Unicode</strong>, que expande essa ideia para representar praticamente qualquer caractere de
    qualquer idioma do mundo, além de emojis.</p>

    <h3>Como a imagem vira número</h3>
    <p>Uma imagem digital é dividida em uma grade de pequenos pontos chamados <strong>pixels</strong>.
    Cada pixel guarda números que representam sua cor — geralmente três valores, indicando a intensidade
    de vermelho, verde e azul (o modelo RGB). Uma foto com muitos pixels (alta resolução) é, no fundo,
    uma tabela enorme de números de cor guardados em sequência.</p>

    <h3>Como o som vira número</h3>
    <p>O som é uma onda contínua no ar. Para guardá-lo digitalmente, o computador faz medições
    (amostragens) da altura dessa onda várias milhares de vezes por segundo, e cada medição vira um
    número. Quanto mais medições por segundo (maior a taxa de amostragem), mais fiel é a reprodução do
    som original.</p>

    <h3>O ponto central</h3>
    <p>Textos, imagens, sons, vídeos e qualquer outro tipo de conteúdo têm, na origem, formas muito
    diferentes — mas todos, ao entrarem em um computador, são convertidos e representados exatamente da
    mesma maneira: sequências de bits, agrupados em bytes. É essa uniformização em binário que permite ao
    mesmo hardware processar coisas tão diferentes quanto uma planilha financeira e uma música.</p>',
    <<<'GIFT'
::Modulo2 Q1:: O que é um "bit" na computação? {
=A menor unidade de informação de um computador, podendo valer 0 ou 1.
~Um grupo de 8 bytes.
~Um tipo de arquivo de imagem.
~O nome de um processador antigo.
####Bit vem de "binary digit" (dígito binário). É a menor unidade possível de informação: só pode assumir dois valores, 0 ou 1. Um grupo de 8 bits forma um byte.
}

::Modulo2 Q2:: Como uma imagem digital é representada dentro do computador? {
=Dividida em pequenos pontos (pixels), cada um guardando números que representam sua cor.
~Como um texto comum, letra por letra.
~Diretamente como som, sem conversão.
~Não é possível representar imagens em binário.
####Toda imagem digital é uma grade de pixels, e cada pixel é armazenado como números (geralmente representando a intensidade de vermelho, verde e azul) -- e esses números, no fundo, são sequências de 0s e 1s.
}
GIFT
);

// --- Módulo 3: O que é um Programa de Computador ---
seed_criar_topico($course, 3, 'O que é um Programa de Computador',
    '<h3>Programa e algoritmo</h3>
    <p>Um <strong>programa de computador</strong> é uma sequência de instruções que a máquina executa,
    passo a passo, para resolver um problema ou realizar uma tarefa específica. Antes de qualquer código
    ser escrito, essa sequência de passos costuma ser planejada como um <strong>algoritmo</strong> —
    pense nele como uma receita de bolo, só que escrita para o computador seguir: uma lista clara e
    ordenada de ações que, se seguidas corretamente, sempre levam ao resultado esperado.</p>

    <h3>Como o programa toma decisões</h3>
    <p>Programas raramente seguem um único caminho fixo do início ao fim — eles precisam reagir a
    diferentes situações. Isso é feito através de <strong>estruturas de decisão</strong> (também
    chamadas de estruturas condicionais), do tipo "SE esta condição for verdadeira, ENTÃO faça esta
    ação, SENÃO faça outra". É exatamente assim que um aplicativo de login decide, por exemplo, se deve
    mostrar a mensagem "senha inválida" ou liberar o acesso ao sistema: ele compara a senha digitada com
    a senha correta e segue por um caminho ou por outro dependendo do resultado dessa comparação.</p>
    <p>Além das decisões simples, programas também usam <strong>repetições</strong> (fazer a mesma
    ação várias vezes, como processar cada item de uma lista de compras) para lidar com tarefas que
    envolvem múltiplos itens ou que precisam continuar até que uma condição seja satisfeita.</p>

    <h3>Como o programa acessa dados</h3>
    <p>Enquanto está rodando, um programa precisa guardar valores temporários — por exemplo, o total de
    uma compra sendo calculado, ou o nome que o usuário acabou de digitar. Esses valores ficam guardados
    em <strong>variáveis</strong>, que são, na prática, posições de memória RAM reservadas para aquele
    dado específico. Quando o programa precisa de dados que devem sobreviver depois que ele for
    fechado, ele lê e grava informações em <strong>arquivos</strong> no armazenamento permanente, ou em
    um <strong>banco de dados</strong> — um sistema especializado em guardar e organizar grandes
    quantidades de informação de forma estruturada e rápida de consultar.</p>

    <h3>Código-fonte: a linguagem que o programador escreve</h3>
    <p>O <strong>código-fonte</strong> é o texto que uma pessoa programadora efetivamente escreve, usando
    uma linguagem de programação (como Python, Java, JavaScript ou C). Essas linguagens são desenhadas
    para serem relativamente legíveis por humanos — usam palavras como "se", "enquanto" ou "para" (ou
    seus equivalentes em inglês) — mas o processador, por si só, não entende nada disso diretamente.</p>

    <h3>Compilação e interpretação: a ponte até a máquina</h3>
    <p>Para que o código-fonte realmente rode, ele precisa ser traduzido para <strong>linguagem de
    máquina</strong> — as sequências binárias que a CPU de fato executa (lembra do Módulo 2?). Essa
    tradução acontece de duas formas principais:</p>
    <ul>
      <li><strong>Compilação:</strong> um programa chamado compilador lê todo o código-fonte de uma vez
      e gera, antecipadamente, um arquivo executável já traduzido para linguagem de máquina. É o que
      acontece, por exemplo, com programas escritos em C ou C++.</li>
      <li><strong>Interpretação:</strong> um programa chamado interpretador lê e executa o código-fonte
      linha por linha, traduzindo e rodando em tempo real, sem gerar um executável separado
      antecipadamente. É o caso, por exemplo, do Python em sua forma mais comum de uso.</li>
    </ul>
    <p>Muitas linguagens modernas, na prática, usam uma combinação das duas técnicas para equilibrar
    velocidade de execução e flexibilidade — mas o conceito central é sempre o mesmo: alguém precisa
    traduzir o que o programador escreveu para o que o processador realmente entende.</p>',
    <<<'GIFT'
::Modulo3 Q1:: O que é um algoritmo? {
=Uma sequência de passos, planejada antes de virar código, para resolver um problema.
~Um erro que acontece durante a execução de um programa.
~O nome de uma linguagem de programação específica.
~A tradução do código-fonte para linguagem de máquina.
####Um algoritmo é o "plano" ou "receita" com os passos para resolver um problema, escrito de forma independente de qualquer linguagem de programação -- ele é a base sobre a qual o código-fonte depois é escrito.
}

::Modulo3 Q2:: Qual é a função da compilação (ou interpretação) de um programa? {
=Traduzir o código-fonte, escrito numa linguagem legível por humanos, para instruções que o processador consegue executar.
~Guardar o código-fonte permanentemente no armazenamento.
~Corrigir automaticamente os erros de lógica do programador.
~Criar a interface gráfica do programa.
####O processador só entende linguagem de máquina (sequências binárias). Compiladores e interpretadores existem justamente para traduzir o código-fonte, escrito numa linguagem como Python ou Java, para essas instruções que a máquina consegue de fato executar.
}
GIFT
);

// --- Módulo 4: Redes de Computadores ---
seed_criar_topico($course, 4, 'Redes de Computadores',
    '<h3>O que é uma rede</h3>
    <p>Uma <strong>rede de computadores</strong> é um conjunto de dois ou mais dispositivos conectados
    entre si, capazes de trocar dados. Essa troca não acontece de uma vez só, em um bloco enorme: as
    informações são divididas em pequenos <strong>pacotes</strong>, que viajam separadamente — por
    cabos, fibra óptica ou ondas de rádio (Wi-Fi) — e são remontados na ordem certa quando chegam ao
    destino.</p>

    <h3>Por que dividir em pacotes?</h3>
    <p>Dividir os dados em pacotes pequenos traz vantagens importantes: se um pacote se perde ou chega
    corrompido no meio do caminho, só aquele pedaço precisa ser reenviado (em vez do arquivo inteiro), e
    vários pacotes de comunicações diferentes podem compartilhar o mesmo cabo ou canal de rede,
    intercalados, sem que uma transmissão precise esperar a outra terminar completamente.</p>

    <h3>LAN x WAN</h3>
    <ul>
      <li><strong>LAN (Local Area Network — rede local):</strong> conecta dispositivos dentro de uma
      área geograficamente pequena, como uma casa, uma sala de aula ou um prédio inteiro de escritórios.
      </li>
      <li><strong>WAN (Wide Area Network — rede de longa distância):</strong> conecta redes que estão
      espalhadas por distâncias muito maiores, ligando cidades, estados ou países diferentes. A própria
      <strong>internet</strong>, que veremos em detalhe no Módulo 6, é a maior WAN que existe — uma
      rede de redes que cobre o planeta inteiro.</li>
    </ul>

    <h3>Endereços: como um dispositivo é encontrado na rede</h3>
    <p>Para que um pacote de dados chegue ao lugar certo, cada dispositivo conectado a uma rede precisa
    de um <strong>endereço IP</strong> (Internet Protocol) — um identificador único, parecido em função
    com um endereço postal. Sem um endereço, seria impossível saber para onde enviar cada pacote de
    informação.</p>

    <h3>Os equipamentos que fazem a rede funcionar</h3>
    <ul>
      <li><strong>Switch:</strong> conecta vários dispositivos dentro de uma mesma rede local,
      encaminhando cada pacote diretamente para o destino correto dentro daquela rede.</li>
      <li><strong>Roteador:</strong> conecta redes diferentes entre si — por exemplo, sua rede doméstica
      (LAN) com a rede do seu provedor de internet (parte da WAN) — decidindo o melhor caminho para
      encaminhar pacotes de uma rede para outra.</li>
    </ul>
    <p>No dia a dia, o "roteador Wi-Fi" que a maioria das casas tem combina, na prática, as duas
    funções: ele conecta os dispositivos da casa entre si (como um switch) e também os conecta à
    internet através do provedor (como um roteador).</p>',
    <<<'GIFT'
::Modulo4 Q1:: Qual a principal diferença entre uma rede LAN e uma rede WAN? {
=LAN cobre uma área pequena (como uma casa ou escritório), enquanto WAN conecta redes a longas distâncias.
~LAN só funciona com cabos, e WAN só funciona sem fio.
~WAN é sempre mais rápida que qualquer LAN.
~Não existe diferença real entre elas.
####LAN (Local Area Network) é limitada a uma área pequena, como uma residência ou escritório. WAN (Wide Area Network) conecta redes espalhadas por distâncias muito maiores -- a internet é o maior exemplo de WAN que existe.
}

::Modulo4 Q2:: Para que serve o endereço IP de um dispositivo em uma rede? {
=Identificar de forma única aquele dispositivo dentro da rede, permitindo que outros o encontrem para trocar dados.
~Definir a cor da interface do sistema operacional.
~Armazenar o conteúdo dos arquivos do dispositivo.
~Acelerar o processamento do computador.
####O endereço IP funciona como um "endereço postal" digital: é através dele que outros dispositivos da rede sabem para onde enviar os pacotes de dados destinados àquele computador específico.
}
GIFT
);

// --- Módulo 5: Dispositivos Modernos ---
seed_criar_topico($course, 5, 'Dispositivos Modernos (celular, smart TV)',
    '<h3>Também são computadores</h3>
    <p>Um celular ou uma smart TV são, por baixo dos panos, computadores completos: têm processador
    (CPU), memória RAM e armazenamento, exatamente como vimos no Módulo 1 sobre um notebook ou desktop —
    só que miniaturizados, otimizados para consumir pouca energia e desenhados para caber em um formato
    pequeno e portátil.</p>

    <h3>Sistema operacional móvel</h3>
    <p>Assim como um computador de mesa roda o Windows, o macOS ou o Linux, celulares e tablets rodam um
    sistema operacional próprio, criado especialmente para telas de toque e uso móvel — os mais comuns
    são o <strong>Android</strong> (Google) e o <strong>iOS</strong> (Apple). Smart TVs, por sua vez,
    costumam rodar variantes desses mesmos sistemas adaptadas para controle remoto e tela grande, como o
    Android TV, o Google TV, o Tizen (Samsung) ou o webOS (LG). Em todos os casos, o sistema operacional
    é responsável por gerenciar o hardware do aparelho e permitir que os aplicativos sejam instalados e
    executados.</p>

    <h3>Sensores: sentidos extras</h3>
    <p>Diferente de um computador de mesa tradicional, celulares costumam vir equipados com diversos
    <strong>sensores</strong> que ajudam o aparelho a "perceber" o ambiente e o próprio movimento:</p>
    <ul>
      <li><strong>Câmera:</strong> captura imagens e vídeos, convertendo luz em dados digitais (pixels,
      como vimos no Módulo 2).</li>
      <li><strong>GPS:</strong> calcula a localização geográfica do aparelho, usando sinais de
      satélites.</li>
      <li><strong>Giroscópio e acelerômetro:</strong> detectam movimento, inclinação e orientação do
      aparelho — é o que faz a tela girar quando você deita o celular de lado, por exemplo.</li>
      <li><strong>Sensor de luz e de proximidade:</strong> ajustam automaticamente o brilho da tela ou
      desligam a tela quando o celular está próximo ao rosto durante uma ligação.</li>
    </ul>

    <h3>Conectividade: várias formas de se comunicar</h3>
    <p>Esses dispositivos costumam suportar várias tecnologias de conexão simultaneamente:</p>
    <ul>
      <li><strong>Wi-Fi:</strong> conexão sem fio de curto/médio alcance, geralmente usada dentro de
      casa ou do escritório, conectando o aparelho a uma rede local (vista no Módulo 4) e, através dela,
      à internet.</li>
      <li><strong>Bluetooth:</strong> conexão sem fio de curtíssimo alcance, usada para conectar
      acessórios próximos, como fones de ouvido, teclados ou smartwatches.</li>
      <li><strong>Dados móveis:</strong> conexão à internet através da rede da operadora de telefonia
      (4G, 5G), disponível mesmo longe de qualquer rede Wi-Fi.</li>
    </ul>

    <h3>Aplicativos: os "programas" desses dispositivos</h3>
    <p>Os programas que rodam em celulares e smart TVs são chamados de <strong>aplicativos (apps)</strong>.
    Conceitualmente, é exatamente a mesma ideia de "programa de computador" que vimos no Módulo 3 —
    sequências de instruções, escritas em código-fonte, compiladas ou interpretadas — só que adaptadas
    para telas sensíveis ao toque, controles remotos, e para as particularidades técnicas do sistema
    operacional móvel de cada aparelho.</p>',
    <<<'GIFT'
::Modulo5 Q1:: Por que celulares e smart TVs são considerados computadores? {
=Porque possuem processador, memória RAM e armazenamento, assim como um notebook ou desktop.
~Porque só conseguem fazer ligações e assistir vídeos, nada mais.
~Porque não têm sistema operacional, apenas aplicativos.
~Porque são conectados exclusivamente por cabo de rede.
####A definição de computador não depende do tamanho ou formato: qualquer dispositivo com processador, memória e armazenamento, capaz de executar programas, é um computador -- incluindo celulares e smart TVs.
}

::Modulo5 Q2:: O que é o sistema operacional de um celular (como Android ou iOS)? {
=O software responsável por gerenciar o hardware do aparelho e permitir que os aplicativos rodem.
~Um sensor que detecta a posição do celular.
~Um tipo de rede sem fio, como o Wi-Fi.
~O nome dado à tela sensível ao toque.
####O sistema operacional é a camada de software que administra os recursos do dispositivo (processador, memória, sensores) e serve de base para que os aplicativos possam funcionar -- é o equivalente móvel do Windows ou Linux num computador tradicional.
}
GIFT
);

// --- Módulo 6: Internet ---
seed_criar_topico($course, 6, 'Internet',
    '<h3>A rede das redes</h3>
    <p>A <strong>internet</strong> é uma gigantesca rede mundial que conecta milhões de redes menores —
    de provedores, empresas, universidades, órgãos governamentais e residências — entre si. É por isso
    que ela costuma ser descrita como "a rede das redes": ela não é uma única rede centralizada, mas o
    resultado da interconexão de inúmeras redes independentes ao redor do mundo, todas se comunicando
    através de um conjunto comum de regras (protocolos).</p>

    <h3>O que acontece quando você acessa um site, passo a passo</h3>
    <ol>
      <li>Você digita um endereço (por exemplo, www.exemplo.com) na barra do navegador.</li>
      <li>Um serviço chamado <strong>DNS</strong> (Domain Name System) traduz esse nome, feito para
      humanos lembrarem, para o <strong>endereço IP</strong> numérico do servidor correspondente (visto
      no Módulo 4) — funcionando como uma agenda de contatos gigante que transforma nomes em números.</li>
      <li>O navegador envia um pedido através da rede até esse servidor, usando o protocolo
      <strong>HTTP</strong> (Hypertext Transfer Protocol) ou sua versão segura e criptografada,
      <strong>HTTPS</strong>.</li>
      <li>O pedido viaja em pacotes (Módulo 4), passando por diversos roteadores intermediários, até
      chegar fisicamente ao servidor de destino.</li>
      <li>O servidor processa o pedido e envia de volta o conteúdo da página, também em pacotes, pelo
      caminho inverso.</li>
      <li>O navegador recebe esses pacotes, remonta a página e a exibe na tela.</li>
    </ol>
    <p>Tudo isso normalmente acontece em uma fração de segundo, mesmo quando o servidor de destino está
    do outro lado do planeta.</p>

    <h3>HTTP x HTTPS</h3>
    <p>A diferença entre os dois é a segurança: no <strong>HTTP</strong>, os dados trafegam sem
    criptografia, o que significa que, em teoria, alguém no meio do caminho poderia interceptar e ler o
    conteúdo. No <strong>HTTPS</strong> ("S" de "Secure"), os dados são criptografados antes de serem
    enviados, tornando-os ilegíveis para qualquer um que os intercepte no meio do trajeto — por isso é o
    padrão recomendado hoje em dia, especialmente para sites que lidam com senhas, dados pessoais ou
    informações de pagamento.</p>

    <h3>Provedor de internet (ISP)</h3>
    <p>Para que uma residência ou empresa tenha acesso à internet, é preciso contratar um
    <strong>provedor de internet</strong> (ISP — Internet Service Provider), que fornece a conexão
    física (via cabo, fibra óptica ou antena) que liga aquele local à grande rede mundial.</p>',
    <<<'GIFT'
::Modulo6 Q1:: Qual é a função do DNS ao acessar um site pelo navegador? {
=Traduzir o nome do site digitado (como www.exemplo.com) para o endereço IP do servidor correspondente.
~Criptografar o conteúdo da página antes de exibi-la.
~Armazenar permanentemente os sites visitados no computador.
~Aumentar a velocidade da conexão com a internet.
####O DNS funciona como uma "agenda de contatos" da internet: como os computadores se localizam por números (endereços IP), e nomes como www.exemplo.com são mais fáceis para humanos lembrarem, o DNS faz essa tradução de nome para número.
}

::Modulo6 Q2:: Por que a internet é descrita como "a rede das redes"? {
=Porque ela conecta milhões de redes menores (de provedores, empresas, residências) entre si, formando uma rede mundial.
~Porque só existe uma única rede no mundo, chamada internet.
~Porque cada site tem sua própria internet particular.
~Porque a internet é o mesmo que uma rede LAN doméstica.
####A internet não é uma rede única e isolada -- ela é resultado da interconexão de inúmeras redes menores (LANs, redes de provedores, redes corporativas) espalhadas pelo mundo todo, todas se comunicando através de protocolos em comum.
}
GIFT
);

// --- Módulo 7: Servidores e Clientes ---
seed_criar_topico($course, 7, 'Servidores e Clientes',
    '<h3>O modelo cliente-servidor</h3>
    <p>Boa parte dos serviços que usamos na internet — sites, e-mail, aplicativos de mensagem, redes
    sociais — funciona seguindo o modelo <strong>cliente-servidor</strong>. Nesse modelo, o
    <strong>cliente</strong> (seu navegador, o aplicativo no celular) faz um pedido, e o
    <strong>servidor</strong> (um computador em algum data center, ligado continuamente) processa esse
    pedido e devolve uma resposta. Essa dinâmica de pedido-e-resposta é a base de praticamente toda a
    comunicação que vimos no Módulo 6 sobre a internet.</p>

    <h3>O que é, na prática, um servidor</h3>
    <p>Um <strong>servidor</strong> é, estruturalmente, um computador como qualquer outro que estudamos
    no Módulo 1 — com processador, memória e armazenamento — só que dedicado a uma função específica:
    ficar sempre ligado e disponível, atendendo pedidos de vários clientes ao mesmo tempo. Por isso,
    servidores geralmente têm hardware mais robusto e redundante do que um computador pessoal comum, de
    forma que, se um componente falhar, o serviço continue no ar.</p>

    <h3>Tipos comuns de servidor</h3>
    <ul>
      <li><strong>Servidor web:</strong> armazena e entrega as páginas de um site sempre que um
      navegador as solicita.</li>
      <li><strong>Servidor de e-mail:</strong> recebe, armazena e envia mensagens de e-mail entre
      usuários e outros servidores de e-mail.</li>
      <li><strong>Servidor de banco de dados:</strong> guarda e organiza grandes quantidades de dados de
      forma estruturada, respondendo a consultas feitas por outros sistemas (como o site que você está
      acessando agora mesmo, provavelmente).</li>
      <li><strong>Servidor de arquivos:</strong> centraliza o armazenamento de arquivos que precisam ser
      acessados por várias pessoas ou dispositivos ao mesmo tempo.</li>
    </ul>
    <p>É comum que um mesmo serviço (por exemplo, uma rede social) dependa, ao mesmo tempo, de vários
    tipos diferentes de servidor trabalhando em conjunto — o que já é uma pequena amostra do que vamos
    aprofundar no Módulo 8, sobre sistemas corporativos complexos.</p>

    <h3>Computação em nuvem (cloud)</h3>
    <p>Quando uma empresa opta por usar servidores de terceiros — como os oferecidos por Amazon (AWS),
    Google (Google Cloud) ou Microsoft (Azure) — em vez de comprar e manter os próprios servidores
    fisicamente, isso é chamado de <strong>computação em nuvem</strong>. Na prática, a empresa está
    alugando capacidade de processamento, memória e armazenamento de servidores que pertencem e são
    mantidos por outra empresa, acessados através da internet.</p>',
    <<<'GIFT'
::Modulo7 Q1:: No modelo cliente-servidor, qual é o papel do "cliente"? {
=Fazer um pedido (por exemplo, abrir um site), que será processado e respondido pelo servidor.
~Ficar ligado continuamente, esperando pedidos de outros computadores.
~Armazenar permanentemente os dados de todos os usuários do sistema.
~Ser sempre um computador mais potente do que o servidor.
####O cliente é quem inicia a comunicação, solicitando algo (uma página, um e-mail, um dado) -- é o servidor quem fica esperando esses pedidos chegarem, para processá-los e responder.
}

::Modulo7 Q2:: O que significa uma empresa usar "computação em nuvem"? {
=Usar servidores de terceiros (como Amazon, Google ou Microsoft) em vez de manter os próprios servidores.
~Guardar arquivos apenas no computador local, sem conexão com a internet.
~Não usar nenhum tipo de servidor.
~Um tipo de rede sem fio usada apenas em residências.
####Computação em nuvem significa que, em vez de comprar e manter seus próprios servidores fisicamente, uma empresa (ou pessoa) aluga capacidade de processamento e armazenamento de servidores que pertencem a outra empresa, acessados pela internet.
}
GIFT
);

// --- Módulo 8: Sistemas Complexos Corporativos ---
seed_criar_topico($course, 8, 'Sistemas Complexos Corporativos',
    '<h3>Quando um sistema não é só "um programa"</h3>
    <p>Empresas grandes não rodam com um único programa isolado, instalado em um único computador.
    Elas dependem de <strong>vários sistemas integrados</strong>, trabalhando juntos — geralmente
    distribuídos em muitos servidores diferentes (Módulo 7), possivelmente em diversos data centers ao
    redor do mundo, trocando dados entre si constantemente.</p>

    <h3>Exemplos comuns de sistemas corporativos</h3>
    <ul>
      <li><strong>ERP (Enterprise Resource Planning — Planejamento de Recursos Empresariais):</strong>
      integra áreas centrais da empresa, como financeiro, estoque, compras e recursos humanos, em um só
      sistema, garantindo que todos trabalhem com as mesmas informações atualizadas.</li>
      <li><strong>CRM (Customer Relationship Management — Gestão de Relacionamento com o Cliente):</strong>
      organiza todo o histórico de relacionamento da empresa com seus clientes — vendas realizadas,
      atendimentos prestados, preferências registradas.</li>
      <li><strong>Sistemas de e-commerce:</strong> integram catálogo de produtos, controle de estoque,
      processamento de pagamentos e logística de entrega, tudo funcionando em conjunto.</li>
    </ul>

    <h3>Os desafios de operar em grande escala</h3>
    <p>Um sistema corporativo grande frequentemente precisa lidar com milhares — às vezes milhões — de
    usuários e transações acontecendo ao mesmo tempo. Por isso, esse tipo de sistema se preocupa
    especialmente com três características:</p>
    <ul>
      <li><strong>Escalabilidade:</strong> a capacidade de continuar funcionando bem mesmo quando o
      número de usuários ou de dados cresce muito — geralmente resolvida adicionando mais servidores
      trabalhando em paralelo, em vez de depender de uma única máquina cada vez mais poderosa.</li>
      <li><strong>Disponibilidade:</strong> a capacidade de ficar no ar continuamente, mesmo que algum
      servidor específico apresente falha — normalmente alcançada usando vários servidores redundantes,
      de forma que, se um cair, outro assuma automaticamente a função, sem que os usuários sequer
      percebam.</li>
      <li><strong>Segurança:</strong> a proteção de dados sensíveis — financeiros, pessoais, estratégicos
      — contra acessos indevidos, vazamentos ou ataques, algo especialmente crítico quando o sistema lida
      com informações de milhares de clientes.</li>
    </ul>

    <h3>Um exemplo do dia a dia</h3>
    <p>Pense em uma rede de lojas fazendo uma promoção online: o site (servidor web) precisa consultar o
    estoque disponível (banco de dados) em tempo real, processar o pagamento com segurança, avisar o
    sistema de logística para separar e enviar o produto, e ainda registrar tudo isso no CRM da empresa
    para futuras campanhas de marketing — tudo isso acontecendo simultaneamente, para milhares de
    clientes diferentes, sem que o site saia do ar.</p>',
    <<<'GIFT'
::Modulo8 Q1:: O que é um sistema ERP? {
=Um sistema que integra diferentes áreas de uma empresa, como financeiro, estoque e recursos humanos, num só lugar.
~Um tipo de rede usada apenas para conectar impressoras.
~Um sistema operacional exclusivo para celulares.
~Um protocolo de segurança usado em sites de banco.
####ERP significa "Enterprise Resource Planning" (Planejamento de Recursos Empresariais). É um sistema corporativo que integra várias áreas da empresa -- financeiro, estoque, RH -- para que todas trabalhem com as mesmas informações, de forma coordenada.
}

::Modulo8 Q2:: Por que a "disponibilidade" é um desafio importante em sistemas corporativos grandes? {
=Porque o sistema precisa continuar funcionando mesmo se algum servidor falhar, geralmente usando redundância.
~Porque disponibilidade significa ter o maior número possível de cores na interface.
~Porque só sistemas pequenos precisam se preocupar com isso.
~Porque disponibilidade é o mesmo que ter um bom design gráfico.
####Sistemas corporativos grandes não podem parar de funcionar quando um único servidor falha -- por isso costumam usar vários servidores redundantes, de forma que, se um cair, outro assuma automaticamente e os usuários nem percebam a falha.
}
GIFT
);

// --- Módulo 9: Inteligência Artificial ---
seed_criar_topico($course, 9, 'Inteligência Artificial',
    '<h3>Programação tradicional x aprendizado de máquina</h3>
    <p>No Módulo 3 vimos que, num programa tradicional, o programador escreve <strong>regras explícitas</strong>:
    "SE a idade for maior ou igual a 18, ENTÃO é considerado maior de idade". Toda decisão que o programa
    pode tomar já foi prevista e escrita, passo a passo, por uma pessoa.</p>
    <p>Em <strong>machine learning</strong> (aprendizado de máquina), a abordagem é diferente: em vez de
    escrever regras à mão para cada situação possível, o sistema é alimentado com uma quantidade enorme
    de exemplos de dados e aprende, por conta própria, os padrões escondidos ali dentro. Por exemplo, em
    vez de escrever regras explícitas do tipo "se a imagem tiver bigode e orelhas pontudas, é um gato",
    o sistema analisa milhares (ou milhões) de fotos já identificadas como "gato" ou "não é gato" e
    aprende sozinho quais características, no conjunto, indicam a presença de um gato.</p>

    <h3>O que é, afinal, Inteligência Artificial</h3>
    <p><strong>Inteligência Artificial (IA)</strong> é a área da computação dedicada a criar sistemas
    capazes de realizar tarefas que, normalmente, exigiriam algum tipo de inteligência humana — como
    reconhecer o conteúdo de uma imagem, entender o significado de um texto, traduzir entre idiomas ou
    tomar decisões complexas com base em grandes volumes de dados. Machine learning, mencionado acima, é
    hoje a técnica mais usada para construir esse tipo de sistema, mas não é a única abordagem que já
    existiu na história da IA.</p>

    <h3>Exemplos de IA no dia a dia</h3>
    <ul>
      <li><strong>Assistentes virtuais:</strong> sistemas que entendem comandos de voz e respondem a
      perguntas faladas, convertendo áudio em texto (lembra da conversão de som em números, no Módulo 2?)
      e depois interpretando o significado dessas palavras.</li>
      <li><strong>Sistemas de recomendação:</strong> analisam seu histórico de uso para sugerir vídeos,
      produtos ou músicas que provavelmente vão te interessar, com base em padrões de comportamento de
      milhões de outros usuários parecidos com você.</li>
      <li><strong>Reconhecimento facial e de objetos:</strong> identificam rostos ou objetos específicos
      dentro de fotos e vídeos, usada tanto para desbloquear celulares quanto em sistemas de segurança.</li>
      <li><strong>IA generativa:</strong> uma categoria mais recente de sistemas, capazes de
      <em>gerar</em> conteúdo novo — como textos, imagens ou trechos de código — em vez de apenas
      classificar ou reconhecer algo que já existe. Chatbots que respondem perguntas em linguagem natural
      são o exemplo mais popular dessa categoria.</li>
    </ul>

    <h3>De onde vem esse "aprendizado"</h3>
    <p>Todos os exemplos acima têm algo em comum: foram <strong>treinados</strong> previamente com
    grandes quantidades de dados — textos, imagens, gravações de voz, históricos de comportamento —, e
    não foram programados manualmente regra por regra, como um programa tradicional do Módulo 3. Esse
    processo de treinamento é o que permite que esses sistemas generalizem o que aprenderam para
    situações novas, que nunca tinham visto antes exatamente daquela forma.</p>',
    <<<'GIFT'
::Modulo9 Q1:: Qual a principal diferença entre programação tradicional e machine learning (aprendizado de máquina)? {
=Na programação tradicional o programador escreve regras explícitas; no machine learning o sistema aprende padrões a partir de exemplos de dados.
~Machine learning não usa computadores, apenas processos manuais.
~Programação tradicional é sempre mais rápida que qualquer sistema de IA.
~Não existe diferença real entre as duas abordagens.
####Na programação tradicional, cada decisão do programa é definida explicitamente pelo programador através de regras (como estruturas SE/SENÃO). No machine learning, em vez de escrever as regras à mão, o sistema aprende esses padrões automaticamente a partir de muitos exemplos de dados.
}

::Modulo9 Q2:: O que caracteriza um sistema de "IA generativa"? {
=A capacidade de gerar conteúdo novo, como textos, imagens ou código, a partir de padrões aprendidos.
~A capacidade de apagar arquivos automaticamente do computador.
~Um tipo de servidor usado exclusivamente para armazenar e-mails.
~Um protocolo de rede usado para conectar impressoras.
####IA generativa se refere a sistemas de inteligência artificial (como chatbots) capazes de criar conteúdo novo -- um texto, uma imagem, um trecho de código -- com base nos padrões que aprenderam durante o treinamento, em vez de apenas classificar ou reconhecer algo já existente.
}
GIFT
);

seed_step("Reconstruir cache do curso", function () use ($course) {
    rebuild_course_cache($course->id, true);
});

// ---------------------------------------------------------------------
// 5. CRIAR (OU REAPROVEITAR) PROFESSOR E ALUNOS
// ---------------------------------------------------------------------
function seed_create_user($username, $firstname, $lastname, $email, $password) {
    global $DB;
    $existing = $DB->get_record('user', ['username' => $username, 'deleted' => 0]);
    if ($existing) {
        echo "[SKIP] Usuário '$username' já existe (id={$existing->id})\n";
        return $existing;
    }

    return seed_step("Criar usuário '$username'",
        function () use ($username, $firstname, $lastname, $email, $password) {
            global $DB;
            $user = new stdClass();
            $user->username   = $username;
            $user->password   = $password;
            $user->firstname  = $firstname;
            $user->lastname   = $lastname;
            $user->email      = $email;
            $user->auth       = 'manual';
            $user->confirmed  = 1;
            $user->mnethostid = 1;
            $user->lang       = 'pt_br';

            $userid = user_create_user($user, true, false);
            return $DB->get_record('user', ['id' => $userid]);
        }
    );
}

$teacher = seed_create_user('prof.silva', 'Roberto', 'Silva', 'roberto.silva@example.com', $SENHA_PADRAO);

$students = [
    ['aluno.ana',    'Ana',    'Ferreira', 'ana.ferreira@example.com'],
    ['aluno.bruno',  'Bruno',  'Costa',    'bruno.costa@example.com'],
    ['aluno.carla',  'Carla',  'Mendes',   'carla.mendes@example.com'],
    ['aluno.diego',  'Diego',  'Santos',   'diego.santos@example.com'],
    ['aluno.elisa',  'Elisa',  'Rocha',    'elisa.rocha@example.com'],
];

$studentusers = [];
foreach ($students as [$username, $first, $last, $email]) {
    $studentusers[] = seed_create_user($username, $first, $last, $email, $SENHA_PADRAO);
}
echo "[OK] Professor e " . count($studentusers) . " alunos prontos (criados ou já existentes)\n";

// ---------------------------------------------------------------------
// 6. MATRICULAR NO CURSO (só quem ainda não está matriculado)
// ---------------------------------------------------------------------
seed_step("Matricular professor e alunos", function () use ($course, $teacher, $studentusers) {
    global $DB;

    $enrolplugin = enrol_get_plugin('manual');
    $instances = enrol_get_instances($course->id, true);
    $manualinstance = null;
    foreach ($instances as $inst) {
        if ($inst->enrol === 'manual') {
            $manualinstance = $inst;
            break;
        }
    }
    if (!$manualinstance) {
        $instanceid = $enrolplugin->add_instance($course);
        $manualinstance = $DB->get_record('enrol', ['id' => $instanceid]);
    }

    $teacherroleid = $DB->get_field('role', 'id', ['shortname' => 'editingteacher']);
    $studentroleid = $DB->get_field('role', 'id', ['shortname' => 'student']);
    $coursecontext = context_course::instance($course->id);

    $matricular = function ($user, $roleid, $papel) use ($enrolplugin, $manualinstance, $coursecontext) {
        if (is_enrolled($coursecontext, $user->id)) {
            echo "\n   [SKIP] {$user->username} já está matriculado.";
            return;
        }
        $enrolplugin->enrol_user($manualinstance, $user->id, $roleid);
        echo "\n   [OK] {$user->username} matriculado como $papel.";
    };

    $matricular($teacher, $teacherroleid, 'professor');
    foreach ($studentusers as $student) {
        $matricular($student, $studentroleid, 'aluno');
    }
    echo "\n";
});

echo "\n===========================================================\n";
echo " Curso pronto!\n";
echo " Nome: {$course->fullname}\n";
echo " Shortname: {$course->shortname}\n";
echo " URL: {$CFG->wwwroot}/course/view.php?id={$course->id}\n";
echo " Senha padrão dos usuários criados (se novos): $SENHA_PADRAO\n";
echo " (recomendado: forçar troca de senha no primeiro login)\n";
echo "===========================================================\n";
