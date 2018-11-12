package src;
import java.lang.management.ManagementFactory;

import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;


import uniandes.gload.core.Task;

public class ClientServerTask extends Task {

    private long tiempoActualizar = 0;
    private long tiempoLlave = 0;
    private double cpuTime = 0;
    private DataLogger dataLogger;

    public ClientServerTask(DataLogger dataLogger) {
        this.dataLogger = dataLogger;
    }

    @Override
    public void execute() {
        Cliente client = new Cliente();
        client.enviar();
        try {
        	cpuTime = getProcessCpuLoad();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        tiempoActualizar = client.getTimeAct();
        tiempoLlave = client.getTimeSim();
        dataLogger.logData(tiempoActualizar, tiempoLlave, cpuTime, client.isSent());
        if(client.isSent()) {
            success();
        } else {
            fail();
        }
    }

    @Override
    public void fail() {
        System.out.println("Fallo");
    }

    @Override
    public void success() {
        System.out.println("Sirvio");
        System.out.println("Tiempo actualizacion: " + tiempoActualizar);
        System.out.println("Tiempo llave: " + tiempoLlave);

    }
    
    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }
}