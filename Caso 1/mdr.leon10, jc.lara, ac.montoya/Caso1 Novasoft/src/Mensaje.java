import javax.lang.model.element.Name;

public class Mensaje
{
	//constante para saber si los mensajes se generan aleatoriamente.
	public static int random = 1;
	//constante para saber si los mensajes de generan en serie.
	public static int serie = 0;
	//Contador para llevar el número en el que va la serie.
	private static int contador = 1;
	//variable para confirmar si el mensaje fue procesado o no.
	public int cache;
	//variable que representa el mensaje.
	private int mensaje;

	/*
	 * Constructor de la clase.
	 * genera el valor del mensaje.
	 * @Params tipo si el mensaje debe ser serial o aleatorio.
	 */
	public Mensaje (int tipo)
	{
		mensaje = 0;
		if (tipo == 0)
		{
			mensaje = contador++;
		}
		else if (tipo == 1)
		{
			mensaje = (int)(Math.random()*100000);
		}

		cache = mensaje;
	}

	/*
	 * Obtiene el valor del mensaje.
	 * @Return mensaje.
	 */
	public int getMensaje()
	{
		return mensaje;
	}

	/*
	 * Cambia el valor del mensaje.
	 * @Param mensaje: nuevo valor del mensaje.
	 */
	public void setMensaje(int mensaje)
	{
		this.mensaje = mensaje;
	}
	
	/*
	 * comprueba que el mensaje haya sido procesado.
	 * @Return true si el mensaje fue procesado.
	 */
	public boolean processed() {
		return (mensaje - cache) == 1;
	}
}
