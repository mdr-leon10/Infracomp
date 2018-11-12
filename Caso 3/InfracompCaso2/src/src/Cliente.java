package src;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.bcpg.InputStreamPacket;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class Cliente {
    //Algoritmos
    private static final String MD5 = "HMACMD5";
    private static final String SHA1 = "HMACSHA1";
    private static final String SHA256 = "HMACSHA256";
    private static final String AES = "AES";
    private static final String RSA = "RSA";
    private static final String BF = "Blowfish";
    private static final String PADDING = "/ECB/PKCS5Padding";
    
    //Cadenas de protocolo
    private static final String CTIMEOUT = "Connection timed out";
    private static final String SIN = "Server << ";
    private static final String SOUT = "Client >> ";
    private static final String INIC = "HOLA";
    private static final String ALG = "ALGORITMOS";
    private static final String CC = "Certificado del cliente";
    private static final String CS = "Certificado del servidor";
    private static final String EST_OK = "OK";
    private static final String EST_ERROR = "ERROR";
    private static final String ERRCERT = "No es valido el certificado dado";
    private static final String CONSULTA = "CONS1: ";
    private static final String HCONSULTA = "HASHCONS: ";
    private long timeSimetrica = 0;
    private long timeResp = 0;
    private static final int TIMEOUT = 1000000;
    
    //Puerto
    private static final int PORT = 9160;
    
    //Cliente
    PrintWriter writer = null;
    BufferedReader reader = null;
    String [] ALGORITMOS = null;
    Socket socket = null;
    X509Certificate cliCert = null;
    KeyPair key = null;
    private boolean sent = false;
    
    //Servidor
    X509Certificate servCert = null;
    PublicKey llavePublica = null;
    SecretKey llaveSecreta = null;
    
    //BufferReader para las interacciónes con el usuario
    BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
    
    //lectura de bf
    int code = 0;
    
    //Le pregunta al usuario el código de autentificación
    public void autenticarse() throws IOException
    {
        code = (int)(Math.random()*1000);
    }
    
    public void establecerPuerto () throws IOException {
    	int port = 9160;
    	   try 
    	   {
              socket = new Socket(InetAddress.getLocalHost(), port);
           } 
    	   catch (Exception e) 
    	   { 
    		   throw e; 
    		 
    	   }
           System.out.println("Conectado al puerto " + port);

           writer = new PrintWriter(socket.getOutputStream(), true);
           reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void establecerConexion () throws IOException
    {
        String r = "y";
        
        if(r.equals("1") || r.toLowerCase().equals("y")) {
            writer.println(INIC);
            System.out.println(SOUT + INIC);
        }
        else
            System.exit(0);
        
        //respuesta del servidor
        String line = reader.readLine();
        if (line != null) {
            System.out.println(SIN + line);
        }
        else
        {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }
        
        
    }
      
    public void confirmarAlgoritmos() throws IOException
    {
        ALGORITMOS = algoritmos();
        String msg = ALG + ":" + ALGORITMOS[0] + ":RSA:" + ALGORITMOS[1];
        writer.println(msg);
        System.out.println(SOUT + msg);
        
        //Recibir confirmacion
        String r = reader.readLine();
        if (r!=null)
        {
            System.out.println(SIN + r);
        }
        else
        {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }
    }
    
    public String[] algoritmos() throws IOException {
        String[] alg = new String[2];

        System.out.println("Que algoritmo simetrico desea usar?" +
                "\n" + "(1) AES" + "\n" + "(2) Blowfish");
        if(bf.readLine().equals("2")) alg[0] = BF;
        else alg[0] = AES;

        System.out.println("Que algoritmo HMAC desea usar?" +
                "\n" + "(1) MD5" + "\n" + "(2) SHA1" + "\n" + "(3) SHA256");
        String s = bf.readLine();
        if(s.equals("2")) alg[1] = SHA1;
        else if(s.equals("3")) alg[1] = SHA256;
        else alg[1] = MD5;

        return alg;
    }
        
    public byte[] toByteArray(Object obj) {
    	byte[] arregloBytes = null;
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutput out = null;
    	try {
    	  out = new ObjectOutputStream(bos);   
    	  out.writeObject(obj);
    	  out.flush();
    	  arregloBytes = bos.toByteArray();
    	  bos.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    		try {
				bos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
    	
    	return arregloBytes;
    }
    
    public void intercambiarCertificados() throws IOException {

        //Generar certificado
        try {
            cliCert = generarCertificado("SHA256WithRSA");
        } catch (Exception e) { }
        if(cliCert == null) System.exit(-1);

        //Enviar certificado
        //writer.println(CC);
        System.out.println(SOUT + CC);
        try {
            writer.println(DatatypeConverter.printHexBinary(cliCert.getEncoded()));
        } catch (Exception e) { e.printStackTrace(); }

        //Leer resultado certificado
        String s = reader.readLine();
        if(s != null) {
            System.out.println(SIN + s);
        }
        else {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }

        //Obtener certificado del servidor y extraer la PublicKey
        try {
        	byte[] read = DatatypeConverter.parseHexBinary(reader.readLine());
            servCert = (X509Certificate) (CertificateFactory.getInstance("X.509")).generateCertificate(new ByteArrayInputStream(read));
        } catch (Exception e) {
            writer.println(EST_ERROR);
            System.out.println(SOUT + EST_ERROR);
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        llavePublica = servCert.getPublicKey();

        //Validar fecha certificado
        Date a = servCert.getNotAfter();
        Date b = servCert.getNotBefore();
        Date d = new Date();
        if(a.compareTo(d) * d.compareTo(b) > 0) {
            writer.println(EST_OK);
            System.out.println(SOUT + EST_OK);
        }
        else {
            writer.println(EST_ERROR);
            System.out.println(ERRCERT);
            System.exit(-1);
        }
    }
    
    public java.security.cert.X509Certificate generarCertificado(String algorithm) throws Exception
    {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(1024);
        key = keygen.generateKeyPair();
        Date notBefore = new Date();
        Date notAfter = new Date(2019, 12, 31);
        BigInteger randomSerial = new BigInteger(32,new Random());
        Security.addProvider(new BouncyCastleProvider());

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(new X500Name("CN=Cert"),
                randomSerial, notBefore, notAfter, new X500Name("CN=JAGV"),
                new SubjectPublicKeyInfo(ASN1Sequence.getInstance(key.getPublic().getEncoded())));

        AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(key.getPrivate().getEncoded());
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithm);
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        X509CertificateHolder holder = builder.build((new BcRSAContentSignerBuilder(sigAlgId, digAlgId)).build(privateKeyAsymKeyParam));

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }
    
    public void obtenerLlaveSimetrica() throws IOException {
        //Leer mensaje encriptado
        String s = reader.readLine();
        System.out.println(SIN + s);

        //Descifrar llave del mensaj
        byte[] llaveBytes = RSACipher.descifrar(DatatypeConverter.parseHexBinary(s), key.getPrivate());
        llaveSecreta = new SecretKeySpec(llaveBytes, ALGORITMOS[0] + PADDING);
    }
    
    public void generarConsulta() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String sCode = String.valueOf(code);
        
        //Cifrar consulta
        byte[] consultaBytes = AESCipher.cifrar(sCode, llaveSecreta, ALGORITMOS[0] + PADDING);
        String consulta = DatatypeConverter.printHexBinary(consultaBytes);

        //Enviar consulta cifrado
        System.out.println(SOUT + CONSULTA + consulta);
        writer.println(consulta);

        //Obtener MAC de consulta
        byte[] macText = getMAC(sCode.getBytes(), llaveSecreta, ALGORITMOS[1]);
        //byte[] hashBytes = RSACipher.cifrar(llavePublica, macText);
        String hash = DatatypeConverter.printHexBinary(macText).toUpperCase();
        System.out.println(SOUT + HCONSULTA + hash);
        writer.println(hash);
    }
    
    public static byte[] getMAC(byte[] text, Key key, String alg) throws NoSuchAlgorithmException, InvalidKeyException
    {
        Mac macGen = Mac.getInstance(alg);
        macGen.init(key);
        return macGen.doFinal(text);
    }
    
    public void esperarRespuesta() throws IOException {
        //Leer respuesta servidor
        String s = reader.readLine();
        if(s != null) {
            System.out.println(SIN + s);
        }
        else {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }
    }
    
    public void retornarLlaveSimetrica() throws IOException {
    	//Cifrar llave simetrica
    	byte[] llaveSimCifrada = RSACipher.cifrar(llavePublica, llaveSecreta.getEncoded());
    	String cipheredKey = DatatypeConverter.printHexBinary(llaveSimCifrada);
    	
    	//Enviar llave simetrica
    	writer.println(cipheredKey);
    	System.out.println(SIN + cipheredKey);
    	
    	//Esperar respuesta servidor
    	String r = reader.readLine();
        if (r!=null)
        {
            System.out.println(SIN + r);
        }
        else
        {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }
    }
    
    public void enviar()
    {
    	try {
    			timeResp = System.currentTimeMillis();
    			autenticarse();
    			establecerPuerto();
    			establecerConexion();
    			definirAlgoritmos();
    			timeSimetrica = System.currentTimeMillis();
    			intercambiarCertificados();
    			timeSimetrica = System.currentTimeMillis() - timeSimetrica;
    			obtenerLlaveSimetrica();;
    			retornarLlaveSimetrica();
    			generarConsulta();
    			esperarRespuesta(); 
    			sent = true;
    			timeResp = System.currentTimeMillis() - timeResp;
    		} 
    	catch (Exception e){ e.printStackTrace(); timeSimetrica = 0; timeResp = 0; sent = false; }
    }
    
    private void definirAlgoritmos() throws IOException {
    	ALGORITMOS = new String[2];
    	ALGORITMOS[0] = AES;
    	ALGORITMOS[1] = MD5;
    	
    	String msg = ALG + ":" + ALGORITMOS[0] + ":RSA:" + ALGORITMOS[1];
        writer.println(msg);
        System.out.println(SOUT + msg);
        
        //Recibir confirmacion
        String r = reader.readLine();
        if (r!=null)
        {
            System.out.println(SIN + r);
        }
        else
        {
            System.out.println(CTIMEOUT);
            System.exit(-1);
        }
	}

	public long getTimeSim(){
        return timeSimetrica;
    }

    public long getTimeAct(){
        return timeResp;
    }

    public boolean isSent() {
        return sent;
    }
    
    public static void main(String[] args) {
		Cliente c = new Cliente();
			c.enviar();
	}

}