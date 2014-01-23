package br.com.caelum.stella.boleto.transformer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import br.com.caelum.stella.boleto.Boleto;
import br.com.caelum.stella.boleto.exception.GeracaoBoletoException;

/**
 * Geração do boleto em arquivos. Essa é uma classe utilitária, dessa maneira
 * você não precisa trabalhar com InputStream, byte[], BoletoWriters, etc.
 * 
 * Basta passar um Boleto para o construtor e usar o método adequado para gerar
 * um PDF, PNG, etc e grava-lo como o arquivo desejado.
 * 
 * 
 */
@SuppressWarnings("unchecked")
public class GeradorDeBoleto {

	protected final Boleto[] boletos;
	protected InputStream templateJasper;
	protected JasperPrint relatorio;

	@SuppressWarnings("rawtypes")
	protected Map parametros = new HashMap();
	
	/**
	 * Cria um gerador de boletos com o template padrão
	 * 
	 * @param boletos
	 */
	public GeradorDeBoleto(Boleto... boletos) {
		this.boletos = boletos;
		templateJasper = GeradorDeBoleto.class.getResourceAsStream("/br/com/caelum/stella/boleto/templates/boleto-default.jasper");
		parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
	}
	
	/**
	 * Cria um gerador de boletos que usa um template customizado.
	 * 
	 * @param template o template (.jasper) a ser usado (obrigatório)
	 * @param parametros parametros extras para o relatório( opcional)
	 * @param boletos boletos
	 */
	@SuppressWarnings({ "rawtypes" })
	public GeradorDeBoleto(InputStream template, Map parametros, Boleto... boletos) {
		this(boletos);
		
		this.templateJasper = template;
		if(parametros != null){
			Set<Map.Entry> entrySet = parametros.entrySet();
			for (Entry entry : entrySet) {
				this.parametros.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Gera um boleto em PDF, e grava no caminho indicado
	 * 
	 * @param arquivo
	 */
	public void geraPDF(String arquivo) {
		geraPDF(new File(arquivo));
	}

	protected JasperPrint geraRelatorio(){
		try{
			if(relatorio == null){
				JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Arrays.asList(boletos));
				relatorio = JasperFillManager.fillReport(templateJasper,parametros,	dataSource);
			}
			return relatorio;
		}catch(Exception e){
			throw new GeracaoBoletoException(e);
		}
	}

	/**
	 * Gera um boleto em PDF, e grava no arquivo indicado
	 * 
	 * @param arquivo
	 */
	public void geraPDF(File arquivo) {
		try {
			OutputStream out = new FileOutputStream(arquivo);
			geraPDFHelper(out); 
		} catch (FileNotFoundException e) {
			throw new GeracaoBoletoException(e);
		}
	}
	
	protected void geraPDFHelper(OutputStream out){
		try {
			JasperPrint relatorio = geraRelatorio();
			JasperExportManager.exportReportToPdfStream(relatorio, out); 
		} catch (Exception e) {
			throw new GeracaoBoletoException(e);
		}
	}

	/**
	 * Gera um boleto em PNG, e grava no caminho indicado
	 * 
	 * @param arquivo
	 */
	public void geraPNG(String arquivo) {
		geraPNG(new File(arquivo));
	}

	/**
	 * Gera um boleto em PNG, e grava no arquivo indicado
	 * 
	 * @param arquivo
	 */
	public void geraPNG(File arquivo) {
		try {
			geraPNGHelper(new FileOutputStream(arquivo));
		} catch (FileNotFoundException e) {
			throw new GeracaoBoletoException(e);
		}
	}

	protected void geraPNGHelper(OutputStream out) {
		try {
			JasperPrint relatorio = geraRelatorio();
			BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(relatorio, 0, 2);
			ImageIO.write(image,"png",out);
		} catch (Exception e) {
			throw new GeracaoBoletoException(e);
		}
	}

	/**
	 * Devolve um array de bytes representando o PDF desse boleto ja gerado.
	 */
	public byte[] geraPDF() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		geraPDFHelper(stream);
		return stream.toByteArray();	
	}

	/**
	 * Devolve um array de bytes representando o PNG desse boleto ja gerado.
	 */
	public byte[] geraPNG() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		geraPNGHelper(stream);
		return stream.toByteArray();
	}
		
	/**
	 * Gera o boleto no formato pdf 
	 * @return inputStream com o conteúdo do arquivo
	 */
	public InputStream geraPDFStream() {
		return new ByteArrayInputStream(geraPDF());
	}

	/**
	 * Gera o boleto no formato png 
	 * @return inputStream com o conteúdo do arquivo
	 */
	public InputStream geraPNGStream() {
		return new ByteArrayInputStream(geraPNG());
	}
}
