package airldm2.util;

import airldm2.core.LDInstances;

public interface ArffWriterI {

	public abstract String writeArff(LDInstances data, String fileName);

}