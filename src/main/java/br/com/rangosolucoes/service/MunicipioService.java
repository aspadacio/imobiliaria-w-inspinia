package br.com.rangosolucoes.service;

import java.io.Serializable;

import javax.inject.Inject;

import br.com.rangosolucoes.model.TbEnderecoPessoa;
import br.com.rangosolucoes.model.TbMunicipio;
import br.com.rangosolucoes.repository.MunicipioRepository;
import br.com.rangosolucoes.util.jpa.Transacional;

public class MunicipioService implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Inject
	private MunicipioRepository municipioRepository;
	
	@Transacional
	public TbMunicipio salvar(TbMunicipio municipio){
		TbMunicipio municipioExistente = municipioRepository.porNomeDoMunicipio(municipio.getNoMunicipio());
		
		if(municipioExistente != null && municipioExistente.equals(municipio)){
			return municipioExistente;
		}else{
			return municipioRepository.salvar(municipio);
		}
	}

	/**
	 * Método responsável por retornar o objeto {@link TbMunicipio} a partir da FK do objeto {@link TbEnderecoPessoa}
	 * */
	@Transacional
	public TbMunicipio findByEnderecoPessoaId(Long id){
		return municipioRepository.findByEnderecoPessoaId(id);
	}
}
