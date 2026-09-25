import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { Activity, Ban, Banknote, Building2, Check, ChevronDown, CircleDollarSign, CreditCard, FileUp, History, Landmark, LogOut, Package, PanelLeftClose, PanelLeftOpen, Pencil, RefreshCw, Save, ScrollText, ShoppingCart, Trash2, Truck, UserPlus, Users, Wallet, X } from 'lucide-react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const companyDefaults = {
  id: null, tipoPessoa: 'JURIDICA', razaoSocial: '', nomeFantasia: '', cpf: '', cnpj: '', rg: '', inscricaoEstadual: '',
  tipo: 'FORNECEDOR', email: '', telefone: '', celular: '', cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: ''
};
const userDefaults = { id: null, nome: '', email: '', senha: '', perfil: 'OPERADOR' };
const productDefaults = { id: null, sku: '', nome: '', unidade: 'UN', precoVenda: '', precoCusto: '', estoque: '0', estoqueMinimo: '0' };
const today = () => {
  const agora = new Date();
  return `${agora.getFullYear()}-${String(agora.getMonth() + 1).padStart(2, '0')}-${String(agora.getDate()).padStart(2, '0')}`;
};
const pedidoDefaults = tipo => ({ id: null, tipo, empresaId: '', empresaLabel: '', formaPagamentoId: '', formaPagamentoLabel: '', transportadoraId: '', transportadoraLabel: '', frete: '0', dataEmissao: today(), observacao: '', itens: [{ produtoId: '', produtoLabel: '', quantidade: '1', valorUnitario: '' }] });
const paymentDefaults = { id: null, nome: '', parcelas: '1', diasVencimento: '0', tipoDestino: 'CAIXA', contaCorrenteId: '', caixaId: '' };
const caixaDefaults = { id: null, descricao: '' };
const carrierDefaults = { id: null, nome: '', cnpj: '', telefone: '' };
const accountDefaults = { id: null, descricao: '', bancoCodigo: '', bancoNome: '', agencia: '', agenciaDigito: '', conta: '', contaDigito: '', tipo: 'CORRENTE', titular: '', pix: '' };
const tituloDefaults = { id: null, tipo: 'RECEBER', empresaId: '', descricao: '', vencimento: today(), valor: '' };
const lancamentoDefaults = { tipo: 'ENTRADA', descricao: '', valor: '' };
const money = value => Number(value || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
function parseDateTime(value) {
  if (!value) return null;
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
  const raw = String(value).trim();
  if (!raw) return null;
  const comFuso = /[zZ]|[+-]\d{2}:?\d{2}$/.test(raw);
  const data = new Date(comFuso || !raw.includes('T') ? raw : `${raw}Z`);
  return Number.isNaN(data.getTime()) ? null : data;
}
const dataHora = value => {
  const data = parseDateTime(value);
  return data ? data.toLocaleString('pt-BR') : '';
};
const nomeEmpresa = item => item?.nomeFantasia || item?.razaoSocial || 'Empresa';
const num = value => {
  const parsed = Number(String(value ?? '').replace(',', '.'));
  return Number.isFinite(parsed) ? parsed : 0;
};
function valorCampo(field, event) {
  const value = event.target.value;
  const type = event.target.type;
  if (typeof value !== 'string') return value;
  if (type === 'email' || /email/i.test(field) || field === 'pix') return value;
  if (type === 'password' || field === 'senha') return value;
  if (type === 'number' || type === 'date' || type === 'file' || type === 'hidden') return value;
  return value.toLocaleUpperCase('pt-BR');
}

const ITENS_POR_PAGINA = 5;
const ConfirmContext = createContext(async () => false);

function ConfirmProvider({ children }) {
  const [dialogo, setDialogo] = useState(null);
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const resolver = useRef(null);
  const confirmar = useCallback((mensagem, opcoes = {}) => new Promise(resolve => {
    resolver.current = resolve;
    setEmail(opcoes.emailPadrao || '');
    setSenha('');
    setDialogo({
      mensagem,
      titulo: opcoes.titulo || 'Confirmação',
      confirmarLabel: opcoes.confirmarLabel || 'Confirmar',
      cancelarLabel: opcoes.cancelarLabel || 'Cancelar',
      perigo: opcoes.perigo !== false,
      credenciais: Boolean(opcoes.credenciais)
    });
  }), []);
  const fechar = useCallback(ok => {
    resolver.current?.(ok);
    resolver.current = null;
    setDialogo(null);
    setSenha('');
  }, []);
  useEffect(() => {
    if (!dialogo) return;
    const onKey = event => { if (event.key === 'Escape') fechar(false); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [dialogo, fechar]);
  function enviar(event) {
    event.preventDefault();
    if (dialogo.credenciais) {
      if (!email.trim() || !senha) return;
      fechar({ email: email.trim(), senha });
      return;
    }
    fechar(true);
  }
  return (
    <ConfirmContext.Provider value={confirmar}>
      {children}
      {dialogo && (
        <div className="confirm-overlay" onClick={() => fechar(false)}>
          <form className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title" onClick={event => event.stopPropagation()} onSubmit={enviar}>
            <p className="eyebrow">Atenção</p>
            <h3 id="confirm-title">{dialogo.titulo}</h3>
            <p>{dialogo.mensagem}</p>
            {dialogo.credenciais && (
              <div className="confirm-creds">
                <label>Usuário (e-mail)
                  <input required type="email" className="keep-case" autoComplete="username" value={email} onChange={event => setEmail(event.target.value)} />
                </label>
                <label>Senha
                  <input required type="password" autoComplete="current-password" value={senha} onChange={event => setSenha(event.target.value)} />
                </label>
                <p className="confirm-hint">Informe um usuário cadastrado para autorizar a exclusão ou alteração.</p>
              </div>
            )}
            <div className="confirm-actions">
              <button type="button" className="cancel" onClick={() => fechar(false)}>{dialogo.cancelarLabel}</button>
              <button type="submit" className={dialogo.perigo ? 'save confirm-danger' : 'save'}>{dialogo.confirmarLabel}</button>
            </div>
          </form>
        </div>
      )}
    </ConfirmContext.Provider>
  );
}

function useConfirmar() {
  return useContext(ConfirmContext);
}

function PagedList({ items, empty = 'Nenhum registro encontrado.', className = 'company-list', resetKey, children }) {
  const [pagina, setPagina] = useState(1);
  const itens = Array.isArray(items) ? items : [];
  useEffect(() => { setPagina(1); }, [resetKey]);
  const totalPaginas = Math.max(1, Math.ceil(itens.length / ITENS_POR_PAGINA));
  const paginaAtual = Math.min(pagina, totalPaginas);
  const visiveis = itens.slice((paginaAtual - 1) * ITENS_POR_PAGINA, paginaAtual * ITENS_POR_PAGINA);
  return (
    <div className={className}>
      {visiveis.length ? children(visiveis) : <p className="empty">{empty}</p>}
      {itens.length > ITENS_POR_PAGINA && (
        <div className="list-pager">
          <button type="button" className="cancel" disabled={paginaAtual <= 1} onClick={() => setPagina(paginaAtual - 1)}>Anterior</button>
          <span>Página {paginaAtual} de {totalPaginas}</span>
          <button type="button" className="cancel" disabled={paginaAtual >= totalPaginas} onClick={() => setPagina(paginaAtual + 1)}>Próxima</button>
        </div>
      )}
    </div>
  );
}

async function request(url, { method = 'GET', headers, body } = {}) {
  const isForm = body instanceof FormData;
  const response = await fetch(url, {
    method,
    headers: isForm ? headers : { ...headers, ...(body ? { 'Content-Type': 'application/json' } : {}) },
    body: isForm ? body : body ? JSON.stringify(body) : undefined
  });
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.detail || data.message || 'Falha na operação.');
  return data;
}

function App() {
  const confirmar = useConfirmar();
  const [session, setSession] = useState(() => JSON.parse(localStorage.getItem('conciliacao-session') || 'null'));
  async function sair() {
    if (!await confirmar('Sair do sistema?', { titulo: 'Sair', confirmarLabel: 'Sair', perigo: false })) return;
    localStorage.removeItem('conciliacao-session');
    setSession(null);
  }
  if (!session) return <Auth onSuccess={setSession} />;
  return <Dashboard session={session} onLogout={sair} />;
}

function Auth({ onSuccess }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ nome: '', email: '', senha: '', razaoSocial: '', nomeFantasia: '', cnpj: '', emailEmpresa: '' });
  const [erro, setErro] = useState('');
  const [loading, setLoading] = useState(false);
  const update = field => event => setForm(value => ({ ...value, [field]: valorCampo(field, event) }));
  async function submit(event) {
    event.preventDefault(); setLoading(true); setErro('');
    const body = mode === 'login' ? { email: form.email, senha: form.senha } : { ...form, cnpj: form.cnpj.replace(/\D/g, '') };
    try {
      const data = await request(`/api/auth/${mode === 'login' ? 'login' : 'cadastro'}`, { method: 'POST', body });
      localStorage.setItem('conciliacao-session', JSON.stringify(data)); onSuccess(data);
    } catch (error) { setErro(error.message); } finally { setLoading(false); }
  }
  return (
    <main className="auth-shell">
      <section className="auth-aside">
        <div className="brand"><Landmark size={23} /><span>CONCILIA<span className="accent">.ERP</span></span></div>
        <div>
          <p className="eyebrow">ERP comercial</p>
          <h1>Vendas, estoque e<br /><em>conciliação.</em></h1>
          <p className="intro">Controle pedidos, títulos e retorno bancário em um único fluxo operacional.</p>
        </div>
      </section>
      <section className="auth-panel">
        <div className="auth-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Entrar</button>
          <button className={mode === 'cadastro' ? 'active' : ''} onClick={() => setMode('cadastro')}>Criar conta</button>
        </div>
        <p className="eyebrow">{mode === 'login' ? 'Acesso da operação' : 'Primeiro acesso'}</p>
        <h2>{mode === 'login' ? 'Bem-vindo de volta.' : 'Cadastre sua empresa.'}</h2>
        {erro && <p className="error">{erro}</p>}
        <form className="auth-form" onSubmit={submit}>
          {mode === 'cadastro' && <>
            <label>Seu nome<input required value={form.nome} onChange={update('nome')} /></label>
            <label>Razão social<input required value={form.razaoSocial} onChange={update('razaoSocial')} /></label>
            <label>Nome fantasia<input value={form.nomeFantasia} onChange={update('nomeFantasia')} /></label>
            <label>CNPJ<input required pattern="[0-9]{14}" maxLength="14" value={form.cnpj} onChange={update('cnpj')} /></label>
            <label>E-mail comercial<input required type="email" value={form.emailEmpresa} onChange={update('emailEmpresa')} /></label>
          </>}
          <label>E-mail de acesso<input required type="email" value={form.email} onChange={update('email')} /></label>
          <label>Senha<input required minLength="8" type="password" value={form.senha} onChange={update('senha')} /></label>
          <button className="save" disabled={loading}>{loading ? 'Aguarde...' : mode === 'login' ? 'Entrar no ERP' : 'Criar conta e empresa'}</button>
        </form>
      </section>
    </main>
  );
}

function Dashboard({ session, onLogout }) {
  const confirmar = useConfirmar();
  const [view, setView] = useState('inicio');
  const [dados, setDados] = useState(null);
  const [retornos, setRetornos] = useState([]);
  const [empresas, setEmpresas] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [titulos, setTitulos] = useState([]);
  const [erro, setErro] = useState('');
  const [empresa, setEmpresa] = useState(companyDefaults);
  const [usuario, setUsuario] = useState(userDefaults);
  const [produto, setProduto] = useState(productDefaults);
  const [venda, setVenda] = useState(pedidoDefaults('VENDA'));
  const [compra, setCompra] = useState(pedidoDefaults('COMPRA'));
  const [titulo, setTitulo] = useState(tituloDefaults);
  const [formasPagamento, setFormasPagamento] = useState([]);
  const [transportadoras, setTransportadoras] = useState([]);
  const [pagamento, setPagamento] = useState(paymentDefaults);
  const [transportadora, setTransportadora] = useState(carrierDefaults);
  const [contasCorrentes, setContasCorrentes] = useState([]);
  const [contaCorrente, setContaCorrente] = useState(accountDefaults);
  const [caixas, setCaixas] = useState([]);
  const [caixa, setCaixa] = useState(caixaDefaults);
  const [logs, setLogs] = useState([]);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [menuVisible, setMenuVisible] = useState(() => typeof window === 'undefined' || window.matchMedia('(min-width: 961px)').matches);
  const inputRef = useRef(null);
  const headers = { Authorization: `Bearer ${session.token}` };
  const navGroups = [
    { id: 'inicio', label: 'Início', items: [{ id: 'inicio', label: 'Visão geral' }] },
    {
      id: 'comercial', label: 'Comercial', items: [
        { id: 'vendas', label: 'Vendas' },
        { id: 'compras', label: 'Compras' },
        { id: 'estoque', label: 'Estoque' }
      ]
    },
    {
      id: 'financeiro', label: 'Financeiro', items: [
        { id: 'financeiro', label: 'Títulos' },
        { id: 'contas', label: 'Contas correntes' },
        { id: 'caixa', label: 'Caixa' },
        { id: 'pagamentos', label: 'Formas de pagamento' },
        { id: 'conciliacao', label: 'Conciliação' }
      ]
    },
    {
      id: 'cadastros', label: 'Cadastros', items: [
        { id: 'empresas', label: 'Cliente/Fornecedor' },
        { id: 'transportadoras', label: 'Transportadoras' },
        ...(session.perfil === 'ADMIN' ? [{ id: 'usuarios', label: 'Usuários' }] : [])
      ]
    },
    { id: 'sistema', label: 'Sistema', items: [{ id: 'logs', label: 'Log do sistema' }] }
  ];
  const viewLabel = navGroups.flatMap(group => group.items).find(item => item.id === view)?.label || 'Visão geral';
  function goTo(id) {
    setView(id);
    if (window.matchMedia('(max-width: 960px)').matches) setMenuVisible(false);
  }
  function toggleMenu() {
    setMenuVisible(aberto => !aberto);
  }

  async function carregar() {
    try {
      const urls = ['/api/dashboard', '/api/retornos', '/api/empresas', '/api/usuarios', '/api/produtos', '/api/pedidos', '/api/titulos', '/api/formas-pagamento', '/api/transportadoras', '/api/contas-correntes', '/api/caixas', '/api/logs'];
      const responses = await Promise.all(urls.map(url => fetch(url, { headers })));
      if (responses.some(response => response.status === 401)) return onLogout();
      if (responses.some(response => !response.ok)) throw new Error('Falha ao carregar dados.');
      const [dashboard, listaRetornos, listaEmpresas, listaUsuarios, listaProdutos, listaPedidos, listaTitulos, listaPagamentos, listaTransportadoras, listaContas, listaCaixas, listaLogs] = await Promise.all(responses.map(response => response.json()));
      setDados(dashboard);
      setRetornos(Array.isArray(listaRetornos) ? listaRetornos : []);
      setEmpresas(Array.isArray(listaEmpresas) ? listaEmpresas : []);
      setUsuarios(Array.isArray(listaUsuarios) ? listaUsuarios : []);
      setProdutos(Array.isArray(listaProdutos) ? listaProdutos : []);
      setPedidos(Array.isArray(listaPedidos) ? listaPedidos : []);
      setTitulos(Array.isArray(listaTitulos) ? listaTitulos : []);
      setFormasPagamento(Array.isArray(listaPagamentos) ? listaPagamentos : []);
      setTransportadoras(Array.isArray(listaTransportadoras) ? listaTransportadoras : []);
      setContasCorrentes(Array.isArray(listaContas) ? listaContas : []);
      setCaixas(Array.isArray(listaCaixas) ? listaCaixas : []);
      setLogs(Array.isArray(listaLogs) ? listaLogs : []);
    } catch (error) { setErro(error.message); }
  }

  useEffect(() => { carregar(); }, []);
  const update = setter => field => event => setter(value => ({ ...value, [field]: valorCampo(field, event) }));

  async function autorizar(mensagem, opcoes = {}) {
    const cred = await confirmar(mensagem, { credenciais: true, emailPadrao: session.email, ...opcoes });
    return cred && cred.email ? cred : null;
  }

  async function autorizarSe(precisa, mensagem, opcoes) {
    if (!precisa) return undefined;
    return (await autorizar(mensagem, opcoes)) || null;
  }

  async function mutate(url, options = {}) {
    setErro('');
    const extra = options.credenciais?.email ? { 'X-Confirm-Email': options.credenciais.email, 'X-Confirm-Senha': options.credenciais.senha } : {};
    await request(url, { ...options, headers: { ...headers, ...extra, ...options.headers } });
    await carregar();
  }

  async function saveCompany(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(empresa.id), 'Confirmar alteração deste cadastro?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const fisica = empresa.tipoPessoa === 'FISICA';
      const body = {
        tipoPessoa: empresa.tipoPessoa,
        razaoSocial: empresa.razaoSocial,
        nomeFantasia: fisica ? null : empresa.nomeFantasia,
        cpf: fisica ? (empresa.cpf || '').replace(/\D/g, '') : null,
        cnpj: fisica ? null : (empresa.cnpj || '').replace(/\D/g, ''),
        rg: fisica ? empresa.rg : null,
        inscricaoEstadual: fisica ? null : empresa.inscricaoEstadual,
        tipo: empresa.tipo,
        email: empresa.email,
        telefone: (empresa.telefone || '').replace(/\D/g, ''),
        celular: (empresa.celular || '').replace(/\D/g, ''),
        cep: (empresa.cep || '').replace(/\D/g, ''),
        logradouro: empresa.logradouro,
        numero: empresa.numero,
        complemento: empresa.complemento,
        bairro: empresa.bairro,
        cidade: empresa.cidade,
        uf: empresa.uf
      };
      if (empresa.id) await mutate(`/api/empresas/${empresa.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/empresas', { method: 'POST', body });
      setEmpresa(companyDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveUser(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(usuario.id), 'Confirmar alteração deste usuário?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = { nome: usuario.nome, email: usuario.email, perfil: usuario.perfil, senha: usuario.senha };
      if (!usuario.id) await mutate('/api/usuarios', { method: 'POST', body });
      else {
        if (!body.senha) delete body.senha;
        await mutate(`/api/usuarios/${usuario.id}`, { method: 'PUT', body, credenciais });
      }
      setUsuario(userDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveProduct(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(produto.id), 'Confirmar alteração deste produto?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = {
        sku: produto.sku, nome: produto.nome, unidade: produto.unidade,
        precoVenda: num(produto.precoVenda), precoCusto: num(produto.precoCusto),
        estoque: num(produto.estoque), estoqueMinimo: num(produto.estoqueMinimo)
      };
      if (produto.id) await mutate(`/api/produtos/${produto.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/produtos', { method: 'POST', body });
      setProduto(productDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function savePayment(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(pagamento.id), 'Confirmar alteração desta forma de pagamento?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = {
        nome: pagamento.nome,
        parcelas: Math.max(1, Math.trunc(num(pagamento.parcelas))),
        diasVencimento: Math.max(0, Math.trunc(num(pagamento.diasVencimento))),
        tipoDestino: pagamento.tipoDestino,
        contaCorrenteId: pagamento.tipoDestino === 'CONTA_CORRENTE' && pagamento.contaCorrenteId ? Number(pagamento.contaCorrenteId) : null,
        caixaId: pagamento.tipoDestino === 'CAIXA' && pagamento.caixaId ? Number(pagamento.caixaId) : null
      };
      if (pagamento.id) await mutate(`/api/formas-pagamento/${pagamento.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/formas-pagamento', { method: 'POST', body });
      setPagamento(paymentDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveCarrier(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(transportadora.id), 'Confirmar alteração desta transportadora?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = { nome: transportadora.nome, cnpj: (transportadora.cnpj || '').replace(/\D/g, ''), telefone: transportadora.telefone };
      if (transportadora.id) await mutate(`/api/transportadoras/${transportadora.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/transportadoras', { method: 'POST', body });
      setTransportadora(carrierDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveCaixa(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(caixa.id), 'Confirmar alteração deste caixa?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = { descricao: caixa.descricao };
      if (caixa.id) await mutate(`/api/caixas/${caixa.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/caixas', { method: 'POST', body });
      setCaixa(caixaDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function abrirCaixa(item) {
    const valor = window.prompt('Saldo inicial do caixa', '0');
    if (valor === null) return;
    const credenciais = await autorizar(`Abrir o caixa "${item.descricao}" com saldo inicial ${money(num(valor))}?`, { titulo: 'Abrir caixa', confirmarLabel: 'Abrir', perigo: false });
    if (!credenciais) return;
    try { await mutate(`/api/caixas/${item.id}/abrir`, { method: 'POST', body: { saldoInicial: num(valor) }, credenciais }); }
    catch (error) { setErro(error.message); }
  }

  async function fecharCaixa(item) {
    const sugerido = item.saldoAtual ?? item.sessaoAberta?.saldoCalculado ?? 0;
    const valor = window.prompt('Saldo contado no fechamento', String(sugerido));
    if (valor === null) return;
    const credenciais = await autorizar(`Fechar o caixa "${item.descricao}" com saldo ${money(num(valor))}?`, { titulo: 'Fechar caixa' });
    if (!credenciais) return;
    try { await mutate(`/api/caixas/${item.id}/fechar`, { method: 'POST', body: { saldoInformado: num(valor) }, credenciais }); }
    catch (error) { setErro(error.message); }
  }

  async function saveAccount(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(contaCorrente.id), 'Confirmar alteração desta conta?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = {
        descricao: contaCorrente.descricao,
        bancoCodigo: contaCorrente.bancoCodigo,
        bancoNome: contaCorrente.bancoNome,
        agencia: (contaCorrente.agencia || '').replace(/\D/g, ''),
        agenciaDigito: (contaCorrente.agenciaDigito || '').replace(/\D/g, ''),
        conta: (contaCorrente.conta || '').replace(/\D/g, ''),
        contaDigito: (contaCorrente.contaDigito || '').replace(/\D/g, ''),
        tipo: contaCorrente.tipo,
        titular: contaCorrente.titular,
        pix: contaCorrente.pix
      };
      if (contaCorrente.id) await mutate(`/api/contas-correntes/${contaCorrente.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/contas-correntes', { method: 'POST', body });
      setContaCorrente(accountDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveLancamento(event, contaId, lancamento, reset) {
    event.preventDefault();
    const tipo = lancamento.tipo === 'SAIDA' ? 'saída' : 'entrada';
    const credenciais = await autorizar(`Lançar ${tipo} de ${money(num(lancamento.valor))} no extrato?`, { titulo: 'Lançamento', confirmarLabel: 'Lançar', perigo: lancamento.tipo === 'SAIDA' });
    if (!credenciais) return;
    try {
      setSaving(true);
      await mutate(`/api/contas-correntes/${contaId}/lancamentos`, {
        method: 'POST',
        body: { tipo: lancamento.tipo, descricao: lancamento.descricao, valor: num(lancamento.valor) },
        credenciais
      });
      reset(lancamentoDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  function pedidoBody(form) {
    if (!form.empresaId) throw new Error(form.tipo === 'VENDA' ? 'Consulte e selecione o cliente.' : 'Consulte e selecione o fornecedor.');
    const itens = form.itens.filter(item => item.produtoId).map(item => ({
      produtoId: Number(item.produtoId), quantidade: Math.max(1, Math.trunc(num(item.quantidade))), valorUnitario: num(item.valorUnitario)
    }));
    if (!itens.length) throw new Error('Consulte e selecione ao menos um produto.');
    if (!form.formaPagamentoId) throw new Error('Selecione a forma de pagamento.');
    return {
      tipo: form.tipo,
      empresaId: Number(form.empresaId),
      formaPagamentoId: Number(form.formaPagamentoId),
      transportadoraId: form.transportadoraId ? Number(form.transportadoraId) : null,
      frete: num(form.frete),
      dataEmissao: form.dataEmissao,
      observacao: form.observacao,
      itens
    };
  }

  async function savePedido(event, form, reset) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(form.id), 'Confirmar alteração deste pedido?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = pedidoBody(form);
      if (form.id) await mutate(`/api/pedidos/${form.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/pedidos', { method: 'POST', body });
      reset(pedidoDefaults(form.tipo));
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function saveTitulo(event) {
    event.preventDefault();
    const credenciais = await autorizarSe(Boolean(titulo.id), 'Confirmar alteração deste título?', { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (credenciais === null) return;
    try {
      setSaving(true);
      const body = { tipo: titulo.tipo, empresaId: Number(titulo.empresaId), descricao: titulo.descricao, vencimento: titulo.vencimento, valor: num(titulo.valor) };
      if (titulo.id) await mutate(`/api/titulos/${titulo.id}`, { method: 'PUT', body, credenciais });
      else await mutate('/api/titulos', { method: 'POST', body });
      setTitulo(tituloDefaults);
    } catch (error) { setErro(error.message); } finally { setSaving(false); }
  }

  async function toggle(url, ativo) {
    const credenciais = await autorizar(ativo ? 'Ativar este registro?' : 'Desativar este registro?', { confirmarLabel: ativo ? 'Ativar' : 'Desativar', perigo: !ativo });
    if (!credenciais) return;
    try { await mutate(url, { method: 'PATCH', body: { ativo }, credenciais }); }
    catch (error) { setErro(error.message); }
  }

  async function remove(url, mensagem) {
    const credenciais = await autorizar(mensagem, { titulo: 'Excluir', confirmarLabel: 'Excluir' });
    if (!credenciais) return;
    try { await mutate(url, { method: 'DELETE', credenciais }); }
    catch (error) { setErro(error.message); }
  }

  async function postAction(url, mensagem) {
    const credenciais = mensagem ? await autorizar(mensagem) : null;
    if (mensagem && !credenciais) return false;
    try { await mutate(url, { method: 'POST', credenciais }); return true; }
    catch (error) { setErro(error.message); return false; }
  }

  async function upload(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!await confirmar(`Importar o retorno "${file.name}"?`, { titulo: 'Importar retorno', confirmarLabel: 'Importar', perigo: false })) {
      event.target.value = '';
      return;
    }
    try {
      setUploading(true); setErro('');
      const form = new FormData();
      form.append('arquivo', file);
      await request('/api/retornos', { method: 'POST', headers, body: form });
      await carregar();
    } catch (error) { setErro(error.message); } finally { setUploading(false); event.target.value = ''; }
  }

  async function editarRetorno(item) {
    const nome = window.prompt('Nome do arquivo', item.nomeArquivo);
    if (!nome || nome.trim() === item.nomeArquivo) return;
    const credenciais = await autorizar(`Alterar o nome do retorno para "${nome.trim()}"?`, { titulo: 'Alterar', confirmarLabel: 'Salvar', perigo: false });
    if (!credenciais) return;
    try { await mutate(`/api/retornos/${item.id}`, { method: 'PUT', body: { nomeArquivo: nome.trim().toLocaleUpperCase('pt-BR') }, credenciais }); }
    catch (error) { setErro(error.message); }
  }

  function editarPedido(item, setter) {
    setter({
      id: item.id, tipo: item.tipo, empresaId: String(item.empresa?.id || ''),
      empresaLabel: nomeEmpresa(item.empresa),
      formaPagamentoId: String(item.formaPagamento?.id || ''),
      formaPagamentoLabel: item.formaPagamento?.nome || '',
      transportadoraId: String(item.transportadora?.id || ''),
      transportadoraLabel: item.transportadora?.nome || '',
      frete: String(item.frete ?? 0),
      dataEmissao: item.dataEmissao, observacao: item.observacao || '',
      itens: (item.itens || []).map(linha => ({
        produtoId: String(linha.produto?.id || ''),
        produtoLabel: linha.produto ? `${linha.produto.sku} · ${linha.produto.nome}` : '',
        quantidade: String(Math.max(1, Math.trunc(Number(linha.quantidade)) || 1)),
        valorUnitario: String(linha.valorUnitario)
      }))
    });
  }

  const pedidoProps = tipo => ({
    empresas: empresas.filter(item => item.ativo !== false && (item.tipo === 'AMBOS' || item.tipo === (tipo === 'VENDA' ? 'CLIENTE' : 'FORNECEDOR'))),
    produtos: produtos.filter(item => item.ativo !== false),
    formasPagamento: formasPagamento.filter(item => item.ativo !== false),
    transportadoras: transportadoras.filter(item => item.ativo !== false),
    pedidos: pedidos.filter(item => item.tipo === tipo),
    saving,
    onEdit: item => tipo === 'VENDA' ? editarPedido(item, setVenda) : editarPedido(item, setCompra),
    onToggle: item => toggle(`/api/pedidos/${item.id}/ativo`, !item.ativo),
    onDelete: item => remove(`/api/pedidos/${item.id}`, `Excluir o pedido ${item.numero}?`),
    onFaturar: async item => {
      const ok = await postAction(`/api/pedidos/${item.id}/faturar`, `Faturar ${item.numero} e gerar título financeiro?`);
      if (ok) {
        if (tipo === 'VENDA') setVenda(atual => atual.id === item.id ? pedidoDefaults('VENDA') : atual);
        else setCompra(atual => atual.id === item.id ? pedidoDefaults('COMPRA') : atual);
      }
    },
    onCancelarPedido: item => postAction(`/api/pedidos/${item.id}/cancelar`, `Cancelar o pedido ${item.numero}?`)
  });

  return (
    <main className={`shell app-layout${menuVisible ? '' : ' menu-hidden'}`}>
      <SideNav groups={navGroups} view={view} onGo={goTo} session={session} onLogout={onLogout} open={menuVisible} />
      <div className="workspace">
      <header className="workspace-top">
        <button type="button" className="menu-toggle icon-button" title={menuVisible ? 'Esconder menu' : 'Mostrar menu'} onClick={toggleMenu}>
          {menuVisible ? <PanelLeftClose size={18} /> : <PanelLeftOpen size={18} />}
        </button>
        <p className="eyebrow workspace-title">{viewLabel}</p>
        <div className="header-user">
          <span>{session.nome}</span>
          <button className="icon-button" title="Sair" onClick={onLogout}><LogOut size={17} /></button>
        </div>
      </header>
      {erro && <p className="error">{erro}</p>}
      {view === 'inicio' && (
        <Overview dados={dados} pedidos={pedidos} retornos={retornos} onGo={goTo} inputRef={inputRef} upload={upload} uploading={uploading} reload={carregar} />
      )}
      {view === 'vendas' && (
        <PedidoScreen {...pedidoProps('VENDA')} form={venda} setForm={setVenda} submit={event => savePedido(event, venda, setVenda)} onCancel={() => setVenda(pedidoDefaults('VENDA'))} />
      )}
      {view === 'compras' && (
        <PedidoScreen {...pedidoProps('COMPRA')} form={compra} setForm={setCompra} submit={event => savePedido(event, compra, setCompra)} onCancel={() => setCompra(pedidoDefaults('COMPRA'))} />
      )}
      {view === 'estoque' && (
        <ProductScreen produtos={produtos} form={produto} update={update(setProduto)} submit={saveProduct} saving={saving}
          onCancel={() => setProduto(productDefaults)}
          onEdit={item => setProduto({ ...productDefaults, ...item, precoVenda: item.precoVenda, precoCusto: item.precoCusto, estoque: item.estoque, estoqueMinimo: item.estoqueMinimo })}
          onToggle={item => toggle(`/api/produtos/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/produtos/${item.id}`, `Excluir o produto ${item.sku}?`)} />
      )}
      {view === 'financeiro' && (
        <FinanceScreen titulos={titulos} empresas={empresas.filter(item => item.ativo !== false)} form={titulo} update={update(setTitulo)}
          submit={saveTitulo} saving={saving} onCancel={() => setTitulo(tituloDefaults)}
          onEdit={item => setTitulo({ id: item.id, tipo: item.tipo, empresaId: String(item.empresa?.id || ''), descricao: item.descricao, vencimento: item.vencimento, valor: item.valor })}
          onToggle={item => toggle(`/api/titulos/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/titulos/${item.id}`, `Excluir o título "${item.descricao}"?`)}
          onBaixar={item => postAction(`/api/titulos/${item.id}/baixar`, `Baixar ${item.descricao} como liquidado?`)} />
      )}
      {view === 'caixa' && (
        <CaixaScreen lista={caixas} form={caixa} update={update(setCaixa)} submit={saveCaixa} saving={saving}
          onCancel={() => setCaixa(caixaDefaults)}
          onEdit={item => setCaixa({ id: item.id, descricao: item.descricao })}
          onToggle={item => toggle(`/api/caixas/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/caixas/${item.id}`, `Excluir o caixa "${item.descricao}"?`)}
          onAbrir={abrirCaixa} onFechar={fecharCaixa} />
      )}
      {view === 'contas' && (
        <AccountScreen lista={contasCorrentes} form={contaCorrente} update={update(setContaCorrente)} submit={saveAccount} saving={saving}
          onCancel={() => setContaCorrente(accountDefaults)}
          onEdit={item => setContaCorrente({
            id: item.id, descricao: item.descricao, bancoCodigo: item.bancoCodigo || '', bancoNome: item.bancoNome || '',
            agencia: item.agencia || '', agenciaDigito: item.agenciaDigito || '', conta: item.conta || '', contaDigito: item.contaDigito || '',
            tipo: item.tipo || 'CORRENTE', titular: item.titular || '', pix: item.pix || ''
          })}
          onToggle={item => toggle(`/api/contas-correntes/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/contas-correntes/${item.id}`, `Excluir a conta "${item.descricao}"?`)}
          onLancar={saveLancamento} />
      )}
      {view === 'conciliacao' && (
        <ConciliacaoScreen dados={dados} retornos={retornos} inputRef={inputRef} upload={upload} uploading={uploading} reload={carregar}
          onEdit={editarRetorno}
          onToggle={item => toggle(`/api/retornos/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/retornos/${item.id}`, `Excluir o retorno "${item.nomeArquivo}"?`)} />
      )}
      {view === 'pagamentos' && (
        <PaymentScreen formas={formasPagamento} form={pagamento} update={update(setPagamento)} submit={savePayment} saving={saving}
          contas={contasCorrentes.filter(item => item.ativo !== false)} caixas={caixas.filter(item => item.ativo !== false)}
          onCancel={() => setPagamento(paymentDefaults)}
          onEdit={item => setPagamento({
            id: item.id, nome: item.nome, parcelas: String(item.parcelas), diasVencimento: String(item.diasVencimento),
            tipoDestino: item.tipoDestino || 'CAIXA',
            contaCorrenteId: item.contaCorrente?.id ? String(item.contaCorrente.id) : '',
            caixaId: item.caixa?.id ? String(item.caixa.id) : ''
          })}
          onToggle={item => toggle(`/api/formas-pagamento/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/formas-pagamento/${item.id}`, `Excluir a forma "${item.nome}"?`)} />
      )}
      {view === 'transportadoras' && (
        <CarrierScreen lista={transportadoras} form={transportadora} update={update(setTransportadora)} submit={saveCarrier} saving={saving}
          onCancel={() => setTransportadora(carrierDefaults)}
          onEdit={item => setTransportadora({ id: item.id, nome: item.nome, cnpj: item.cnpj || '', telefone: item.telefone || '' })}
          onToggle={item => toggle(`/api/transportadoras/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/transportadoras/${item.id}`, `Excluir a transportadora "${item.nome}"?`)} />
      )}
      {view === 'empresas' && (
        <CompanyScreen empresas={empresas} form={empresa} setForm={setEmpresa} update={update(setEmpresa)} submit={saveCompany} saving={saving}
          onCancel={() => setEmpresa(companyDefaults)} onEdit={item => setEmpresa({
            ...companyDefaults, ...item, cpf: item.cpf || '', cnpj: item.cnpj || '',
            telefone: (item.telefone || '').replace(/\D/g, ''), celular: (item.celular || '').replace(/\D/g, '')
          })}
          onToggle={item => toggle(`/api/empresas/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/empresas/${item.id}`, `Excluir a empresa "${nomeEmpresa(item)}"?`)} />
      )}
      {view === 'usuarios' && (
        <UserScreen usuarios={usuarios} form={usuario} update={update(setUsuario)} submit={saveUser} saving={saving} currentId={session.usuarioId}
          onCancel={() => setUsuario(userDefaults)}
          onEdit={item => setUsuario({ id: item.id, nome: item.nome, email: item.email, senha: '', perfil: item.perfil })}
          onToggle={item => toggle(`/api/usuarios/${item.id}/ativo`, !item.ativo)}
          onDelete={item => remove(`/api/usuarios/${item.id}`, `Excluir o usuário "${item.nome}"?`)} />
      )}
      {view === 'logs' && <LogScreen logs={logs} />}
      </div>
      {menuVisible && <button type="button" className="sidebar-backdrop" aria-label="Fechar menu" onClick={() => setMenuVisible(false)} />}
    </main>
  );
}

function SideNav({ groups, view, onGo, session, onLogout, open }) {
  const [abertoId, setAbertoId] = useState(null);
  function toggle(group) {
    setAbertoId(atual => atual === group.id ? null : group.id);
  }
  return (
    <aside className={`sidebar${open ? ' open' : ''}`}>
      <div className="brand"><Landmark size={23} /><span>CONCILIA<span className="accent">.ERP</span></span></div>
      <nav className="side-nav">
        {groups.map(group => {
          const openGroup = abertoId === group.id;
          const atual = group.items.some(item => item.id === view);
          return (
            <div className={`nav-group${openGroup ? ' open' : ''}${atual ? ' current' : ''}`} key={group.id}>
              <button type="button" className="nav-group-toggle" onClick={() => toggle(group)}>
                <span>{group.label}</span>
                <ChevronDown size={15} className={openGroup ? 'chevron open' : 'chevron'} />
              </button>
              {openGroup && (
                <div className="nav-sub">
                  {group.items.map(item => (
                    <button key={item.id} type="button" className={view === item.id ? 'active' : ''} onClick={() => onGo(item.id)}>{item.label}</button>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </nav>
      <div className="sidebar-user">
        <span>{session.nome}</span>
        <button className="icon-button" title="Sair" onClick={onLogout}><LogOut size={17} /></button>
      </div>
    </aside>
  );
}

function SwitchAtivo({ active, onToggle, disabled }) {
  return (
    <button type="button" className={`switch${active ? ' on' : ''}`} role="switch" aria-checked={active}
      title={active ? 'Desativar' : 'Ativar'} disabled={disabled} onClick={onToggle}>
      <span className="switch-track"><span className="switch-thumb" /></span>
      <span className="switch-label">{active ? 'Ativo' : 'Inativo'}</span>
    </button>
  );
}

function CampoSituacao({ item, onToggle, disabled }) {
  const active = item ? item.ativo !== false : true;
  return (
    <label className="switch-field">Situação
      <SwitchAtivo active={active} disabled={disabled || !item} onToggle={item && onToggle ? () => onToggle(item) : undefined} />
    </label>
  );
}

function RowActions({ active, onEdit, onToggle, onDelete, locked, disableToggle, extras }) {
  return (
    <div className="row-actions">
      {extras}
      <SwitchAtivo active={active} disabled={disableToggle} onToggle={onToggle} />
      {onEdit && <button type="button" className="icon-button" title="Editar" disabled={locked} onClick={onEdit}><Pencil size={15} /></button>}
      <button type="button" className="icon-button danger" title="Excluir" disabled={locked} onClick={onDelete}><Trash2 size={15} /></button>
    </div>
  );
}

function Overview({ dados, pedidos, retornos, onGo, inputRef, upload, uploading, reload }) {
  const recentes = pedidos.filter(item => item.ativo !== false).slice().reverse();
  const arquivos = retornos.slice().reverse();
  return (
    <>
      <section className="hero">
        <div>
          <p className="eyebrow">ERP comercial / Visão geral</p>
          <h1>Operação que vende,<br />estoque e <em>fecha o banco.</em></h1>
          <p className="intro">Pedidos, contas a pagar e receber e conciliação CNAB no mesmo painel.</p>
        </div>
        <div className="hero-actions">
          <button className="upload" onClick={() => onGo('vendas')}><ShoppingCart size={18} />Nova venda</button>
          <input ref={inputRef} hidden type="file" accept=".ret,.txt" onChange={upload} />
          <button className="ghost" onClick={() => inputRef.current?.click()} disabled={uploading}><FileUp size={18} />{uploading ? 'Importando...' : 'Importar retorno'}</button>
        </div>
      </section>
      <section className="metrics">
        <Metric label="Faturamento do mês" value={money(dados?.faturamentoMes)} note="vendas faturadas" positive />
        <Metric label="Contas a receber" value={money(dados?.aReceber)} note="títulos em aberto" />
        <Metric label="Contas a pagar" value={money(dados?.aPagar)} note="títulos em aberto" />
        <Metric label="Estoque crítico" value={dados?.estoqueBaixo ?? 0} note="produtos no mínimo" />
        <Metric label="Retornos CNAB" value={dados?.totalArquivos ?? 0} note="arquivos ativos" />
        <Metric label="Conciliados" value={`${dados?.processados ?? 0}`} note={`${dados?.registros ?? 0} registros`} positive />
      </section>
      <section className="dash-grid">
        <div className="panel">
          <div className="panel-title"><div><p className="eyebrow">Comercial</p><h2>Pedidos recentes</h2></div><button className="icon-button" title="Atualizar" onClick={reload}><RefreshCw size={17} /></button></div>
          <PagedList items={recentes} empty="Nenhum pedido lançado." className="dash-list">
            {visiveis => visiveis.map(item => (
              <div className="activity" key={item.id}>
                <ShoppingCart size={20} />
                <div><strong>{item.numero} · {nomeEmpresa(item.empresa)}</strong><span>{item.tipo} · {money(item.total)}</span></div>
                <b className="status">{item.status}</b>
              </div>
            ))}
          </PagedList>
        </div>
        <div className="panel">
          <div className="panel-title"><div><p className="eyebrow">Tesouraria</p><h2>Conciliação recente</h2></div><button className="ghost-link" onClick={() => onGo('conciliacao')}>Ver módulo</button></div>
          <PagedList items={arquivos} empty="Nenhum retorno importado." className="dash-list">
            {visiveis => visiveis.map(item => (
              <div className={`activity${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
                <Activity size={20} />
                <div><strong>{item.nomeArquivo}</strong><span>{item.registros} registros</span></div>
                <b className="status">{item.status}</b>
              </div>
            ))}
          </PagedList>
        </div>
      </section>
    </>
  );
}

function coincideCadastro(item, termo) {
  const texto = (termo || '').toLocaleUpperCase('pt-BR').trim();
  const digitos = texto.replace(/\D/g, '');
  const nome = `${item.razaoSocial || ''} ${item.nomeFantasia || ''} ${item.label || ''}`.toLocaleUpperCase('pt-BR');
  const docs = `${item.cpf || ''} ${item.cnpj || ''} ${item.sub || ''}`.replace(/\D/g, '');
  if (!texto) return true;
  if (nome.includes(texto) || (item.sub || '').toLocaleUpperCase('pt-BR').includes(texto)) return true;
  return Boolean(digitos && docs.includes(digitos));
}

function SearchLookup({ label, query, selectedId, options, placeholder, onQuery, onSelect, required = true, pageSize = ITENS_POR_PAGINA }) {
  const [aberto, setAberto] = useState(false);
  const [pagina, setPagina] = useState(1);
  const filtrados = options.filter(item => coincideCadastro(item, query));
  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / pageSize));
  const paginaAtual = Math.min(pagina, totalPaginas);
  const lista = filtrados.slice((paginaAtual - 1) * pageSize, paginaAtual * pageSize);
  return (
    <label className="lookup">
      {label}
      <input
        value={query}
        placeholder={placeholder}
        autoComplete="off"
        onChange={event => { onQuery(valorCampo('busca', event)); setPagina(1); setAberto(true); }}
        onFocus={() => setAberto(true)}
        onBlur={() => setTimeout(() => setAberto(false), 180)}
      />
      <input tabIndex={-1} className="lookup-required" value={selectedId} required={required} onChange={() => {}} />
      {aberto && (
        <div className="lookup-list">
          {lista.length ? lista.map(item => (
            <button type="button" key={item.id} onMouseDown={event => { event.preventDefault(); onSelect(item); setAberto(false); }}>
              <strong>{item.label}</strong>
              {item.sub && <span>{item.sub}</span>}
            </button>
          )) : <p className="empty">Nenhum resultado. Digite para consultar.</p>}
          {filtrados.length > pageSize && (
            <div className="lookup-pager">
              <button type="button" disabled={paginaAtual <= 1} onMouseDown={event => { event.preventDefault(); setPagina(atual => Math.max(1, atual - 1)); }}>Anterior</button>
              <span>{paginaAtual} / {totalPaginas}</span>
              <button type="button" disabled={paginaAtual >= totalPaginas} onMouseDown={event => { event.preventDefault(); setPagina(atual => Math.min(totalPaginas, atual + 1)); }}>Próxima</button>
            </div>
          )}
        </div>
      )}
    </label>
  );
}

function PedidoScreen({ form, setForm, empresas, produtos, formasPagamento, transportadoras, pedidos, submit, saving, onCancel, onEdit, onToggle, onDelete, onFaturar, onCancelarPedido }) {
  const venda = form.tipo === 'VENDA';
  const editing = Boolean(form.id);
  function setItem(index, field, value) {
    setForm(atual => ({ ...atual, itens: atual.itens.map((item, i) => i === index ? { ...item, [field]: value } : item) }));
  }
  function setQuantidade(index, value) {
    if (value === '') { setItem(index, 'quantidade', ''); return; }
    setItem(index, 'quantidade', String(Math.max(1, Math.trunc(Number(value) || 1))));
  }
  function escolherProduto(index, produto) {
    const preco = venda ? produto?.precoVenda : produto?.precoCusto;
    setForm(atual => ({
      ...atual,
      itens: atual.itens.map((item, i) => i === index ? {
        ...item,
        produtoId: String(produto.id),
        produtoLabel: `${produto.sku} · ${produto.nome}`,
        valorUnitario: preco ?? item.valorUnitario
      } : item)
    }));
  }
  const total = form.itens.reduce((sum, item) => sum + num(item.quantidade) * num(item.valorUnitario), 0) + num(form.frete);
  const clientes = empresas.map(item => ({
    id: item.id,
    label: nomeEmpresa(item).toLocaleUpperCase('pt-BR'),
    sub: item.tipoPessoa === 'FISICA' ? (item.cpf || '') : (item.cnpj || ''),
    cpf: item.cpf,
    cnpj: item.cnpj,
    razaoSocial: item.razaoSocial,
    nomeFantasia: item.nomeFantasia,
    raw: item
  }));
  const catalogo = produtos.map(item => ({ id: item.id, label: `${item.sku} · ${item.nome}`.toLocaleUpperCase('pt-BR'), sub: `ESTOQUE ${item.estoque} ${item.unidade}`, raw: item }));
  const fretes = (transportadoras || []).map(item => ({ id: item.id, label: item.nome, sub: item.cnpj || 'SEM CNPJ', raw: item }));
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div>
          <p className="eyebrow">{venda ? 'Comercial' : 'Suprimentos'}</p>
          <h2>{venda ? 'Pedidos de venda' : 'Pedidos de compra'}</h2>
        </div>
        {venda ? <ShoppingCart size={26} /> : <Package size={26} />}
      </div>
      <form className="company-form" onSubmit={submit}>
        <SearchLookup
          label={venda ? 'Cliente' : 'Fornecedor'}
          query={form.empresaLabel || ''}
          selectedId={form.empresaId}
          placeholder={venda ? 'Consultar por nome, CNPJ ou CPF' : 'Consultar por nome, CNPJ ou CPF'}
          options={clientes}
          onQuery={texto => setForm(value => ({ ...value, empresaLabel: texto, empresaId: '' }))}
          onSelect={item => setForm(value => ({ ...value, empresaId: String(item.id), empresaLabel: item.label }))}
        />
        <label>Forma de pagamento
          <select required value={form.formaPagamentoId} onChange={event => {
            const id = event.target.value;
            const escolhida = (formasPagamento || []).find(item => String(item.id) === id);
            setForm(value => ({ ...value, formaPagamentoId: id, formaPagamentoLabel: escolhida?.nome || '' }));
          }}>
            <option value="">Selecione</option>
            {(formasPagamento || []).map(item => (
              <option key={item.id} value={item.id}>{item.nome} · {item.tipoDestino === 'CONTA_CORRENTE' ? 'BANCO' : 'CAIXA'} {destinoForma(item)}</option>
            ))}
          </select>
        </label>
        <SearchLookup
          label="Transportadora"
          query={form.transportadoraLabel || ''}
          selectedId={form.transportadoraId}
          placeholder="Digite para consultar a transportadora"
          required={false}
          options={fretes}
          onQuery={texto => setForm(value => ({ ...value, transportadoraLabel: texto, transportadoraId: '' }))}
          onSelect={item => setForm(value => ({ ...value, transportadoraId: String(item.id), transportadoraLabel: item.label }))}
        />
        <label>Frete<input type="number" min="0" step="0.01" value={form.frete} onChange={event => setForm(value => ({ ...value, frete: event.target.value }))} /></label>
        <label>Emissão<input required type="date" value={form.dataEmissao} onChange={event => setForm(value => ({ ...value, dataEmissao: event.target.value }))} /></label>
        <label>Observação<input value={form.observacao} onChange={event => setForm(value => ({ ...value, observacao: valorCampo('observacao', event) }))} /></label>
        <label>Total<strong className="total-preview">{money(total)}</strong></label>
        <div className="item-builder">
          {form.itens.map((item, index) => (
            <div className="item-row" key={index}>
              <SearchLookup
                label="Produto"
                query={item.produtoLabel || ''}
                selectedId={item.produtoId}
                placeholder="Digite para consultar o produto"
                options={catalogo}
                onQuery={texto => setForm(atual => ({ ...atual, itens: atual.itens.map((linha, i) => i === index ? { ...linha, produtoLabel: texto, produtoId: '' } : linha) }))}
                onSelect={opcao => escolherProduto(index, opcao.raw)}
              />
              <label>Qtd<input required type="number" min="1" step="1" value={item.quantidade} onChange={event => setQuantidade(index, event.target.value)} /></label>
              <label>Unitário<input required type="number" min="0" step="0.01" value={item.valorUnitario} onChange={event => setItem(index, 'valorUnitario', event.target.value)} /></label>
              <button type="button" className="icon-button danger" title="Remover item" disabled={form.itens.length === 1} onClick={() => setForm(value => ({ ...value, itens: value.itens.filter((_, i) => i !== index) }))}><Trash2 size={15} /></button>
            </div>
          ))}
          <button type="button" className="ghost" onClick={() => setForm(value => ({ ...value, itens: [...value.itens, { produtoId: '', produtoLabel: '', quantidade: '1', valorUnitario: '' }] }))}>Adicionar item</button>
        </div>
        <div className="form-actions">
          <CampoSituacao item={pedidos.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar pedido' : 'Abrir pedido'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={pedidos} empty={venda ? 'Nenhum pedido de venda.' : 'Nenhum pedido de compra.'}>
        {visiveis => visiveis.map(item => {
          const aberto = item.status === 'ABERTO';
          return (
            <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
              {venda ? <ShoppingCart size={18} /> : <Package size={18} />}
              <div>
                <strong>{item.numero} · {nomeEmpresa(item.empresa)}</strong>
                <span>{money(item.total)} · frete {money(item.frete)} · {item.formaPagamento?.nome || 'SEM PAGAMENTO'}{item.formaPagamento ? ` · ${item.formaPagamento.tipoDestino === 'CONTA_CORRENTE' ? 'BANCO' : 'CAIXA'} ${destinoForma(item.formaPagamento)}` : ''} · {item.transportadora?.nome || 'SEM FRETE'} · {item.status}{item.ativo === false ? ' · inativo' : ''}</span>
              </div>
              <RowActions
                active={item.ativo !== false}
                locked={!aberto}
                onEdit={aberto ? () => onEdit(item) : undefined}
                onToggle={() => onToggle(item)}
                onDelete={() => onDelete(item)}
                extras={<>
                  {aberto && <button type="button" className="icon-button" title="Faturar" onClick={() => onFaturar(item)}><Check size={15} /></button>}
                  {item.status !== 'CANCELADO' && <button type="button" className="icon-button" title="Cancelar pedido" onClick={() => onCancelarPedido(item)}><Ban size={15} /></button>}
                </>}
              />
            </div>
          );
        })}
      </PagedList>
    </section>
  );
}

function ProductScreen({ produtos, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div>
          <p className="eyebrow">Estoque e catálogo</p>
          <h2>Produtos e saldo</h2>
        </div>
        <Package size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>SKU<input required value={form.sku} onChange={update('sku')} /></label>
        <label>Nome<input required value={form.nome} onChange={update('nome')} /></label>
        <label>Unidade<select value={form.unidade} onChange={update('unidade')}><option>UN</option><option>KG</option><option>CX</option><option>LT</option></select></label>
        <label>Preço de venda<input required type="number" min="0" step="0.01" value={form.precoVenda} onChange={update('precoVenda')} /></label>
        <label>Preço de custo<input required type="number" min="0" step="0.01" value={form.precoCusto} onChange={update('precoCusto')} /></label>
        <label>Estoque<input required type="number" step="0.001" value={form.estoque} onChange={update('estoque')} /></label>
        <label>Estoque mínimo<input required type="number" min="0" step="0.001" value={form.estoqueMinimo} onChange={update('estoqueMinimo')} /></label>
        <div className="form-actions">
          <CampoSituacao item={produtos.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar produto' : 'Cadastrar produto'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={produtos} empty="Nenhum produto cadastrado.">
        {visiveis => visiveis.map(item => (
          <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
            <Package size={18} />
            <div>
              <strong>{item.sku} · {item.nome}</strong>
              <span>{item.unidade} · venda {money(item.precoVenda)} · saldo {item.estoque}{item.estoqueBaixo ? ' · crítico' : ''}{item.ativo === false ? ' · inativo' : ''}</span>
            </div>
            <RowActions active={item.ativo !== false} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
          </div>
        ))}
      </PagedList>
    </section>
  );
}

function FinanceScreen({ titulos, empresas, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete, onBaixar }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div>
          <p className="eyebrow">Financeiro</p>
          <h2>Contas a pagar e receber</h2>
        </div>
        <Wallet size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>Tipo<select value={form.tipo} onChange={update('tipo')}><option value="RECEBER">A receber</option><option value="PAGAR">A pagar</option></select></label>
        <label>Empresa
          <select required value={form.empresaId} onChange={update('empresaId')}>
            <option value="">Selecione</option>
            {empresas.map(item => <option key={item.id} value={item.id}>{nomeEmpresa(item)}</option>)}
          </select>
        </label>
        <label>Descrição<input required value={form.descricao} onChange={update('descricao')} /></label>
        <label>Vencimento<input required type="date" value={form.vencimento} onChange={update('vencimento')} /></label>
        <label>Valor<input required type="number" min="0.01" step="0.01" value={form.valor} onChange={update('valor')} /></label>
        <div className="form-actions">
          <CampoSituacao item={titulos.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><CircleDollarSign size={17} />{saving ? 'Salvando...' : editing ? 'Salvar título' : 'Lançar título'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={titulos} empty="Nenhum título lançado.">
        {visiveis => visiveis.map(item => {
          const aberto = item.status === 'ABERTO';
          return (
            <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
              <Wallet size={18} />
              <div>
                <strong>{item.tipo === 'RECEBER' ? 'Receber' : 'Pagar'} · {item.descricao}</strong>
                <span>{nomeEmpresa(item.empresa)} · {money(item.saldo)} de {money(item.valor)} · {item.vencimento} · {item.status}{item.destinoNome ? ` · ${item.destinoTipo === 'BANCO' ? 'BANCO' : 'CAIXA'} ${item.destinoNome}` : ''}{item.pedidoNumero ? ` · ${item.pedidoNumero}` : ''}</span>
              </div>
              <RowActions
                active={item.ativo !== false}
                locked={!aberto}
                onEdit={aberto ? () => onEdit(item) : undefined}
                onToggle={() => onToggle(item)}
                onDelete={() => onDelete(item)}
                extras={aberto && <button type="button" className="icon-button" title="Baixar título" onClick={() => onBaixar(item)}><Check size={15} /></button>}
              />
            </div>
          );
        })}
      </PagedList>
    </section>
  );
}

function ConciliacaoScreen({ dados, retornos, inputRef, upload, uploading, reload, onEdit, onToggle, onDelete }) {
  const total = dados?.totalArquivos ?? 0;
  const lista = retornos.slice().reverse();
  return (
    <>
      <section className="hero">
        <div>
          <p className="eyebrow">Financeiro / Conciliação bancária</p>
          <h1>Retornos que fecham<br /><em>sem ruído.</em></h1>
          <p className="intro">Importe CNAB, acompanhe liquidações e mantenha o banco alinhado aos títulos.</p>
        </div>
        <input ref={inputRef} hidden type="file" accept=".ret,.txt" onChange={upload} />
        <button className="upload" onClick={() => inputRef.current?.click()} disabled={uploading}>
          <FileUp size={18} />{uploading ? 'Importando...' : 'Importar retorno'}
        </button>
      </section>
      <section className="metrics">
        <Metric label="Arquivos recebidos" value={total} note="ativos no banco" />
        <Metric label="Processados" value={`${total ? Math.round((dados?.processados ?? 0) / total * 100) : 0}%`} note={`${dados?.processados ?? 0} arquivos conciliados`} />
        <Metric label="Registros conciliados" value={(dados?.registros ?? 0).toLocaleString('pt-BR')} note="total de registros" positive />
      </section>
      <section className="panel conciliacao-panel">
        <div className="panel-title">
          <div><p className="eyebrow">Monitoramento assíncrono</p><h2>Arquivos de retorno</h2></div>
          <button className="icon-button" title="Atualizar" onClick={reload}><RefreshCw size={17} /></button>
        </div>
        <PagedList items={lista} empty="Nenhum retorno encontrado." className="dash-list">
          {visiveis => visiveis.map(item => (
            <div className={`activity${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
              <Activity size={20} />
              <div><strong>{item.nomeArquivo}</strong><span>{item.registros} registros · {item.ativo === false ? 'inativo' : 'ativo'}</span></div>
              <b className="status">{item.status}</b>
              <RowActions active={item.ativo !== false} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
            </div>
          ))}
        </PagedList>
      </section>
    </>
  );
}

function linhasExtrato(movimentos) {
  let saldo = 0;
  return [...(movimentos || [])].sort((a, b) => (a.id || 0) - (b.id || 0)).map(movimento => {
    saldo += movimento.tipo === 'SAIDA' ? -Number(movimento.valor || 0) : Number(movimento.valor || 0);
    return { ...movimento, saldo };
  });
}

function AccountScreen({ lista, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete, onLancar }) {
  const editing = Boolean(form.id);
  const [extratoId, setExtratoId] = useState(null);
  const [lancamento, setLancamento] = useState(lancamentoDefaults);
  const updateLancamento = field => event => setLancamento(value => ({ ...value, [field]: valorCampo(field, event) }));
  const conta = lista.find(item => item.id === extratoId);
  if (conta) {
    const linhas = linhasExtrato(conta.movimentos);
    return (
      <section className="registry screen">
        <div className="panel-title">
          <div>
            <p className="eyebrow">Financeiro / Contas</p>
            <h2>Extrato · {conta.descricao}</h2>
          </div>
          <ScrollText size={26} />
        </div>
        <p className="extrato-saldo">Saldo {money(conta.saldoAtual)}</p>
        <p className="extrato-meta">{conta.bancoCodigo} {conta.bancoNome} · {conta.contaResumo} · {tipoContaLabel(conta.tipo)}</p>
        <form className="company-form" onSubmit={event => onLancar(event, conta.id, lancamento, setLancamento)}>
          <label>Tipo
            <select value={lancamento.tipo} onChange={updateLancamento('tipo')}>
              <option value="ENTRADA">Entrada</option>
              <option value="SAIDA">Saída</option>
            </select>
          </label>
          <label>Descrição<input required value={lancamento.descricao} onChange={updateLancamento('descricao')} placeholder="LANÇAMENTO MANUAL" /></label>
          <label>Valor<input required type="number" min="0.01" step="0.01" value={lancamento.valor} onChange={updateLancamento('valor')} /></label>
          <div className="form-actions">
            <button className="save" disabled={saving}><Save size={17} />{saving ? 'Lançando...' : 'Lançar no extrato'}</button>
            <button type="button" className="cancel" onClick={() => setExtratoId(null)}><X size={16} />Voltar às contas</button>
          </div>
        </form>
        <PagedList items={linhas} empty="Nenhum lançamento neste extrato." resetKey={conta.id}>
          {visiveis => (
            <table className="extrato-table">
              <thead>
                <tr>
                  <th>Data</th>
                  <th>Histórico</th>
                  <th>Origem</th>
                  <th>Entrada</th>
                  <th>Saída</th>
                  <th>Saldo</th>
                </tr>
              </thead>
              <tbody>
                {visiveis.map(linha => (
                  <tr key={linha.id}>
                    <td>{dataHora(linha.dataMovimento)}</td>
                    <td>{linha.descricao}{linha.pedidoNumero ? ` · ${linha.pedidoNumero}` : ''}</td>
                    <td>{linha.origem === 'LANCAMENTO' ? 'LANÇAMENTO' : linha.origem}</td>
                    <td className="entrada">{linha.tipo === 'ENTRADA' ? money(linha.valor) : ''}</td>
                    <td className="saida">{linha.tipo === 'SAIDA' ? money(linha.valor) : ''}</td>
                    <td>{money(linha.saldo)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </PagedList>
      </section>
    );
  }
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Financeiro</p><h2>Contas correntes</h2></div>
        <Banknote size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>Descrição<input required value={form.descricao} onChange={update('descricao')} placeholder="CONTA PRINCIPAL" /></label>
        <label>Código do banco<input required maxLength="3" pattern="[0-9]{1,3}" value={form.bancoCodigo} onChange={update('bancoCodigo')} placeholder="341" /></label>
        <label>Nome do banco<input required value={form.bancoNome} onChange={update('bancoNome')} placeholder="ITAU" /></label>
        <label>Tipo<select value={form.tipo} onChange={update('tipo')}><option value="CORRENTE">Corrente</option><option value="POUPANCA">Poupança</option></select></label>
        <label>Agência<input required maxLength="20" inputMode="numeric" value={form.agencia} onChange={update('agencia')} /></label>
        <label>Dígito agência<input maxLength="2" inputMode="numeric" value={form.agenciaDigito} onChange={update('agenciaDigito')} /></label>
        <label>Conta<input required maxLength="20" inputMode="numeric" value={form.conta} onChange={update('conta')} /></label>
        <label>Dígito conta<input maxLength="2" inputMode="numeric" value={form.contaDigito} onChange={update('contaDigito')} /></label>
        <label>Titular<input value={form.titular} onChange={update('titular')} /></label>
        <label>Chave PIX<input className="keep-case" value={form.pix} onChange={update('pix')} /></label>
        <div className="form-actions">
          <CampoSituacao item={lista.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar conta' : 'Cadastrar conta'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={lista} empty="Nenhuma conta corrente cadastrada.">
        {visiveis => visiveis.map(item => (
          <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
            <Banknote size={18} />
            <div>
              <strong>{item.descricao}</strong>
              <span>{item.bancoCodigo} {item.bancoNome} · {item.contaResumo} · {tipoContaLabel(item.tipo)} · saldo {money(item.saldoAtual)}{item.pix ? ` · PIX ${item.pix}` : ''} · {item.ativo === false ? 'inativa' : 'ativa'}</span>
            </div>
            <RowActions
              active={item.ativo !== false}
              onEdit={() => onEdit(item)}
              onToggle={() => onToggle(item)}
              onDelete={() => onDelete(item)}
              extras={<button type="button" className="ghost compact" title="Extrato da conta" onClick={() => setExtratoId(item.id)}><ScrollText size={15} />Extrato</button>}
            />
          </div>
        ))}
      </PagedList>
    </section>
  );
}

function tipoContaLabel(tipo) {
  return tipo === 'POUPANCA' ? 'POUPANÇA' : 'CORRENTE';
}

function destinoForma(item) {
  if (!item) return '';
  if (item.destinoDescricao) return item.destinoDescricao;
  if (item.tipoDestino === 'CONTA_CORRENTE') {
    const conta = item.contaCorrente;
    return conta ? `${conta.descricao} · ${tipoContaLabel(conta.tipo)}` : 'BANCO SEM CONTA';
  }
  return item.caixa?.descricao || 'CAIXA';
}

function PaymentScreen({ formas, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete, contas, caixas }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Cadastros</p><h2>Formas de pagamento</h2></div>
        <CreditCard size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>Nome<input required value={form.nome} onChange={update('nome')} /></label>
        <label>Parcelas<input required type="number" min="1" step="1" value={form.parcelas} onChange={update('parcelas')} /></label>
        <label>Dias para vencimento<input required type="number" min="0" step="1" value={form.diasVencimento} onChange={update('diasVencimento')} /></label>
        <label>Destino do valor
          <select required value={form.tipoDestino} onChange={update('tipoDestino')}>
            <option value="CAIXA">Caixa</option>
            <option value="CONTA_CORRENTE">Banco (conta corrente ou poupança)</option>
          </select>
        </label>
        {form.tipoDestino === 'CAIXA' ? (
          <label>Caixa
            <select required value={form.caixaId} onChange={update('caixaId')}>
              <option value="">Selecione</option>
              {caixas.map(item => <option key={item.id} value={item.id}>{item.descricao}</option>)}
            </select>
          </label>
        ) : (
          <label>Conta bancária
            <select required value={form.contaCorrenteId} onChange={update('contaCorrenteId')}>
              <option value="">Selecione</option>
              {contas.map(item => <option key={item.id} value={item.id}>{item.descricao} · {tipoContaLabel(item.tipo)}</option>)}
            </select>
          </label>
        )}
        <div className="form-actions">
          <CampoSituacao item={formas.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar forma' : 'Cadastrar forma'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={formas} empty="Nenhuma forma de pagamento cadastrada.">
        {visiveis => visiveis.map(item => (
          <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
            <CreditCard size={18} />
            <div><strong>{item.nome}</strong><span>{item.parcelas}x · {item.diasVencimento} dia(s) · {item.tipoDestino === 'CONTA_CORRENTE' ? 'BANCO' : 'CAIXA'} {destinoForma(item)} · {item.ativo === false ? 'inativa' : 'ativa'}</span></div>
            <RowActions active={item.ativo !== false} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
          </div>
        ))}
      </PagedList>
    </section>
  );
}

function CaixaScreen({ lista, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete, onAbrir, onFechar }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Financeiro</p><h2>Caixa</h2></div>
        <CircleDollarSign size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>Descrição<input required value={form.descricao} onChange={update('descricao')} placeholder="CAIXA PRINCIPAL" /></label>
        <div className="form-actions">
          <CampoSituacao item={lista.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar caixa' : 'Cadastrar caixa'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={lista} empty="Nenhum caixa cadastrado.">
        {visiveis => visiveis.map(item => {
          const aberto = item.statusOperacao === 'ABERTO';
          const sessao = item.sessaoAberta;
          return (
            <div className={`company caixa-row${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
              <CircleDollarSign size={18} />
              <div>
                <strong>{item.descricao}</strong>
                <span>
                  {aberto ? 'ABERTO' : 'FECHADO'}
                  {aberto ? ` · saldo ${money(item.saldoAtual)} · inicial ${money(sessao?.saldoInicial)}` : ''}
                  {item.ativo === false ? ' · inativo' : ' · ativo'}
                  {sessao?.usuarioAbertura?.nome ? ` · ${sessao.usuarioAbertura.nome}` : ''}
                </span>
                {aberto && sessao?.movimentos?.length ? (
                  <PagedList items={sessao.movimentos} empty="" className="caixa-movimentos-pager" resetKey={item.id}>
                    {movimentos => (
                      <ul className="caixa-movimentos">
                        {movimentos.map(movimento => (
                          <li key={movimento.id}>
                            {movimento.tipo} · {movimento.origem} · {money(movimento.valor)} · {movimento.descricao}
                            {movimento.pedidoNumero ? ` · ${movimento.pedidoNumero}` : ''}
                          </li>
                        ))}
                      </ul>
                    )}
                  </PagedList>
                ) : null}
              </div>
              <RowActions
                active={item.ativo !== false}
                onEdit={() => onEdit(item)}
                onToggle={() => onToggle(item)}
                onDelete={() => onDelete(item)}
                extras={<>
                  {!aberto && item.ativo !== false && <button type="button" className="icon-button" title="Abrir caixa" onClick={() => onAbrir(item)}><Check size={15} /></button>}
                  {aberto && <button type="button" className="icon-button" title="Fechar caixa" onClick={() => onFechar(item)}><Ban size={15} /></button>}
                </>}
              />
            </div>
          );
        })}
      </PagedList>
    </section>
  );
}

function CarrierScreen({ lista, form, update, submit, saving, onCancel, onEdit, onToggle, onDelete }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Logística</p><h2>Transportadoras</h2></div>
        <Truck size={26} />
      </div>
      <form className="company-form" onSubmit={submit}>
        <label>Nome<input required value={form.nome} onChange={update('nome')} /></label>
        <label>CNPJ<input pattern="[0-9]{14}" maxLength="14" value={form.cnpj} onChange={update('cnpj')} /></label>
        <label>Telefone<input value={form.telefone} onChange={update('telefone')} /></label>
        <div className="form-actions">
          <CampoSituacao item={lista.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar transportadora' : 'Cadastrar transportadora'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={lista} empty="Nenhuma transportadora cadastrada.">
        {visiveis => visiveis.map(item => (
          <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
            <Truck size={18} />
            <div><strong>{item.nome}</strong><span>{item.cnpj || 'SEM CNPJ'} · {item.telefone || 'SEM TELEFONE'} · {item.ativo === false ? 'inativa' : 'ativa'}</span></div>
            <RowActions active={item.ativo !== false} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
          </div>
        ))}
      </PagedList>
    </section>
  );
}

function documentoPessoa(item) {
  if (item.tipoPessoa === 'FISICA') return item.cpf ? `CPF ${item.cpf}` : 'SEM CPF';
  return item.cnpj ? `CNPJ ${item.cnpj}` : 'SEM CNPJ';
}

function CompanyScreen({ empresas, form, setForm, update, submit, saving, onCancel, onEdit, onToggle, onDelete }) {
  const editing = Boolean(form.id);
  const fisica = form.tipoPessoa === 'FISICA';
  const [busca, setBusca] = useState('');
  const [cepStatus, setCepStatus] = useState('');
  const cepTimer = useRef(null);
  useEffect(() => () => clearTimeout(cepTimer.current), []);
  function avisoCep(texto, temporario) {
    clearTimeout(cepTimer.current);
    setCepStatus(texto);
    if (temporario) cepTimer.current = setTimeout(() => setCepStatus(''), 5000);
  }
  function mudarPessoa(event) {
    const tipoPessoa = event.target.value;
    setForm(atual => ({
      ...atual,
      tipoPessoa,
      cpf: tipoPessoa === 'FISICA' ? atual.cpf : '',
      rg: tipoPessoa === 'FISICA' ? atual.rg : '',
      cnpj: tipoPessoa === 'JURIDICA' ? atual.cnpj : '',
      nomeFantasia: tipoPessoa === 'JURIDICA' ? atual.nomeFantasia : '',
      inscricaoEstadual: tipoPessoa === 'JURIDICA' ? atual.inscricaoEstadual : ''
    }));
  }
  async function buscarCep(event) {
    const cep = event.target.value.replace(/\D/g, '').slice(0, 8);
    setForm(atual => ({ ...atual, cep }));
    if (cep.length !== 8) { clearTimeout(cepTimer.current); setCepStatus(''); return; }
    avisoCep('Consultando CEP...');
    try {
      const resposta = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
      const data = await resposta.json();
      if (data.erro) { avisoCep('CEP não encontrado'); return; }
      setForm(atual => ({
        ...atual,
        cep,
        logradouro: (data.logradouro || '').toLocaleUpperCase('pt-BR'),
        bairro: (data.bairro || '').toLocaleUpperCase('pt-BR'),
        cidade: (data.localidade || '').toLocaleUpperCase('pt-BR'),
        uf: (data.uf || '').toLocaleUpperCase('pt-BR')
      }));
      avisoCep('Endereço preenchido', true);
    } catch {
      avisoCep('Falha ao consultar o CEP');
    }
  }
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Cadastros</p><h2>Cliente/Fornecedor</h2></div>
        <Building2 size={26} />
      </div>
      <form className="company-form company-form-grouped" onSubmit={submit}>
        <fieldset className="form-group">
          <legend>Dados principais</legend>
          <label>Tipo de pessoa
            <select value={form.tipoPessoa} onChange={mudarPessoa}>
              <option value="JURIDICA">Pessoa jurídica</option>
              <option value="FISICA">Pessoa física</option>
            </select>
          </label>
          <label>Perfil<select value={form.tipo} onChange={update('tipo')}><option value="FORNECEDOR">Fornecedor</option><option value="CLIENTE">Cliente</option><option value="AMBOS">Cliente e fornecedor</option></select></label>
          {fisica
            ? <label>Nome<input required value={form.razaoSocial} onChange={update('razaoSocial')} /></label>
            : <>
              <label>Razão social<input required value={form.razaoSocial} onChange={update('razaoSocial')} /></label>
              <label>Nome fantasia<input value={form.nomeFantasia || ''} onChange={update('nomeFantasia')} /></label>
            </>}
          <label>E-mail<input type="email" value={form.email || ''} onChange={update('email')} /></label>
          <label>Celular
            <input inputMode="numeric" pattern="[0-9]{11}" maxLength="11" placeholder="43999991212" value={form.celular || ''}
              onChange={event => setForm(atual => ({ ...atual, celular: event.target.value.replace(/\D/g, '').slice(0, 11) }))} />
          </label>
          <label>Telefone
            <input inputMode="numeric" pattern="[0-9]{10}" maxLength="10" placeholder="4330330101" value={form.telefone || ''}
              onChange={event => setForm(atual => ({ ...atual, telefone: event.target.value.replace(/\D/g, '').slice(0, 10) }))} />
          </label>
        </fieldset>
        <fieldset className="form-group">
          <legend>Dados documentais</legend>
          {fisica ? <>
            <label>CPF<input required pattern="[0-9]{11}" maxLength="11" value={form.cpf} onChange={update('cpf')} /></label>
            <label>RG<input value={form.rg || ''} onChange={update('rg')} /></label>
          </> : <>
            <label>CNPJ<input required pattern="[0-9]{14}" maxLength="14" value={form.cnpj} onChange={update('cnpj')} /></label>
            <label>Inscrição estadual<input maxLength="11" value={form.inscricaoEstadual || ''} onChange={update('inscricaoEstadual')} /></label>
          </>}
        </fieldset>
        <fieldset className="form-group">
          <legend>Endereço</legend>
          <label>CEP
            <input required pattern="[0-9]{8}" maxLength="8" value={form.cep || ''} onChange={buscarCep} placeholder="00000000" />
            {cepStatus && <small className="cep-status">{cepStatus}</small>}
          </label>
          <label>Logradouro<input value={form.logradouro || ''} onChange={update('logradouro')} /></label>
          <label>Número<input value={form.numero || ''} onChange={update('numero')} /></label>
          <label>Complemento<input value={form.complemento || ''} onChange={update('complemento')} /></label>
          <label>Bairro<input value={form.bairro || ''} onChange={update('bairro')} /></label>
          <label>Cidade<input value={form.cidade || ''} onChange={update('cidade')} /></label>
          <label>UF<input maxLength="2" value={form.uf || ''} onChange={update('uf')} /></label>
        </fieldset>
        <div className="form-actions">
          <CampoSituacao item={empresas.find(item => item.id === form.id)} onToggle={onToggle} />
          <button className="save" disabled={saving}><Save size={17} />{saving ? 'Salvando...' : editing ? 'Salvar alterações' : 'Cadastrar'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <div className="list-toolbar">
        <label>Consultar cadastro
          <input value={busca} placeholder="NOME, CNPJ OU CPF" autoComplete="off" onChange={event => setBusca(valorCampo('busca', event))} />
        </label>
        <p className="list-count">{empresas.filter(item => coincideCadastro(item, busca)).length} cadastro(s)</p>
      </div>
      <PagedList items={empresas.filter(item => coincideCadastro(item, busca))} empty="Nenhum cadastro encontrado." resetKey={busca}>
        {visiveis => visiveis.map(item => (
          <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
            <Building2 size={18} />
            <div>
              <strong>{nomeEmpresa(item)}</strong>
              <span>{item.tipoPessoa === 'FISICA' ? 'PESSOA FÍSICA' : 'PESSOA JURÍDICA'} · {documentoPessoa(item)} · {item.tipo}{item.cidade ? ` · ${item.cidade}/${item.uf || ''}` : ''} · {item.ativo === false ? 'inativa' : 'ativa'}</span>
            </div>
            <RowActions active={item.ativo !== false} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
          </div>
        ))}
      </PagedList>
    </section>
  );
}

function UserScreen({ usuarios, form, update, submit, saving, currentId, onCancel, onEdit, onToggle, onDelete }) {
  const editing = Boolean(form.id);
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div><p className="eyebrow">Controle de acesso</p><h2>Usuários da operação</h2></div>
        <Users size={26} />
      </div>
      <form className="company-form user-form" onSubmit={submit}>
        <label>Nome<input required value={form.nome} onChange={update('nome')} /></label>
        <label>E-mail<input required type="email" value={form.email} onChange={update('email')} /></label>
        <label>{editing ? 'Nova senha (opcional)' : 'Senha temporária'}<input required={!editing} minLength="8" type="password" value={form.senha} onChange={update('senha')} /></label>
        <label>Perfil<select value={form.perfil} onChange={update('perfil')}><option value="OPERADOR">Operador</option><option value="ADMIN">Administrador</option></select></label>
        <div className="form-actions">
          <CampoSituacao item={usuarios.find(item => item.id === form.id)} onToggle={onToggle} disabled={form.id === currentId} />
          <button className="save" disabled={saving}><UserPlus size={17} />{saving ? 'Salvando...' : editing ? 'Salvar alterações' : 'Cadastrar usuário'}</button>
          {editing && <button type="button" className="cancel" onClick={onCancel}><X size={16} />Cancelar</button>}
        </div>
      </form>
      <PagedList items={usuarios} empty="Nenhum usuário cadastrado.">
        {visiveis => visiveis.map(item => {
          const locked = item.id === currentId;
          return (
            <div className={`company${item.ativo === false ? ' inactive' : ''}`} key={item.id}>
              <Users size={18} />
              <div><strong>{item.nome}{locked ? ' (você)' : ''}</strong><span>{item.email} · {item.perfil} · {item.ativo === false ? 'inativo' : 'ativo'}</span></div>
              <RowActions active={item.ativo !== false} locked={locked} disableToggle={locked} onEdit={() => onEdit(item)} onToggle={() => onToggle(item)} onDelete={() => onDelete(item)} />
            </div>
          );
        })}
      </PagedList>
    </section>
  );
}

function acaoLogLabel(acao) {
  const nomes = {
    CRIAR: 'CRIAÇÃO', ALTERAR: 'ALTERAÇÃO', EXCLUIR: 'EXCLUSÃO', SITUACAO: 'ATIVAÇÃO/INATIVAÇÃO',
    FATURAR: 'FATURAMENTO', CANCELAR: 'CANCELAMENTO', BAIXAR: 'BAIXA', LANCAMENTO: 'LANÇAMENTO',
    ABRIR: 'ABERTURA', FECHAR: 'FECHAMENTO', IMPORTAR: 'IMPORTAÇÃO', LOGIN: 'LOGIN', SAIR: 'SAÍDA'
  };
  return nomes[acao] || acao;
}

function LogScreen({ logs }) {
  const [busca, setBusca] = useState('');
  const filtrados = (logs || []).filter(item => {
    const termo = (busca || '').toLocaleUpperCase('pt-BR').trim();
    if (!termo) return true;
    return `${item.acao} ${item.descricao} ${item.usuarioNome} ${item.usuarioEmail} ${item.confirmadorNome || ''} ${item.confirmadorEmail || ''}`.toLocaleUpperCase('pt-BR').includes(termo);
  });
  return (
    <section className="registry screen">
      <div className="panel-title">
        <div>
          <p className="eyebrow">Sistema</p>
          <h2>Log do sistema</h2>
        </div>
        <History size={26} />
      </div>
      <p className="extrato-meta">Histórico das rotinas de criação, alteração, exclusão e acesso.</p>
      <div className="list-toolbar">
        <label>Consultar rotina
          <input value={busca} placeholder="AÇÃO, USUÁRIO OU MÓDULO" autoComplete="off" onChange={event => setBusca(valorCampo('busca', event))} />
        </label>
        <p className="list-count">{filtrados.length} rotina(s)</p>
      </div>
      <PagedList items={filtrados} empty="Nenhuma rotina registrada." resetKey={busca}>
        {visiveis => (
          <table className="extrato-table">
            <thead>
              <tr>
                <th>Data</th>
                <th>Rotina</th>
                <th>Módulo</th>
                <th>Usuário</th>
                <th>Confirmou</th>
              </tr>
            </thead>
            <tbody>
              {visiveis.map(item => (
                <tr key={item.id}>
                  <td>{dataHora(item.dataHora)}</td>
                  <td>{acaoLogLabel(item.acao)}</td>
                  <td>{(item.descricao || '').replace(/^[A-ZÁ-Ú]+ · /, '')}</td>
                  <td>{item.usuarioNome}<br /><span className="log-email">{item.usuarioEmail}</span></td>
                  <td>{item.confirmadorNome ? <>{item.confirmadorNome}<br /><span className="log-email">{item.confirmadorEmail}</span></> : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </PagedList>
    </section>
  );
}

function Metric({ label, value, note, positive }) {
  return <article className="metric"><span>{label}</span><strong>{value}</strong><small className={positive ? 'positive' : ''}>{note}</small></article>;
}

createRoot(document.getElementById('root')).render(<ConfirmProvider><App /></ConfirmProvider>);
