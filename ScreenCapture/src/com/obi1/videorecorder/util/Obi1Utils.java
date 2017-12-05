package com.obi1.videorecorder.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

public class Obi1Utils {

	public static final String DEFAULT_TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss";
	
	/**
	 * Construtor oculto para a classe não ser instanciada.
	 */
	private Obi1Utils() { }
	
	/**
	 * Verifica se uma string está vazia.
	 * @param val string
	 * @return boolean informando se a string está vazia
	 */
	public static boolean isEmpty(String val) {
		return val == null || "".equals(val.trim());
	}

	/**
	 * Retorna a string de um número completando com 1 zero a esquerda caso ele seja menor que 10.
	 * @param val numero
	 * @return string com 2 digitos
	 */
	public static String getNumber2Digits(Integer val) {
		String result = "";
		if (val < 10) {
			result += "0" + val;
		}
		else {
			result += val;
		}
		
		return result;
	}

	/**
	 * Limpa todo o conteúdo do diretório passado como parametro.
	 * @param path caminho do diretório a ser limpo
	 * @throws IOException exception
	 */
	public static void cleanupDir(String path) throws IOException {
		File dir = new File(path);
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (!file.isDirectory()) {
					FileUtils.forceDelete(file);
				}
				else {
					FileUtils.deleteDirectory(file);
				}
			}
		}
		FileUtils.deleteDirectory(dir);
	}

	/**
	 * Converte uma data em String para long.
	 * @param date data a ser convertida (formato string)
	 * @return long convertido
	 * @throws ParseException exception
	 */
	public static long strDateToLong(String date) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
		return format.parse(date).getTime();
	}
}
