
public class Servidor extends Thread
{
	//Objeto de tipo buffer de donde se recibiran los mensajes.
	private Buffer buffer;
	//Objeto de tipo mensaje. Valor a procesar.
	private Mensaje mensaje;

	/*
	* Constructor de la clase
	* @Param recibe el buffer sobre el cual se va a trabajar.
	*/
	public Servidor(Buffer pBuffer)
	{
		buffer = pBuffer;
	}

	/*
	 * método run de los threads.
	 * Mientras haya clientes recibe mensajes del buffer, los procesa
	 * despierta al cliente.
	 */
	public void run()
	{
		while(buffer.hayClientes()) {
			mensaje = buffer.sacar();
			if(mensaje != null) {
				procesar(mensaje);
				synchronized(mensaje) {
					mensaje.notify();
				}
			}

		}
	}
	
	/*
	 * Procesa los mensajes que recibe del buffer.
	 * @Param pMensaje valor del mensaje.
	 */
	public void procesar (Mensaje pMensaje)
	{
		pMensaje.setMensaje(pMensaje.getMensaje()+1);
	}
}
