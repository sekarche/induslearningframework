package airldm2.core;

/*
 * @author neeraj
 */
public class DefaultOptionHandlerImpl implements OptionHandler {
   String[] options;

   /* (non-Javadoc)
    * @see airldm2.core.OptionHandler#setOptions(java.lang.String[])
    */
   public void setOptions(String[] options) {
      this.options = options;
   }

   /* (non-Javadoc)
    * @see airldm2.core.OptionHandler#getOptions()
    */
   public String[] getOptions() {
      return options;
   }

   /* (non-Javadoc)
    * @see airldm2.core.OptionHandler#containsOption(java.lang.String)
    */
   public boolean containsOption(String opt) {
      if (opt == null || options == null)
         return false;
      boolean found = false;
      for (int i = 0; i < options.length; i++) {
         if (found)
            break;
         if (opt.equals(options[i]))
            found = true;

      }
      return found;
   }
}
