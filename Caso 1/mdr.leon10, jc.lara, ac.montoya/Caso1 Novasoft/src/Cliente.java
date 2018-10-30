
public class Cliente extends Thread
{
	//Objeto de tipo buffer de donde se recibiran los mensajes.
	private Buffer buffer;
	//Numero de mensajes que debe entregar en el buffer.
	private int numMensajes;
	//Tipo de mensajes que se quiere generar.
	private int tipo;
	//Mensaje que se debe enviar
	private Mensaje mensaje;
	private int id;
	private int top;

	/*
	 * Constructor de la clase
	 * @param pCantidad cantidad de mensajes que el cliente debe generar y enviar.
	 * @param pBuffer buffer al que le envia los mensajes.
	 * @param pTipo tipo de mensaje que se debe generar.
	 * @id id del cliente.
	 */
	public Cliente(int pCantidad, Buffer pBuffer, int pTipo, int id)
	{
		buffer = pBuffer;
		numMensajes = pCantidad;
		tipo = pTipo;
		mensaje =null;
		this.id =id;
		top = pCantidad;
	}

	/*
	 * genera el mensaje que se debe entregar y lo asigna a la variable mensaje.
	 */
	public void generarMensaje ()
	{
		mensaje = new Mensaje(tipo);
	}

	/*
	 * método run de thread. genera un mensaje, lo envia al buffer
	 * reduce el número de mensajes pendientes y
	 * valida que el mensaje haya sido procesado correctamente.
	 */
	public void run()
	{
		while (numMensajes > 0)
		{
			generarMensaje();
			enviar();
			numMensajes--;
			validar();
		}
		buffer.salir();
		System.out.println("C:" + id + " DONE.");
	}

	/*
	 * Envia un mensaje al buffer
	 */
	public void enviar()
	{
		buffer.meter(mensaje);
	}

	/*
	 * Valida que el mensaje haya sido procesado correcatamente
	 */
	public void validar() {
		if(mensaje.processed()) {
			System.out.println("C:" + id + " (" + (top-numMensajes) + "/" + top + ")");
		}
		else {
			System.err.println("El cliente con id " + id + "!!!!!!!!!!!!!!! (" + (top-numMensajes) + "/" + top + ")!!!!!!!");
		}
	}
}
