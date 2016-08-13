package br.com.rangosolucoes.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.rangosolucoes.enumeration.Estados;
import br.com.rangosolucoes.model.TbBairro;
import br.com.rangosolucoes.model.TbEnderecoPessoa;
import br.com.rangosolucoes.model.TbMunicipio;
import br.com.rangosolucoes.model.TbPessoa;
import br.com.rangosolucoes.model.TbPessoaJuridica;
import br.com.rangosolucoes.model.TbPessoaTelefone;
import br.com.rangosolucoes.repository.ImobiliariaRepository;
import br.com.rangosolucoes.service.BairroService;
import br.com.rangosolucoes.service.ImobiliariaService;
import br.com.rangosolucoes.service.EnderecoPessoaService;
import br.com.rangosolucoes.service.MunicipioService;
import br.com.rangosolucoes.service.PessoaService;
import br.com.rangosolucoes.service.PessoaTelefoneService;
import br.com.rangosolucoes.util.jsf.FacesUtil;

@Named("cadastroImobiliariasBean")
@SessionScoped
public class CadastroImobiliariasBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private ImobiliariaService imobiliariaService;
	
	@Inject
	private PessoaService pessoaService;
	
	@Inject
	private PessoaTelefoneService pessoaTelefoneService;
	
	@Inject
	private MunicipioService municipioService;
	
	@Inject
	private BairroService bairroService;
	
	@Inject
	private EnderecoPessoaService enderecoPessoaService;
	
	@Inject
	private ImobiliariaRepository imobiliariaRepository;
	
	private TbPessoaJuridica pessoaJuridica;
	private TbPessoaTelefone pessoaTelefone;
	private TbPessoa pessoa;
	private TbEnderecoPessoa enderecoPessoa;
	private TbMunicipio municipio;
	private TbBairro bairro;
	
	private String sgUF;
	private String nuCep;
	private String nuTelefoneDdd;
	private String nuTelefone;
	private String tpTelefone;
	private List<TbPessoaTelefone> telefones;

	@PostConstruct
	public void init() {
		limpar();
	}
	
	public boolean isEditando() {
		return this.pessoaJuridica.getNuCnpj() != null && !this.pessoaJuridica.getNuCnpj().equals("");
	}

	public String novoCadastro() {
		limpar();
		return "/imobiliaria/CadastroImobiliaria?faces-redirect=true";
	}

	public void salvar() {
		if(camposPreenchidos()){
			pessoaJuridica.setNoContato("a definir");
			String cnpjTemp = pessoaJuridica.getNuCnpj().replace(".", "").replace("/", "").replace("-", "");
			pessoaJuridica.setNuCnpj(cnpjTemp);
			
			boolean existePessoaJuridica = imobiliariaRepository.porCNPJ(pessoaJuridica.getNuCnpj()) != null;
			
			if(existePessoaJuridica){
				FacesUtil.addInfoMessage("Imobiliária alterada com sucesso!");
			}else{
				FacesUtil.addInfoMessage("Imobiliária cadastrada com sucesso!");
			}
			
			pessoaJuridica = imobiliariaService.salvar(pessoaJuridica);
			
			pessoa = populaPessoa(pessoaJuridica);
			pessoa = pessoaService.salvar(pessoa);
			
			if(telefones.size() > 0){
				for(TbPessoaTelefone pessoaTelefone : telefones){
					pessoaTelefone.setTbPessoa(pessoa);
					this.pessoaTelefone = pessoaTelefoneService.salvar(pessoaTelefone);
				}
			}else{
				FacesUtil.addErrorMessage("É necessário informar ao menos um telefone para contato na imobiliária.");
			}
			
			municipio.setSgUf(sgUF);
			municipio = municipioService.salvar(municipio);
			
			bairro.setTbMunicipio(municipio);
			bairro = bairroService.salvar(bairro);
			
			enderecoPessoa.setNuCep(Integer.valueOf(this.nuCep.replace(".", "").replace("-", "")));
			enderecoPessoa.setTbPessoa(pessoa);
			enderecoPessoa.setTbMunicipio(municipio);
			enderecoPessoa.setTbBairro(bairro);
			enderecoPessoa = enderecoPessoaService.salvar(enderecoPessoa);
			
			limpar();
		}
	}
	
	private TbPessoa populaPessoa(TbPessoaJuridica pessoaJuridica) {
		pessoa.setTbPessoaJuridica(pessoaJuridica);
		pessoa.setDtUltimaAlteracao(new Date());
		return pessoa;
	}

	public void limpar() {
		pessoaJuridica = new TbPessoaJuridica();
		pessoaTelefone = new TbPessoaTelefone();
		pessoa = new TbPessoa();
		enderecoPessoa = new TbEnderecoPessoa();
		municipio = new TbMunicipio();
		bairro = new TbBairro();
		telefones = new ArrayList<>();
		
		nuCep = "";
		sgUF = "";
		nuTelefoneDdd = "";
		nuTelefone = "";
		tpTelefone = "";
		
	}
	
	public void adicinaTelefoneNaLista(){
		if(nuTelefone != "" && nuTelefone != null && nuTelefoneDdd != "" && nuTelefoneDdd != null &&
				tpTelefone != "" && tpTelefone != null){
			pessoaTelefone.setNuTelefoneDdd(nuTelefoneDdd.replace("(", "").replace(")", ""));
			pessoaTelefone.setNuTelefone(Integer.valueOf(nuTelefone.replace("-", "")));
			pessoaTelefone.setTpTelefone(tpTelefone.charAt(0));
			telefones.add(pessoaTelefone);
			
			nuTelefoneDdd = "";
			nuTelefone = "";
			tpTelefone = "";
			pessoaTelefone = new TbPessoaTelefone();
		}else{
			FacesUtil.addErrorMessage("É necessário informar todos os dados do telefone.");
		}
	}
	
	public boolean camposPreenchidos(){
		boolean preenchido = true;
		
		if(pessoaJuridica.getNoRazaoSocial() == null || pessoaJuridica.getNoRazaoSocial() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Razão Social é obrigatório.");
		}
		
		if(pessoaJuridica.getNoFantasia() == null || pessoaJuridica.getNoFantasia() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Nome Fantasia é obrigatório.");
		}
		
		if(pessoaJuridica.getNuInscricaoEstadual() == null || pessoaJuridica.getNuInscricaoEstadual() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Incrição Estadual é obrigatório.");
		}
		
		if(pessoaJuridica.getNuCnpj() == null || pessoaJuridica.getNuCnpj() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo CNPJ é obrigatório.");
		}
		
		if(pessoa.getDsEmail() == null || pessoa.getDsEmail() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo E-Mail é obrigatório.");
		}
		
		if(this.nuCep == null || this.nuCep == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo CEP é obrigatório.");
		}
		
		if(this.sgUF == null || this.sgUF == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo UF é obrigatório.");
		}
		
		if(enderecoPessoa.getDsEndereco() == null || enderecoPessoa.getDsEndereco() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Endereço é obrigatório.");
		}
		
		if(enderecoPessoa.getNuEndereco() == null){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Número é obrigatório.");
		}
		
		if(bairro.getNoBairro() == null || bairro.getNoBairro() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Bairro é obrigatório.");
		}
		
		if(municipio.getNoMunicipio() == null || municipio.getNoMunicipio() == ""){
			preenchido = false;
			FacesUtil.addErrorMessage("O campo Município é obrigatório.");
		}
		
		return preenchido;
	}
	
	public void alteraImobiliariaSelecionada(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		/*Valida se é um postBack ou um ValidationFailed. Só entra se for uma requisição
		  Valida tbm se está enviando o atributo 'imobiliaria'*/
		
		if(!facesContext.isPostback() && !facesContext.isValidationFailed() &&
				facesContext.getExternalContext().getRequestParameterMap().get("imobiliaria") != null){
			this.pessoa = imobiliariaRepository.consultaPessoa(pessoaJuridica.getNuCnpj());
			this.telefones = imobiliariaRepository.consultaTelefonesPessoa(this.pessoa.getIdPessoa());
			this.enderecoPessoa = imobiliariaRepository.consultaEnderecoPessoa(this.pessoa.getIdPessoa());
			this.nuCep = String.valueOf(this.enderecoPessoa.getNuCep());
			this.bairro = this.enderecoPessoa.getTbBairro();
			this.municipio = this.enderecoPessoa.getTbMunicipio();
			this.sgUF = this.municipio.getSgUf();
		}
	}
	
	public Estados[] getEstados(){
		return Estados.values();
	}

	public TbPessoaJuridica getPessoaJuridica() {
		return pessoaJuridica;
	}

	public void setPessoaJuridica(TbPessoaJuridica pessoaJuridica) {
		this.pessoaJuridica = pessoaJuridica;
	}

	public TbPessoaTelefone getPessoaTelefone() {
		return pessoaTelefone;
	}

	public void setPessoaTelefone(TbPessoaTelefone pessoaTelefone) {
		this.pessoaTelefone = pessoaTelefone;
	}

	public TbPessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(TbPessoa pessoa) {
		this.pessoa = pessoa;
	}

	public TbEnderecoPessoa getEnderecoPessoa() {
		return enderecoPessoa;
	}

	public void setEnderecoPessoa(TbEnderecoPessoa enderecoPessoa) {
		this.enderecoPessoa = enderecoPessoa;
	}

	public TbMunicipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(TbMunicipio municipio) {
		this.municipio = municipio;
	}

	public TbBairro getBairro() {
		return bairro;
	}

	public void setBairro(TbBairro bairro) {
		this.bairro = bairro;
	}

	public List<TbPessoaTelefone> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<TbPessoaTelefone> telefones) {
		this.telefones = telefones;
	}

	public ImobiliariaService getImobiliariaService() {
		return imobiliariaService;
	}

	public void setImobiliariaService(ImobiliariaService imobiliariaService) {
		this.imobiliariaService = imobiliariaService;
	}

	public String getNuTelefoneDdd() {
		return nuTelefoneDdd;
	}

	public void setNuTelefoneDdd(String nuTelefoneDdd) {
		this.nuTelefoneDdd = nuTelefoneDdd;
	}

	public String getNuTelefone() {
		return nuTelefone;
	}

	public void setNuTelefone(String nuTelefone) {
		this.nuTelefone = nuTelefone;
	}

	public String getTpTelefone() {
		return tpTelefone;
	}

	public void setTpTelefone(String tpTelefone) {
		this.tpTelefone = tpTelefone;
	}

	public String getNuCep() {
		return nuCep;
	}

	public void setNuCep(String nuCep) {
		this.nuCep = nuCep;
	}

	public String getSgUF() {
		return sgUF;
	}

	public void setSgUF(String sgUF) {
		this.sgUF = sgUF;
	}

}
