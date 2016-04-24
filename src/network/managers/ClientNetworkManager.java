package network.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import network.messages.NetworkMessage;
import util.SecurityUtils;

public class ClientNetworkManager extends NetworkManager {

	public ClientNetworkManager(Socket socket) {
		super(socket);
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	public boolean sendFile(String filePath, int fileSize, SecretKey key) {
		boolean isValid = true;

		try {
			sendByteFile(filePath, fileSize, key);
		} catch (IOException e) {
			e.printStackTrace();
			isValid = false;
		}

		return isValid;
	}
	
	public File receiveFile(int fileSize, String name, SecretKey key) throws IOException {

		File file = new File(name);
		FileOutputStream fileOut = new FileOutputStream(file);

		int packageSize = getPackageSize();
		int currentLength = 0;
		byte[] bfile = new byte[packageSize];
		int lido;

		while (currentLength < fileSize) {
			int resto = fileSize - currentLength;
			int numThisTime = resto < packageSize ? resto : bfile.length;
			lido = in.read(bfile, 0, numThisTime);

			if (lido == -1) {
				break;
			}
			
			

			fileOut.write(bfile, 0, numThisTime);
			currentLength += lido;
		}

		fileOut.close();

		return file;
	}
	
	/**
	 * 
	 * @param name
	 * @param fileSize
	 * @throws IOException
	 */
	
	private void sendByteFile(String name, int fileSize, SecretKey key) throws IOException {
		int packageSize = PACKAGE_SIZE;
		
		System.out.println("NOME= " + name  );
		
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Stream para ler do ficheiro
		FileInputStream fileInputStream = new FileInputStream(name);
		int currentLength = 0;
		int i = 0;
		byte[] bfile = new byte [packageSize] ;
		
		
		/*
		//Stream para cifrar e enviar para o socket;
		CipherOutputStream cos = new CipherOutputStream(out, cipher);
		
		
		
		while ((i = fileInputStream.read(bfile)) != -1) {
			System.out.println("li= " + i  );
			currentLength  += i; 
			cos.write(bfile, 0, i);	
		}
		System.out.println("[ClientNetworkMAnager] sendByteFile total = " + currentLength);
		cos.flush();
		*/
		
		/*
		while (currentLength < fileSize) {
			if ((fileSize - currentLength) < packageSize)
				bfile = new byte[(fileSize - currentLength)];
			else
				bfile = new byte[packageSize];

			int lido = fileInputStream.read(bfile, 0, bfile.length);
			currentLength += lido;
			
			//bfile = SecurityUtils.cipherWithSessionKey(bfile, key);
			
			out.write(bfile, 0, bfile.length);
		}
	*/
		byte [] ciphered = null;
		while (currentLength < fileSize) {
			if ((fileSize - currentLength) < packageSize)
				bfile = new byte[(fileSize - currentLength)];
			else
				bfile = new byte[packageSize];

			int lido = fileInputStream.read(bfile, 0, bfile.length);
			currentLength += lido;
			System.out.println("[ClientNetworkMAnager] sendByteFile lido  = " + lido);
			
			if(bfile.length == packageSize)
				ciphered = cipher.update(bfile);
			else
				try {
					ciphered = cipher.doFinal(bfile);
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			System.out.println("[ClientNetworkMAnager] sendByteFile = " + ciphered.length);
			
			out.write(ciphered, 0, ciphered.length);
		}

		out.flush();
		
		
		fileInputStream.close();
	}

	
}
