import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Buffer
{

	private static final String N_CLIENTES = "N_CLIENTES";
	private static final String N_SERVIDORES = "N_SERVIDORES";
	private static final String BUFFER_SIZE = "BUFFER_SIZE";
	private static final String N_MESSAGES = "N_MESSAGES";
	private static String PATH = "files/data2";

	private static int ID_CLT = 1;
	private Cliente  cliente;
	private Servidor servidor;
    private Buffer buffer;
    private ArrayList <Mensaje> mensajes;
    private int tamaño;
    private Integer clientesVivos;

    
    public Buffer (int tamañoCola, int cantClientes)
    {
    	mensajes = new ArrayList<Mensaje>();
    	tamaño = tamañoCola;
    	clientesVivos = cantClientes;
    }

    /*
     * Saca un mensaje de la lista
     **/
	public Mensaje sacar()
	{
		synchronized(mensajes) {
			if (mensajes.size() > 0)
			{
				Mensaje m = mensajes.get(0);
				mensajes.remove(m);
				return m;
			}
			else
				return null;
		}

	}

	/*
	*reduce la variable clientes vivos.
	**/
	public void salir() {
		synchronized(clientesVivos) {
			clientesVivos--;
		}

	}
	/*
	* Intenta meter el mensaje en la lista.
	* Si no puede cede procesador.
	* Si es exitoso, duerme el thread hasta que el mensaje sea procesado.
	**/
	public void meter(Mensaje mensaje) {
			synchronized(mensaje) {
				while(!hayEspacio(mensaje)) {
					Thread.yield();
				}

				try {
					mensaje.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}

	/*
	* Reviza si en la lista hay espacio, si lo haya
	* añade el mensaje a la lista.
	* @param mensaje mensaje que se quiere añadir a la lista.
	**/
	public boolean hayEspacio(Mensaje mensaje) {
		synchronized(mensajes) {
			if(mensajes.size() < tamaño) {
				mensajes.add(mensaje);
				return true;
			} else {
				return false;
			}
		}
	}

	/*
	* Revisa si hay clientes activos
	**/
	public boolean hayClientes() {
		synchronized(clientesVivos) {
			if(clientesVivos > 0) return true;
			return false;
		}
	}


	public static void main( String[] args )
	{
		Buffer buf = null;
		int [] mensajes = null;
		int numClientes = 0;
		int numServidores = 0;
		int tamBuffer = 0;
		boolean randomMessages = false;
		int base = 0;
		int random = 0;

		File f = new File(PATH);
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String l = br.readLine();
			while(l != null) {
				String[] opt = l.split(" ");
				if(opt[0].equals(BUFFER_SIZE)) {
					tamBuffer = Integer.parseInt(opt[1]);
				} else if(opt[0].equals(N_CLIENTES)) {
					numClientes = Integer.parseInt(opt[1]);
				} else if(opt[0].equals(N_MESSAGES)) {
					mensajes = new int[opt.length - 1];
					if(opt[1].equals("Y")) {
						randomMessages = true;
						random = Integer.parseInt(opt[2]);
						base = Integer.parseInt(opt[3]);
					}
					else {
						for(int i = 0; i < mensajes.length; i++) mensajes[i] = Integer.parseInt(opt[i+1]);
					}
				} else if(opt[0].equals(N_SERVIDORES)) {
					numServidores = Integer.parseInt(opt[1]);
				} l = br.readLine();
			}
		} catch (Exception e) { e.printStackTrace(); }

		buf = new Buffer(tamBuffer, numClientes);
		if(randomMessages) {
			mensajes = new int[numClientes];
			for(int i = 0; i < mensajes.length; i++) mensajes[i] = base + (int)(Math.random()*random);
		}

		for(int i = 0; i < numClientes; i++) {
			Cliente cli = new Cliente(mensajes[i], buf, 0, buf.ID_CLT );
			cli.start();
			buf.ID_CLT++;
		}

		for(int i = 0; i < numServidores; i++) {
			Servidor ser = new Servidor(buf);
			ser.start();
		}

	}
}
