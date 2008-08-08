package airldm2.core;

/*
 * @author neeraj
 */

public interface OptionHandler {

   public abstract void setOptions(String[] options);

   public abstract String[] getOptions();

   public abstract boolean containsOption(String opt);

}